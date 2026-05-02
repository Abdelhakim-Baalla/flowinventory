package com.flowinventory.core;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.network.NetworkHandler;
import com.flowinventory.profiles.ActivityType;
// InventoryScanner sits in the same package, so no import needed.
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Heart of FlowInventory's intelligence.
 * <p>
 * Each tick this class collects dozens of signals from the player
 * (held item, off-hand item, worn armor, nearby blocks, nearby mobs,
 * dimension, biome, time of day, light level, recent damage, health,
 * hunger, fire, water depth, fall distance, status effects, manual
 * hotbar scroll, etc.) and produces a weighted vote for every
 * {@link ActivityType}. The activity with the highest score above a
 * confidence threshold becomes "current".
 * <p>
 * Important behaviour:
 * <ul>
 *   <li>If the player just scrolled their hotbar manually, the detector
 *       enters a "manual override" window during which it stops sending
 *       activity-change packets so the player keeps the slot they
 *       picked. Emergencies bypass this window.</li>
 *   <li>If a hostile mob is close and the player isn't holding a weapon
 *       (and they own one in the inventory), the detector triggers
 *       {@link ActivityType#EMERGENCY_COMBAT}, which the swapper
 *       handles by force-equipping a weapon plus a shield in the
 *       off-hand.</li>
 *   <li>Status effects, health, hunger, fire, drowning and fall
 *       distance also feed dedicated activities so the swapper can
 *       react with potions, food or elytra automatically.</li>
 * </ul>
 */
public class ActivityDetector {

    /** ms after a manual scroll during which auto-apply is suppressed. */
    private static final long MANUAL_OVERRIDE_MS = 4500;

    /**
     * ms after the player explicitly sets an activity (G/V/forceSetActivity)
     * during which auto-detection is fully suppressed. Without this, the
     * detector would immediately switch back to whatever the held item
     * scores highest for, making G feel broken.
     */
    private static final long FORCE_OVERRIDE_MS = 12_000;

    /** Cooldown between two emergency-combat triggers. */
    private static final long EMERGENCY_COOLDOWN_MS = 2500;

    /** Scan radius for nearby workstations (blocks). */
    private static final int WORKSTATION_RADIUS = 4;

    /** Spawn-egg entity bases (upper-case) that should bias toward farming / breeding. */
    private static final Set<String> FRIENDLY_SPAWN_EGG_MOBS = Set.of(
            "COW", "MOOSHROOM", "PIG", "SHEEP", "CHICKEN", "RABBIT", "HORSE",
            "LLAMA", "TRADER_LLAMA", "CAMEL", "DONKEY", "MULE", "CAT", "OCELOT",
            "WOLF", "PARROT", "FOX", "PANDA", "AXOLOTL", "FROG", "BEE",
            "VILLAGER", "WANDERING_TRADER", "GOAT", "SNIFFER", "TURTLE",
            "STRIDER", "SQUID", "GLOW_SQUID", "DOLPHIN");

    private final EnumMap<ActivityType, Integer> activityWeights = new EnumMap<>(ActivityType.class);

    private ActivityType currentActivity = ActivityType.GENERAL;
    private ActivityType pendingActivity = ActivityType.GENERAL;
    private int pendingTicks = 0;

    private long lastSwitchTime = 0;
    private long lastHurtTime = 0;
    private long lastTickTime = 0;
    private int idleTicks = 0;

    private int lastObservedSelectedSlot = -1;
    private long lastManualScrollTime = 0;
    private long lastEmergencyTime = 0;
    private long lastForceSetTime = 0;

    private double lastX, lastY, lastZ;

    public ActivityType getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Forcibly set the activity (used by manual G/V keybinds and the API).
     * Always pushes the change to the server so the hotbar gets re-arranged
     * AND the held slot is updated.
     */
    public void forceSetActivity(ActivityType activity) {
        if (activity == null) activity = ActivityType.GENERAL;

        currentActivity = activity;
        pendingActivity = activity;
        pendingTicks = 0;
        long now = System.currentTimeMillis();
        lastSwitchTime = now;

        // Cancel any active scroll-override (the player explicitly chose),
        // and start a force-override window so the auto-detector can't
        // switch us back the next tick because of what's in the hand.
        lastManualScrollTime = 0;
        lastForceSetTime = now;

        FlowInventoryMod.LOGGER.info(
                "[FlowInventory] Activity manually set to: {} (auto-detect locked for {}ms)",
                activity.displayName,
                FORCE_OVERRIDE_MS
        );

        if (FlowInventoryMod.config.autoApplyProfile) {
            NetworkHandler.sendActivityChange(activity);
        }
    }

    /** True if the player just used G/V and we're inside the lock window. */
    public boolean isInForceOverride() {
        return (System.currentTimeMillis() - lastForceSetTime) < FORCE_OVERRIDE_MS;
    }

    /** Remaining seconds in the current force-override window (0 if none). */
    public int getForceOverrideRemainingSeconds() {
        long remaining = FORCE_OVERRIDE_MS - (System.currentTimeMillis() - lastForceSetTime);
        return remaining > 0 ? (int) Math.ceil(remaining / 1000.0) : 0;
    }

    public void tick(PlayerEntity player) {
        if (player == null) return;
        if (!FlowInventoryMod.config.autoDetectActivity) return;

        long now = System.currentTimeMillis();
        if (now - lastTickTime < 250) return; // throttle to ~4 Hz
        lastTickTime = now;

        updateScrollTracking(player, now);
        updateIdleCounter(player);

        ItemStack held = player.getMainHandStack();
        ItemStack offhand = player.getOffHandStack();

        activityWeights.clear();
        for (ActivityType t : ActivityType.values()) activityWeights.put(t, 0);

        // 1. Held item is the strongest item-level signal (weight ×4)
        scoreFromItem(held, 4);

        // 2. Off-hand item gets a smaller bump (weight ×2)
        scoreFromItem(offhand, 2);

        // 3. Armor signals (each piece weight ×1)
        for (ItemStack armor : player.getArmorItems()) {
            scoreFromItem(armor, 1);
        }

        // 4. Nearby mobs
        scoreFromNearbyMobs(player);

        // 5. Dimension context
        scoreFromDimension(player);

        // 6. Biome / environment heuristics
        scoreFromEnvironment(player);

        // 7. Idle / sleeping / riding context
        scoreFromPlayerState(player);

        // 8. Health, hunger, status effects, hazards
        scoreFromVitals(player);

        // 9. Nearby workstations & PvP players
        scoreFromBlocksAndPlayers(player);

        // 10. Boost the current activity slightly to reduce flapping
        if (currentActivity != ActivityType.GENERAL && currentActivity != ActivityType.UNKNOWN) {
            activityWeights.merge(currentActivity, 8, Integer::sum);
        }

        // 11. Emergency check FIRST — bypasses every other rule, including
        //     the force-override window (an emergency literally cannot wait).
        if (shouldTriggerEmergencyCombat(player)
                && now - lastEmergencyTime > EMERGENCY_COOLDOWN_MS) {
            triggerEmergency(player, now);
            return; // skip the normal flow this tick
        }

        // 11½. Inside the force-override window (just pressed G/V) we suppress
        //      all auto-detection so the player's explicit choice actually
        //      sticks. Emergencies above already bypassed this; everything
        //      else waits.
        if (now - lastForceSetTime < FORCE_OVERRIDE_MS) {
            return;
        }

        // 11b. Soft tool emergency — if the held tool is about to break and
        //      the inventory has a fresher one of the same family, bias the
        //      detector toward the matching activity so the swapper picks
        //      the spare tool. We only nudge here, we don't force-switch.
        nudgeToolDurabilityEmergency(player, held);

        // 12. Pick the winner above the confidence threshold
        Map.Entry<ActivityType, Integer> winner = activityWeights.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);

        ActivityType detected = (winner != null && winner.getValue() >= scaledThreshold())
                ? winner.getKey()
                : ActivityType.GENERAL;

        // 12b. If the player has nothing matching this activity, don't bother
        //      switching — degrade to GENERAL to avoid the "ghost activity"
        //      glitch where the HUD claims something the inventory can't back.
        //      But: never override an activity whose primary signal is the
        //      held or off-hand item itself (otherwise holding an oak log
        //      would fall back to GENERAL just because the player has no axe).
        if (detected != ActivityType.GENERAL
                && !InventoryScanner.canUseActivity(player, detected)
                && !heldOrOffhandFitsActivity(player, detected)) {
            detected = ActivityType.GENERAL;
        }

        // 13. Debounce — require N consecutive ticks before switching
        if (detected == pendingActivity) {
            pendingTicks++;
        } else {
            pendingActivity = detected;
            pendingTicks = 1;
        }

        int debounce = Math.max(1, FlowInventoryMod.config.activitySwitchDelay / 8);
        if (pendingTicks >= debounce && detected != currentActivity) {
            ActivityType previous = currentActivity;
            currentActivity = detected;
            lastSwitchTime = now;

            FlowInventoryMod.LOGGER.info(
                    "[FlowInventory] Activity changed: {} -> {} (score {})",
                    previous.displayName,
                    currentActivity.displayName,
                    winner != null ? winner.getValue() : 0
            );

            // Suppress auto-apply if the player just scrolled — they
            // explicitly chose what to hold, so don't yank their slot.
            boolean inManualOverride = (now - lastManualScrollTime) < MANUAL_OVERRIDE_MS;

            if (FlowInventoryMod.config.autoApplyProfile && !inManualOverride) {
                NetworkHandler.sendActivityChange(currentActivity);
            }
        }
    }

    // ==================== EMERGENCY COMBAT ====================

    private boolean shouldTriggerEmergencyCombat(PlayerEntity player) {
        ItemStack held = player.getMainHandStack();
        if (isCombatReady(held)) return false;

        World world = player.getWorld();
        if (world == null) return false;

        double range = Math.max(5.0, FlowInventoryMod.config.combatDetectionRange);
        Box box = player.getBoundingBox().expand(range);
        List<HostileEntity> hostiles = world.getEntitiesByClass(
                HostileEntity.class,
                box,
                e -> e != null && e.isAlive() && e.canTarget(player)
        );

        if (hostiles.isEmpty()) return false;

        // Closest hostile within ~6 blocks → emergency
        double closestSq = Double.MAX_VALUE;
        for (HostileEntity e : hostiles) {
            double d = e.squaredDistanceTo(player);
            if (d < closestSq) closestSq = d;
        }
        if (closestSq > 36.0) return false;

        // Player must actually own a weapon somewhere
        return findInventoryWeaponSlot(player) >= 0;
    }

    private void triggerEmergency(PlayerEntity player, long now) {
        lastEmergencyTime = now;
        currentActivity = ActivityType.EMERGENCY_COMBAT;
        pendingActivity = ActivityType.EMERGENCY_COMBAT;
        pendingTicks = 0;
        lastSwitchTime = now;

        // Cancel manual override — emergencies always force a swap
        lastManualScrollTime = 0;

        FlowInventoryMod.LOGGER.warn(
                "[FlowInventory] Emergency: hostile mob close & no weapon held \u2192 forcing EMERGENCY_COMBAT"
        );

        if (FlowInventoryMod.config.autoApplyProfile) {
            NetworkHandler.sendActivityChange(ActivityType.EMERGENCY_COMBAT);
        }
    }

    private static boolean isCombatReady(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        return path.contains("sword") || path.contains("trident") || path.contains("mace")
                || path.equals("bow") || path.equals("crossbow")
                || (path.contains("axe") && !path.contains("pickaxe"));
    }

    private static int findInventoryWeaponSlot(PlayerEntity player) {
        for (int i = 0; i < 36; i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (isCombatReady(s)) return i;
        }
        return -1;
    }

    /**
     * True if the player's held or off-hand item is, by itself, a useful
     * input for the activity (matches at least one of its preset slots).
     * Used to avoid the GENERAL fall-back when the player explicitly chose
     * to hold something relevant.
     */
    private static boolean heldOrOffhandFitsActivity(PlayerEntity player, ActivityType activity) {
        com.flowinventory.profiles.HotbarPreset preset =
                com.flowinventory.profiles.ProfileManager.getPreset(activity);
        if (preset == null || preset.slots == null) return false;

        ItemStack held = player.getMainHandStack();
        ItemStack offhand = player.getOffHandStack();
        if ((held == null || held.isEmpty()) && (offhand == null || offhand.isEmpty())) return false;

        for (String type : preset.slots.values()) {
            if (type == null) continue;
            if (held != null && !held.isEmpty()
                    && com.flowinventory.server.ItemHeuristics.evaluate(held, type) > 0) return true;
            if (offhand != null && !offhand.isEmpty()
                    && com.flowinventory.server.ItemHeuristics.evaluate(offhand, type) > 0) return true;
        }
        return false;
    }

    // ==================== TOOL DURABILITY ====================

    /**
     * Nudges the activity score so the swapper picks a fresh tool when the
     * held one is about to break. Doesn't force a switch on its own.
     */
    private void nudgeToolDurabilityEmergency(PlayerEntity player, ItemStack held) {
        if (held == null || held.isEmpty() || !held.isDamageable()) return;
        int remaining = held.getMaxDamage() - held.getDamage();
        if (remaining > 5) return;
        if (InventoryScanner.findFresherTool(player, held) < 0) return;

        String path = Registries.ITEM.getId(held.getItem()).getPath();
        if (path.contains("pickaxe")) {
            bump(ActivityType.MINING, 60);
        } else if (path.contains("axe") && !path.contains("pickaxe")) {
            bump(ActivityType.WOODWORKING, 60);
        } else if (path.contains("shovel")) {
            bump(ActivityType.DIRT, 60);
        } else if (path.contains("hoe")) {
            bump(ActivityType.FARMING, 60);
        } else if (path.contains("sword")) {
            bump(ActivityType.SWORD_COMBAT, 60);
        } else if (path.contains("bow") && !path.contains("crossbow")) {
            bump(ActivityType.ARCHERY, 60);
        } else if (path.contains("crossbow")) {
            bump(ActivityType.CROSSBOW_COMBAT, 60);
        } else if (path.contains("trident")) {
            bump(ActivityType.TRIDENT_COMBAT, 60);
        } else if (path.contains("fishing_rod")) {
            bump(ActivityType.FISHING, 60);
        } else if (path.contains("shears")) {
            bump(ActivityType.SHEEP_FARMING, 60);
        }
    }

    // ==================== SCROLL TRACKING ====================

    private void updateScrollTracking(PlayerEntity player, long now) {
        int slot = player.getInventory().selectedSlot;
        if (lastObservedSelectedSlot != -1 && slot != lastObservedSelectedSlot) {
            lastManualScrollTime = now;
        }
        lastObservedSelectedSlot = slot;
    }

    private int scaledThreshold() {
        return Math.max(15, FlowInventoryMod.config.activityConfidenceThreshold / 3);
    }

    private void updateIdleCounter(PlayerEntity player) {
        double dx = player.getX() - lastX;
        double dy = player.getY() - lastY;
        double dz = player.getZ() - lastZ;
        double moved = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (moved < 0.05) {
            idleTicks++;
        } else {
            idleTicks = 0;
        }

        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();

        if (player.hurtTime > 0) {
            lastHurtTime = System.currentTimeMillis();
        }
    }

    // ==================== ITEM SCORING ====================

    /**
     * True if {@code path} contains {@code word} as a separate underscore-
     * delimited token, OR is exactly {@code word}. This avoids classic
     * substring traps:
     * <ul>
     *   <li>{@code hasWord("cooked_beef", "BEE")} → false (it's part of "BEEF")</li>
     *   <li>{@code hasWord("waxed_copper_block", "AXE")} → false</li>
     *   <li>{@code hasWord("bowl", "BOW")} → false</li>
     *   <li>{@code hasWord("wooden_axe", "AXE")} → true</li>
     * </ul>
     * Comparison is case-insensitive (caller passes upper-case).
     */
    private static boolean hasWord(String upperPath, String word) {
        if (upperPath.equals(word)) return true;
        if (upperPath.startsWith(word + "_")) return true;
        if (upperPath.endsWith("_" + word)) return true;
        return upperPath.contains("_" + word + "_");
    }

    private void scoreFromItem(ItemStack stack, int weight) {
        if (stack == null || stack.isEmpty()) return;

        Item item = stack.getItem();
        String path = Registries.ITEM.getId(item).getPath();
        String upper = path.toUpperCase();

        // ── Weapons (use word-boundary checks to avoid AXE-in-WAXED etc.) ──
        if (hasWord(upper, "SWORD") || upper.endsWith("_SWORD")) {
            bump(ActivityType.SWORD_COMBAT, 22 * weight);
            bump(ActivityType.COMBAT, 6 * weight);
        }
        boolean isAxe = upper.endsWith("_AXE") && !upper.endsWith("_PICKAXE");
        if (isAxe) {
            bump(ActivityType.AXE_COMBAT, 14 * weight);
            bump(ActivityType.WOODWORKING, 10 * weight);
            bump(ActivityType.TREE_FARMING, 9 * weight);
        }
        if (hasWord(upper, "TRIDENT")) bump(ActivityType.TRIDENT_COMBAT, 25 * weight);
        if (hasWord(upper, "MACE") || hasWord(upper, "HEAVY_CORE")) bump(ActivityType.MACE, 25 * weight);
        // BOW = exact "bow" or anything ending "_bow" but never "_crossbow"
        boolean isBow = (upper.equals("BOW") || (upper.endsWith("_BOW") && !upper.endsWith("_CROSSBOW")));
        if (isBow) {
            bump(ActivityType.ARCHERY, 22 * weight);
            bump(ActivityType.SNIPER, 8 * weight);
        }
        if (upper.equals("CROSSBOW") || upper.endsWith("_CROSSBOW")) {
            bump(ActivityType.CROSSBOW_COMBAT, 22 * weight);
            bump(ActivityType.RAID, 6 * weight);
        }
        if (upper.equals("ARROW") || hasWord(upper, "ARROW")
                || upper.equals("TIPPED_ARROW") || upper.equals("SPECTRAL_ARROW")) {
            bump(ActivityType.ARCHERY, 6 * weight);
        }
        if (hasWord(upper, "SHIELD")) {
            bump(ActivityType.DEFENSIVE, 14 * weight);
            bump(ActivityType.COMBAT, 6 * weight);
        }
        if (upper.equals("TNT") || upper.contains("TNT_MINECART") || upper.contains("FIRE_CHARGE")) {
            bump(ActivityType.EXPLOSIVES, 22 * weight);
        }
        if (upper.equals("WIND_CHARGE") || upper.contains("BREEZE")) {
            bump(ActivityType.BREEZE, 18 * weight);
        }
        if (upper.contains("SPLASH_POTION")) bump(ActivityType.POTION_COMBAT, 16 * weight);
        if (upper.contains("LINGERING_POTION")) bump(ActivityType.POTION_COMBAT, 18 * weight);
        if (upper.equals("POTION")) bump(ActivityType.HEALING, 10 * weight);
        if (upper.equals("TOTEM_OF_UNDYING")) bump(ActivityType.DEFENSIVE, 18 * weight);

        if (upper.endsWith("_PICKAXE")) {
            bump(ActivityType.MINING, 22 * weight);
            bump(ActivityType.STONEMASONRY, 6 * weight);
        }
        if (upper.endsWith("_SHOVEL")) {
            bump(ActivityType.DIRT, 14 * weight);
            bump(ActivityType.SAND, 12 * weight);
            bump(ActivityType.GRAVEL, 10 * weight);
        }
        if (upper.endsWith("_HOE")) {
            bump(ActivityType.FARMING, 18 * weight);
            bump(ActivityType.CROP_FARMING, 14 * weight);
        }
        if (upper.equals("SHEARS")) {
            bump(ActivityType.SHEEP_FARMING, 14 * weight);
            bump(ActivityType.FARMING, 6 * weight);
        }
        if (upper.equals("FISHING_ROD") || upper.endsWith("_FISHING_ROD")) {
            bump(ActivityType.FISHING, 26 * weight);
            bump(ActivityType.OCEAN_FISHING, 8 * weight);
        }
        if (upper.equals("CARROT_ON_A_STICK")) {
            bump(ActivityType.PIG_RIDING, 18 * weight);
        }
        if (upper.equals("WARPED_FUNGUS_ON_A_STICK")) {
            bump(ActivityType.STRIDER_RIDING, 18 * weight);
        }
        if (upper.equals("FLINT_AND_STEEL")) {
            bump(ActivityType.LIGHTING, 12 * weight);
            bump(ActivityType.NETHER, 6 * weight);
        }

        if (item instanceof net.minecraft.item.BlockItem) {
            bump(ActivityType.BUILDING, 8 * weight);
            if (upper.contains("PLANK") || upper.contains("LOG") || upper.contains("WOOD")
                    || upper.endsWith("_STEM") || upper.contains("HYPHAE")) {
                bump(ActivityType.WOODWORKING, 14 * weight);
            }
            if (upper.contains("STONE") || upper.contains("BRICK") || upper.contains("COBBLESTONE")
                    || upper.contains("DEEPSLATE") || upper.contains("BLACKSTONE")
                    || upper.contains("ANDESITE") || upper.contains("DIORITE") || upper.contains("GRANITE")
                    || upper.contains("TUFF") || upper.contains("CALCITE") || upper.contains("BASALT")) {
                bump(ActivityType.STONEMASONRY, 12 * weight);
            }
            if (upper.contains("TERRACOTTA")) bump(ActivityType.TERRACOTTA, 14 * weight);
            if (upper.contains("CONCRETE")) bump(ActivityType.CONCRETE, 14 * weight);
            if (upper.contains("GLASS")) bump(ActivityType.GLASSWORK, 14 * weight);
            if (upper.contains("WOOL") || upper.contains("CARPET")) bump(ActivityType.DECORATING, 12 * weight);
            if (upper.contains("SCULK")) bump(ActivityType.SCULKING, 14 * weight);
            if (upper.contains("BANNER")) bump(ActivityType.DECORATION_BANNER, 14 * weight);
            if (upper.contains("PAINTING") || upper.contains("ITEM_FRAME")) {
                bump(ActivityType.DECORATION_PAINTER, 14 * weight);
            }
            if (upper.contains("LEAVES") || upper.contains("SAPLING") || upper.contains("FLOWER")
                    || upper.contains("ROSE") || upper.contains("TULIP") || upper.contains("DAISY")
                    || upper.contains("ORCHID") || upper.contains("DANDELION") || upper.contains("POPPY")
                    || upper.contains("LILAC") || upper.contains("PEONY") || upper.contains("LILY")
                    || upper.contains("SUNFLOWER") || upper.contains("BLUET") || upper.contains("AZURE")) {
                bump(ActivityType.LANDSCAPING, 14 * weight);
                bump(ActivityType.DECORATING, 6 * weight);
            }
            if (upper.contains("SAPLING") || upper.contains("PROPAGULE")) {
                bump(ActivityType.TREE_FARMING, 14 * weight);
            }
            if (upper.contains("PUMPKIN") || upper.contains("MELON") || upper.contains("HAY")) {
                bump(ActivityType.CROP_FARMING, 8 * weight);
            }
            if (upper.contains("STAIRS") || upper.contains("SLAB") || upper.contains("WALL")
                    || upper.contains("FENCE") || upper.contains("DOOR") || upper.contains("TRAPDOOR")) {
                bump(ActivityType.BUILDING, 8 * weight);
                bump(ActivityType.ROOFING, 6 * weight);
            }
            if (upper.contains("CANDLE")) {
                bump(ActivityType.LIGHTING, 10 * weight);
                bump(ActivityType.DECORATING, 6 * weight);
            }
        }

        if (upper.equals("WHEAT") || upper.equals("WHEAT_SEEDS")) bump(ActivityType.CROP_FARMING, 14 * weight);
        if (upper.equals("CARROT") || upper.equals("POTATO") || upper.equals("BEETROOT")
                || upper.equals("BEETROOT_SEEDS") || upper.equals("PUMPKIN_SEEDS")
                || upper.equals("MELON_SEEDS") || upper.equals("TORCHFLOWER_SEEDS")
                || upper.equals("PITCHER_POD")) {
            bump(ActivityType.CROP_FARMING, 12 * weight);
        }
        if (upper.equals("SWEET_BERRIES") || upper.equals("GLOW_BERRIES")) {
            bump(ActivityType.CROP_FARMING, 12 * weight);
            bump(ActivityType.FOOD, 4 * weight);
        }
        if (upper.equals("SUGAR_CANE")) bump(ActivityType.SUGAR_CANE_FARMING, 14 * weight);
        if (upper.equals("BAMBOO")) bump(ActivityType.BAMBOO_FARMING, 14 * weight);
        if (upper.equals("KELP")) bump(ActivityType.KELP_FARMING, 14 * weight);
        if (upper.equals("RED_MUSHROOM") || upper.equals("BROWN_MUSHROOM")
                || upper.equals("CRIMSON_FUNGUS") || upper.equals("WARPED_FUNGUS")
                || upper.equals("RED_MUSHROOM_BLOCK") || upper.equals("BROWN_MUSHROOM_BLOCK")
                || upper.equals("MUSHROOM_STEM")) {
            bump(ActivityType.MUSHROOM_FARMING, 12 * weight);
        }
        if (upper.equals("BONE_MEAL")) bump(ActivityType.FARMING, 8 * weight);
        // Bee items — explicit list to avoid BEEF / BEETROOT / BEEHIVE collisions
        if (upper.equals("BEEHIVE") || upper.equals("BEE_NEST") || upper.equals("BEE_SPAWN_EGG")
                || upper.equals("HONEY_BOTTLE") || upper.equals("HONEY_BLOCK")
                || upper.equals("HONEYCOMB") || upper.equals("HONEYCOMB_BLOCK")) {
            bump(ActivityType.BEE_FARMING, 14 * weight);
        }
        if (upper.endsWith("_SAPLING") || upper.endsWith("_PROPAGULE")) {
            bump(ActivityType.TREE_FARMING, 12 * weight);
        }

        if (item.getFoodComponent() != null) {
            bump(ActivityType.FOOD, 6 * weight);
            bump(ActivityType.NUTRITION, 4 * weight);
        }
        if (upper.contains("GOLDEN_APPLE") || upper.contains("ENCHANTED_GOLDEN_APPLE")) {
            bump(ActivityType.HEALING, 10 * weight);
        }
        if (upper.equals("MILK_BUCKET") || upper.contains("HONEY_BOTTLE")) {
            bump(ActivityType.HEALING, 8 * weight);
        }

        if (upper.equals("BLAZE_POWDER") || upper.equals("BLAZE_ROD")) {
            bump(ActivityType.BREWING, 14 * weight);
            bump(ActivityType.NETHER, 4 * weight);
        }
        if (upper.equals("NETHER_WART")) {
            bump(ActivityType.BREWING, 14 * weight);
            bump(ActivityType.NETHER_FARMING, 10 * weight);
        }
        if (upper.equals("EXPERIENCE_BOTTLE") || upper.contains("ENCHANTED_BOOK")
                || upper.equals("LAPIS_LAZULI")) {
            bump(ActivityType.ENCHANTING, 14 * weight);
        }
        if (upper.equals("BOOK") || upper.equals("WRITABLE_BOOK") || upper.equals("WRITTEN_BOOK")) {
            bump(ActivityType.WRITING, 12 * weight);
        }

        if (upper.contains("ELYTRA")) bump(ActivityType.ELYTRA, 28 * weight);
        if (upper.contains("FIREWORK_ROCKET")) bump(ActivityType.ELYTRA, 8 * weight);
        if (upper.contains("BOAT") || upper.contains("RAFT")) bump(ActivityType.BOAT, 18 * weight);
        if (upper.contains("MINECART")) bump(ActivityType.MINECART, 18 * weight);
        if (upper.contains("RAIL")) bump(ActivityType.RAILS, 14 * weight);
        if (upper.equals("SADDLE")) {
            bump(ActivityType.RIDING, 18 * weight);
            bump(ActivityType.HORSE_RIDING, 8 * weight);
        }
        if (upper.contains("HORSE_ARMOR")) bump(ActivityType.HORSE_RIDING, 14 * weight);
        if (upper.equals("LEAD")) bump(ActivityType.ANIMAL_FARMING, 10 * weight);
        if (upper.equals("NAME_TAG")) bump(ActivityType.UTILITY, 10 * weight);
        if (upper.equals("MAP") || upper.equals("FILLED_MAP")) bump(ActivityType.MAP, 18 * weight);
        if (upper.contains("COMPASS")) bump(ActivityType.COMPASS, 14 * weight);
        if (upper.equals("CLOCK")) bump(ActivityType.CLOCK, 8 * weight);
        if (upper.equals("SPYGLASS")) bump(ActivityType.SPYGLASS, 14 * weight);
        if (upper.equals("ENDER_PEARL") || upper.equals("ENDER_EYE") || upper.equals("EYE_OF_ENDER")) {
            bump(ActivityType.TELEPORT, 18 * weight);
            bump(ActivityType.END_EXPLORE, 6 * weight);
        }
        if (upper.equals("CHORUS_FRUIT")) bump(ActivityType.TELEPORT, 14 * weight);

        if (upper.equals("TORCH") || upper.equals("SOUL_TORCH")
                || upper.endsWith("_LANTERN") || upper.equals("LANTERN")
                || upper.endsWith("_CANDLE") || upper.equals("CANDLE")
                || upper.equals("GLOWSTONE") || upper.equals("SHROOMLIGHT")) {
            bump(ActivityType.LIGHTING, 8 * weight);
            bump(ActivityType.CAVING, 4 * weight);
        }

        if (upper.contains("REDSTONE")) bump(ActivityType.REDSTONE, 12 * weight);
        if (upper.equals("REPEATER") || upper.equals("COMPARATOR")) bump(ActivityType.REDSTONE_LOGIC, 14 * weight);
        if (upper.equals("OBSERVER")) bump(ActivityType.OBSERVER, 14 * weight);
        if (upper.equals("PISTON") || upper.equals("STICKY_PISTON")) bump(ActivityType.PISTON, 14 * weight);
        if (upper.equals("HOPPER") || upper.equals("HOPPER_MINECART")) bump(ActivityType.HOPPER, 14 * weight);
        if (upper.equals("DROPPER")) bump(ActivityType.DROPPER, 14 * weight);
        if (upper.equals("DISPENSER")) bump(ActivityType.DISPENSER, 14 * weight);
        if (upper.equals("LEVER") || upper.contains("BUTTON") || upper.contains("PRESSURE_PLATE")) {
            bump(ActivityType.REDSTONE_LOGIC, 6 * weight);
        }

        if (upper.equals("WATER_BUCKET")) bump(ActivityType.BUCKET_USE, 8 * weight);
        if (upper.equals("LAVA_BUCKET")) bump(ActivityType.BUCKET_USE, 8 * weight);

        if (upper.contains("NETHERITE")) bump(ActivityType.NETHER_RESOURCES, 6 * weight);
        if (upper.equals("ANCIENT_DEBRIS") || upper.equals("NETHERITE_SCRAP")) {
            bump(ActivityType.ANCIENT_DEBRIS, 24 * weight);
        }
        if (upper.equals("NETHERRACK")) bump(ActivityType.NETHERRACK, 12 * weight);
        if (upper.contains("END_STONE") || upper.contains("PURPUR") || upper.contains("CHORUS")) {
            bump(ActivityType.END_STONE, 12 * weight);
        }

        // ── Resources / smithing / crafting signals ─────────────────────
        if (upper.endsWith("_INGOT") || upper.endsWith("_NUGGET")) {
            bump(ActivityType.SMITHING, 10 * weight);
            bump(ActivityType.SMELTING, 6 * weight);
        }
        if (upper.equals("LAPIS_LAZULI")) {
            bump(ActivityType.ENCHANTING, 16 * weight);
        }
        if (upper.equals("EMERALD")) {
            bump(ActivityType.TRADING, 14 * weight);
        }
        if (upper.equals("DIAMOND") || upper.equals("NETHERITE_INGOT")
                || upper.equals("SMITHING_TEMPLATE") || upper.endsWith("_TEMPLATE")) {
            bump(ActivityType.SMITHING, 14 * weight);
        }
        if (upper.equals("CRAFTING_TABLE") || upper.equals("FURNACE") || upper.equals("SMOKER")
                || upper.equals("BLAST_FURNACE") || upper.equals("SMITHING_TABLE")
                || upper.equals("ANVIL") || upper.equals("CHIPPED_ANVIL") || upper.equals("DAMAGED_ANVIL")
                || upper.equals("STONECUTTER") || upper.equals("LOOM") || upper.equals("CARTOGRAPHY_TABLE")
                || upper.equals("GRINDSTONE") || upper.equals("COMPOSTER")
                || upper.equals("ENCHANTING_TABLE") || upper.equals("BREWING_STAND")) {
            bump(ActivityType.UTILITY, 10 * weight);
        }
        if (upper.equals("ANVIL")) bump(ActivityType.ANVIL, 18 * weight);

        // ── Raw food signals (player wants to cook) ─────────────────────
        // Use exact equals to avoid CHICKEN_SPAWN_EGG / COD_BUCKET false-positives.
        boolean isRawMeat = upper.startsWith("RAW_")
                || upper.equals("BEEF") || upper.equals("PORKCHOP") || upper.equals("MUTTON")
                || upper.equals("CHICKEN") || upper.equals("RABBIT") || upper.equals("COD")
                || upper.equals("SALMON") || upper.equals("TROPICAL_FISH") || upper.equals("PUFFERFISH");
        if (isRawMeat) {
            bump(ActivityType.COOKING, 10 * weight);
            bump(ActivityType.SMELTING, 6 * weight);
            bump(ActivityType.FOOD, 4 * weight);
        }
        if (upper.equals("BUCKET") || upper.equals("WATER_BUCKET") || upper.equals("LAVA_BUCKET")
                || upper.equals("MILK_BUCKET") || upper.equals("POWDER_SNOW_BUCKET")) {
            bump(ActivityType.BUCKET_USE, 8 * weight);
        }

        // ── Light / fire ────────────────────────────────────────────────
        if (upper.equals("FIRE_CHARGE")) bump(ActivityType.LIGHTING, 10 * weight);

        // ── Misc dyes / decoration ──────────────────────────────────────
        if (upper.endsWith("_DYE") || upper.equals("INK_SAC") || upper.equals("GLOW_INK_SAC")
                || upper.equals("BONE_MEAL")) {
            bump(ActivityType.DECORATING, 6 * weight);
        }
        if (upper.contains("PAINTING") || upper.contains("ITEM_FRAME")) {
            bump(ActivityType.DECORATING, 12 * weight);
        }

        // ── Throwables & utility ────────────────────────────────────────
        if (upper.equals("SNOWBALL") || upper.equals("EGG")) {
            bump(ActivityType.UTILITY, 4 * weight);
        }
        if (upper.equals("ENDER_PEARL") || upper.equals("ENDER_EYE") || upper.equals("EYE_OF_ENDER")) {
            // Already bumped TELEPORT/END_EXPLORE above; nothing more here.
        }
        if (upper.equals("SLIME_BALL")) {
            bump(ActivityType.REDSTONE, 8 * weight);
            bump(ActivityType.UTILITY, 4 * weight);
        }
        if (upper.equals("PHANTOM_MEMBRANE")) {
            bump(ActivityType.ELYTRA, 8 * weight);
            bump(ActivityType.UTILITY, 4 * weight);
        }
        if (upper.equals("SPONGE") || upper.equals("WET_SPONGE")) {
            bump(ActivityType.DIVING, 16 * weight);
            bump(ActivityType.OCEAN, 10 * weight);
            bump(ActivityType.MONUMENT, 6 * weight);
        }
        if (upper.equals("ECHO_SHARD")) {
            bump(ActivityType.COMPASS, 14 * weight);
            bump(ActivityType.DEEP_DARK, 8 * weight);
            bump(ActivityType.ANCIENT_CITY, 6 * weight);
        }
        if (upper.equals("RECOVERY_COMPASS")) {
            bump(ActivityType.COMPASS, 22 * weight);
        }
        if (upper.equals("GOAT_HORN")) {
            bump(ActivityType.UTILITY, 14 * weight);
        }
        if (upper.startsWith("MUSIC_DISC_")) {
            bump(ActivityType.IDLE, 8 * weight);
            bump(ActivityType.UTILITY, 6 * weight);
        }
        if (upper.equals("JUKEBOX") || upper.equals("NOTE_BLOCK")) {
            bump(ActivityType.UTILITY, 8 * weight);
        }
        if (upper.equals("SNIFFER_EGG") || upper.equals("TURTLE_EGG")) {
            bump(ActivityType.ANIMAL_FARMING, 12 * weight);
            bump(ActivityType.BREEDING, 6 * weight);
        }
        if (upper.equals("OCHRE_FROGLIGHT") || upper.equals("VERDANT_FROGLIGHT")
                || upper.equals("PEARLESCENT_FROGLIGHT")) {
            bump(ActivityType.LIGHTING, 14 * weight);
            bump(ActivityType.DECORATING, 10 * weight);
        }
        if (upper.equals("AMETHYST_SHARD") || upper.equals("AMETHYST_BLOCK")
                || upper.equals("BUDDING_AMETHYST")) {
            bump(ActivityType.AMETHYST_MINING, 16 * weight);
            bump(ActivityType.DECORATING, 6 * weight);
        }
        if (upper.equals("GLOW_LICHEN") || upper.equals("SHROOMLIGHT")) {
            bump(ActivityType.LIGHTING, 12 * weight);
            bump(ActivityType.DECORATING, 8 * weight);
        }
        if (upper.equals("WITHER_ROSE")) {
            bump(ActivityType.LANDSCAPING, 16 * weight);
            bump(ActivityType.WITHERING, 6 * weight);
        }
        if (upper.equals("DRAGON_HEAD") || upper.equals("ZOMBIE_HEAD")
                || upper.equals("SKELETON_SKULL") || upper.equals("WITHER_SKELETON_SKULL")
                || upper.equals("CREEPER_HEAD") || upper.equals("PIGLIN_HEAD")
                || upper.equals("PLAYER_HEAD")) {
            bump(ActivityType.DECORATING, 12 * weight);
        }
        if (upper.endsWith("_BED")) {
            bump(ActivityType.SLEEPING, 14 * weight);
        }
        if (upper.equals("CONDUIT") || upper.equals("HEART_OF_THE_SEA")
                || upper.equals("NAUTILUS_SHELL")) {
            bump(ActivityType.OCEAN, 18 * weight);
            bump(ActivityType.DIVING, 12 * weight);
            bump(ActivityType.MONUMENT, 6 * weight);
        }
        if (upper.equals("SCUTE") || upper.equals("TURTLE_SCUTE")) {
            bump(ActivityType.OCEAN, 8 * weight);
            bump(ActivityType.ANIMAL_FARMING, 4 * weight);
        }
        if (upper.equals("RABBIT_FOOT")) {
            bump(ActivityType.BREWING, 14 * weight);
        }
        if (upper.equals("DRAGON_BREATH")) {
            bump(ActivityType.ALCHEMY, 16 * weight);
        }
        if (upper.equals("MAGMA_CREAM")) {
            bump(ActivityType.BREWING, 12 * weight);
            bump(ActivityType.NETHER, 4 * weight);
        }
        if (upper.equals("SHULKER_SHELL") || upper.endsWith("_SHULKER_BOX")
                || upper.equals("SHULKER_BOX")) {
            bump(ActivityType.ENDER, 14 * weight);
        }
        if (upper.equals("END_CRYSTAL")) {
            bump(ActivityType.DRAGON, 12 * weight);
            bump(ActivityType.END, 6 * weight);
        }
        if (upper.equals("BUNDLE")) {
            bump(ActivityType.UTILITY, 10 * weight);
        }
        if (upper.endsWith("_SPAWN_EGG")) {
            // Parse entity name from item id (e.g. pig_spawn_egg → PIG) so
            // substring traps like "PIG" matching PIGLIN never happen.
            String base = upper.substring(0, upper.length() - "_SPAWN_EGG".length());
            if (FRIENDLY_SPAWN_EGG_MOBS.contains(base)) {
                bump(ActivityType.ANIMAL_FARMING, 12 * weight);
                bump(ActivityType.BREEDING, 6 * weight);
            } else {
                bump(ActivityType.UTILITY, 4 * weight);
            }
        }
        if (upper.equals("LIGHTNING_ROD")) {
            bump(ActivityType.REDSTONE, 8 * weight);
            bump(ActivityType.DECORATING, 6 * weight);
        }
        if (upper.equals("BRUSH")) {
            bump(ActivityType.EXPLORING, 14 * weight);
        }
        if (upper.endsWith("_POTTERY_SHERD")) {
            bump(ActivityType.DECORATING, 10 * weight);
        }
    }

    // ==================== MOB SCORING ====================

    private void scoreFromNearbyMobs(PlayerEntity player) {
        World world = player.getWorld();
        if (world == null) return;

        double range = Math.max(4.0, FlowInventoryMod.config.combatDetectionRange);
        Box box = player.getBoundingBox().expand(range);
        List<LivingEntity> nearby = world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != null && e != player && e.isAlive()
        );

        boolean anyHostile = false;
        int totalHostiles = 0;
        int totalAnimals = 0;

        for (LivingEntity e : nearby) {
            String type = Registries.ENTITY_TYPE.getId(e.getType()).getPath().toUpperCase();
            boolean hostile = e instanceof HostileEntity;

            if (hostile) {
                anyHostile = true;
                totalHostiles++;
            }
            if (e instanceof AnimalEntity) totalAnimals++;

            switch (type) {
                case "CREEPER" -> bump(ActivityType.CREEPER, 18);
                case "SKELETON", "STRAY" -> bump(ActivityType.SKELETON, 18);
                case "WITHER_SKELETON" -> bump(ActivityType.WITHER_SKELETON, 24);
                case "ZOMBIE", "HUSK" -> bump(ActivityType.ZOMBIE, 16);
                case "DROWNED" -> bump(ActivityType.DROWNED, 18);
                case "SPIDER", "CAVE_SPIDER" -> bump(ActivityType.SPIDER, 16);
                case "ENDERMAN" -> bump(ActivityType.ENDERMAN, 22);
                case "ENDERMITE" -> bump(ActivityType.ENDERMITE, 18);
                case "BLAZE" -> bump(ActivityType.BLAZE, 22);
                case "WITCH" -> bump(ActivityType.WITCH, 20);
                case "GUARDIAN", "ELDER_GUARDIAN" -> bump(ActivityType.GUARDIAN, 24);
                case "SHULKER" -> bump(ActivityType.SHULKER, 24);
                case "WITHER" -> bump(ActivityType.WITHER, 60);
                case "ENDER_DRAGON" -> bump(ActivityType.DRAGON, 80);
                case "WARDEN" -> bump(ActivityType.WARDEN, 70);
                case "PIGLIN" -> bump(ActivityType.PIGLIN, 18);
                case "PIGLIN_BRUTE" -> bump(ActivityType.PIGLIN_BRUTE, 28);
                case "HOGLIN" -> bump(ActivityType.HOGLIN, 18);
                case "ZOGLIN" -> bump(ActivityType.ZOGLIN, 20);
                case "GHAST" -> bump(ActivityType.GHAST, 22);
                case "MAGMA_CUBE" -> bump(ActivityType.MAGMA_CUBE, 14);
                case "SLIME" -> bump(ActivityType.SLIME, 12);
                case "PHANTOM" -> bump(ActivityType.PHANTOM, 18);
                case "RAVAGER" -> bump(ActivityType.RAVAGER, 28);
                case "PILLAGER" -> bump(ActivityType.PILLAGER, 16);
                case "EVOKER" -> bump(ActivityType.EVOKER, 22);
                case "VINDICATOR" -> bump(ActivityType.VINDICATOR, 18);
                case "VEX" -> bump(ActivityType.VEX, 14);
                case "SILVERFISH" -> bump(ActivityType.SILVERFISH, 14);
                case "BREEZE" -> bump(ActivityType.BREEZE, 22);
                case "ZOMBIE_VILLAGER" -> bump(ActivityType.ZOMBIE, 14);
                default -> { /* no-op */ }
            }

            switch (type) {
                case "SHEEP" -> bump(ActivityType.SHEEP_FARMING, 6);
                case "COW", "MOOSHROOM" -> bump(ActivityType.COW_FARMING, 6);
                case "PIG" -> bump(ActivityType.PIG_FARMING, 6);
                case "CHICKEN" -> bump(ActivityType.CHICKEN_FARMING, 6);
                case "BEE" -> bump(ActivityType.BEE_FARMING, 8);
                case "VILLAGER" -> bump(ActivityType.TRADING, 14);
                case "HORSE", "DONKEY", "MULE" -> bump(ActivityType.HORSE_RIDING, 6);
                case "CAMEL" -> bump(ActivityType.CAMEL_RIDING, 8);
                case "LLAMA", "TRADER_LLAMA" -> bump(ActivityType.LLAMA_RIDING, 6);
                case "STRIDER" -> bump(ActivityType.STRIDER_RIDING, 8);
                default -> { /* no-op */ }
            }
        }

        if (anyHostile) bump(ActivityType.COMBAT, 8 + totalHostiles * 2);
        if (totalHostiles >= 4) bump(ActivityType.BATTLE, 18);
        if (totalHostiles >= 8) bump(ActivityType.BERSERK, 24);
        if (totalAnimals >= 3) {
            bump(ActivityType.ANIMAL_FARMING, 8);
            bump(ActivityType.BREEDING, 4);
        }
    }

    // ==================== DIMENSION ====================

    private void scoreFromDimension(PlayerEntity player) {
        World world = player.getWorld();
        if (world == null) return;
        if (!FlowInventoryMod.config.enableDimensionalContext) return;

        RegistryKey<World> dim = world.getRegistryKey();
        if (dim == World.NETHER) {
            bump(ActivityType.NETHER, 8);
            bump(ActivityType.NETHER_EXPLORE, 4);
        } else if (dim == World.END) {
            bump(ActivityType.END, 8);
            bump(ActivityType.END_EXPLORE, 4);
        } else {
            bump(ActivityType.OVERWORLD, 4);
        }
    }

    // ==================== ENVIRONMENT ====================

    private void scoreFromEnvironment(PlayerEntity player) {
        World world = player.getWorld();
        if (world == null) return;

        double y = player.getY();
        if (y < 40) {
            bump(ActivityType.CAVING, 6);
            bump(ActivityType.MINING, 4);
        }
        if (y < 0) {
            bump(ActivityType.DEEPSLATE_MINING, 6);
            bump(ActivityType.DEEP_DARK, 4);
        }
        if (y > 100 && !player.isOnGround()) bump(ActivityType.ELYTRA, 4);

        if (world.getLightLevel(player.getBlockPos()) < 7) bump(ActivityType.LIGHTING, 3);

        long time = world.getTimeOfDay() % 24000;
        if (time > 13000 && time < 23000) bump(ActivityType.COMBAT, 3);

        if (player.isSubmergedInWater()) {
            bump(ActivityType.DIVING, 12);
            bump(ActivityType.OCEAN, 6);
        }
        if (player.isTouchingWater() && !player.isSubmergedInWater()) {
            bump(ActivityType.OCEAN, 4);
        }
    }

    // ==================== PLAYER STATE ====================

    private void scoreFromPlayerState(PlayerEntity player) {
        if (player.isSleeping()) {
            bump(ActivityType.SLEEPING, 60);
            return;
        }

        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            String vt = Registries.ENTITY_TYPE.getId(vehicle.getType()).getPath().toUpperCase();
            switch (vt) {
                case "HORSE", "DONKEY", "MULE" -> bump(ActivityType.HORSE_RIDING, 40);
                case "PIG" -> bump(ActivityType.PIG_RIDING, 40);
                case "STRIDER" -> bump(ActivityType.STRIDER_RIDING, 40);
                case "CAMEL" -> bump(ActivityType.CAMEL_RIDING, 40);
                case "LLAMA" -> bump(ActivityType.LLAMA_RIDING, 40);
                case "BOAT", "CHEST_BOAT" -> bump(ActivityType.BOAT, 40);
                case "MINECART", "CHEST_MINECART", "FURNACE_MINECART", "TNT_MINECART", "HOPPER_MINECART" ->
                        bump(ActivityType.MINECART, 40);
                default -> bump(ActivityType.RIDING, 25);
            }
        }

        if (player.isFallFlying()) bump(ActivityType.ELYTRA, 60);

        if (idleTicks > 200) bump(ActivityType.IDLE, 12);
        if (idleTicks > 1200) bump(ActivityType.AFK, 24);

        if (System.currentTimeMillis() - lastHurtTime < 4000) {
            bump(ActivityType.COMBAT, 14);
            bump(ActivityType.DEFENSIVE, 6);
        }
    }

    // ==================== VITALS / HAZARDS ====================

    private void scoreFromVitals(PlayerEntity player) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float ratio = maxHealth > 0 ? health / maxHealth : 1f;

        if (ratio < 0.30f) {
            bump(ActivityType.LOW_HEALTH, 40);
            bump(ActivityType.HEALING, 30);
            bump(ActivityType.DEFENSIVE, 12);
        } else if (ratio < 0.50f) {
            bump(ActivityType.HEALING, 14);
        }

        int food = player.getHungerManager().getFoodLevel();
        if (food < 6) {
            bump(ActivityType.LOW_HUNGER, 30);
            bump(ActivityType.FOOD, 22);
            bump(ActivityType.NUTRITION, 16);
        } else if (food < 12) {
            bump(ActivityType.FOOD, 6);
        }

        if (player.isOnFire() && !player.isFireImmune()) {
            bump(ActivityType.ON_FIRE, 50);
            bump(ActivityType.HEALING, 18);
            bump(ActivityType.BUCKET_USE, 14);
        }
        if (player.isInLava()) {
            bump(ActivityType.IN_LAVA, 70);
            bump(ActivityType.HEALING, 25);
            bump(ActivityType.BUCKET_USE, 25);
        }

        int air = player.getAir();
        int maxAir = player.getMaxAir();
        if (air >= 0 && air < maxAir / 3) {
            bump(ActivityType.DROWNING, 40);
            bump(ActivityType.DIVING, 20);
            bump(ActivityType.BUCKET_USE, 10);
        }

        if (player.fallDistance > 6f) {
            bump(ActivityType.FALLING, 40);
            bump(ActivityType.ELYTRA, 14);
        }

        if (player.hasStatusEffect(StatusEffects.POISON)) {
            bump(ActivityType.POISONED, 35);
            bump(ActivityType.HEALING, 18);
        }
        if (player.hasStatusEffect(StatusEffects.WITHER)) {
            bump(ActivityType.WITHERING, 50);
            bump(ActivityType.HEALING, 25);
        }
        if (player.hasStatusEffect(StatusEffects.HUNGER)) bump(ActivityType.FOOD, 12);
        if (player.hasStatusEffect(StatusEffects.NIGHT_VISION)) bump(ActivityType.CAVING, 6);
        if (player.hasStatusEffect(StatusEffects.WATER_BREATHING)) bump(ActivityType.DIVING, 10);
        if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) bump(ActivityType.NETHER, 6);
        if (player.hasStatusEffect(StatusEffects.SLOW_FALLING)) bump(ActivityType.ELYTRA, 6);

        if (player.isSneaking()) bump(ActivityType.DEFENSIVE, 4);
        if (player.isSprinting() && !player.isFallFlying()) bump(ActivityType.COMBAT, 3);
    }

    // ==================== NEARBY BLOCKS / PLAYERS ====================

    private void scoreFromBlocksAndPlayers(PlayerEntity player) {
        World world = player.getWorld();
        if (world == null) return;

        BlockPos center = player.getBlockPos();
        for (int dx = -WORKSTATION_RADIUS; dx <= WORKSTATION_RADIUS; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -WORKSTATION_RADIUS; dz <= WORKSTATION_RADIUS; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    String name = Registries.BLOCK.getId(block).getPath();

                    switch (name) {
                        case "crafting_table" -> bump(ActivityType.UTILITY, 8);
                        case "furnace" -> bump(ActivityType.SMELTING, 12);
                        case "blast_furnace" -> bump(ActivityType.SMELTING, 14);
                        case "smoker" -> bump(ActivityType.COOKING, 14);
                        case "anvil", "chipped_anvil", "damaged_anvil" -> bump(ActivityType.ANVIL, 18);
                        case "enchanting_table" -> bump(ActivityType.ENCHANTING, 24);
                        case "brewing_stand" -> bump(ActivityType.BREWING, 24);
                        case "loom" -> bump(ActivityType.LOOM, 18);
                        case "cartography_table" -> bump(ActivityType.CARTOGRAPHY, 18);
                        case "smithing_table" -> bump(ActivityType.SMITHING, 18);
                        case "stonecutter" -> bump(ActivityType.STONECUTTER, 18);
                        case "grindstone" -> bump(ActivityType.GRINDSTONE, 18);
                        case "composter" -> bump(ActivityType.COMPOSTING, 14);
                        case "respawn_anchor" -> bump(ActivityType.NETHER, 8);
                        case "lodestone" -> bump(ActivityType.COMPASS, 10);
                        case "beacon" -> bump(ActivityType.UTILITY, 12);
                        case "bookshelf", "chiseled_bookshelf" -> bump(ActivityType.ENCHANTING, 6);
                        case "jukebox" -> bump(ActivityType.IDLE, 8);
                        case "barrel", "chest", "trapped_chest" -> bump(ActivityType.UTILITY, 4);
                        case "ender_chest" -> bump(ActivityType.ENDER, 10);
                        case "shulker_box" -> bump(ActivityType.ENDER, 8);
                        case "beehive", "bee_nest" -> bump(ActivityType.BEE_FARMING, 14);
                        case "soul_campfire" -> bump(ActivityType.NETHER, 6);
                        case "campfire" -> bump(ActivityType.COOKING, 8);
                        case "spawner" -> bump(ActivityType.COMBAT, 12);
                        case "sculk_shrieker", "sculk_sensor", "sculk_catalyst" -> bump(ActivityType.DEEP_DARK, 18);
                        default -> { /* no-op */ }
                    }
                    if (name.contains("_bed")) bump(ActivityType.SLEEPING, 6);
                }
            }
        }

        // Other players nearby → PvP signal (only count non-self players)
        Box pvpBox = player.getBoundingBox().expand(8.0);
        List<PlayerEntity> others = world.getEntitiesByClass(
                PlayerEntity.class, pvpBox, p -> p != player && p.isAlive()
        );
        if (!others.isEmpty()) bump(ActivityType.PVP, 12 + Math.min(others.size() * 4, 24));
    }

    private void bump(ActivityType type, int amount) {
        activityWeights.merge(type, amount, Integer::sum);
    }

    /** Snapshot of the last activity weights — useful for debugging UI. */
    public List<Map.Entry<ActivityType, Integer>> debugTopActivities(int n) {
        List<Map.Entry<ActivityType, Integer>> list = new ArrayList<>(activityWeights.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(n, list.size()));
    }
}
