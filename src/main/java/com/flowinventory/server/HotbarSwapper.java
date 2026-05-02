package com.flowinventory.server;

import com.flowinventory.profiles.ActivityType;
import com.flowinventory.profiles.HotbarPreset;
import com.flowinventory.profiles.ProfileManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Re-arranges the player's hotbar to match a {@link HotbarPreset} for the
 * currently detected {@link ActivityType}. Always tries to:
 *
 * <ol>
 *   <li>place the activity's primary tool somewhere on the hotbar
 *       (preferring the player's currently selected slot to avoid
 *       yanking what they were using),</li>
 *   <li>fill secondary preset slots with the best matching items,</li>
 *   <li>install the preset's off-hand item (typically a SHIELD), and</li>
 *   <li>switch the held slot to the primary tool ONLY if the slot the
 *       player has selected doesn't already contain a usable item for
 *       this activity.</li>
 * </ol>
 */
public class HotbarSwapper {

    /** Off-hand slot index inside {@link PlayerInventory#main}-relative API. */
    private static final int OFFHAND_SLOT_INDEX = 0; // PlayerInventory#offHand has size 1

    public static void swapHotbar(ServerPlayerEntity player, ActivityType newActivity) {
        if (player == null || newActivity == null) return;

        HotbarPreset preset = ProfileManager.getPreset(newActivity);
        if (preset == null || preset.slots == null || preset.slots.isEmpty()) {
            ensurePrimarySlotSelected(player, newActivity, null);
            return;
        }

        PlayerInventory inventory = player.getInventory();
        int playerSlot = inventory.selectedSlot;
        ItemStack originallyHeld = inventory.getStack(playerSlot).copy();
        String primaryType = preset.slots.get(0);

        // ── Step 1: if the player is holding a usable primary item, lock it
        //            in their current slot so we don't yank it away.
        boolean playerHoldsValidPrimary = primaryType != null
                && !originallyHeld.isEmpty()
                && ItemHeuristics.evaluate(originallyHeld, primaryType) > 0;

        Set<Integer> lockedSlots = new HashSet<>();
        if (playerHoldsValidPrimary) lockedSlots.add(playerSlot);

        // ── Step 2: arrange every preset slot
        for (Map.Entry<Integer, String> entry : preset.slots.entrySet()) {
            int targetSlot = entry.getKey();
            String desiredType = entry.getValue();

            if (lockedSlots.contains(targetSlot)) continue;

            ItemStack currentInTarget = inventory.getStack(targetSlot);
            int currentScore = currentInTarget.isEmpty()
                    ? -1
                    : ItemHeuristics.evaluate(currentInTarget, desiredType);
            if (!currentInTarget.isEmpty()
                    && ItemStack.canCombine(currentInTarget, originallyHeld)
                    && currentScore > 0) {
                currentScore += 1000;
            }

            int bestSlot = findBestItem(inventory, desiredType, lockedSlots, originallyHeld);
            if (bestSlot == -1) {
                if (currentScore > 0) lockedSlots.add(targetSlot);
                continue;
            }

            int bestScore = ItemHeuristics.evaluate(inventory.getStack(bestSlot), desiredType);
            if (ItemStack.canCombine(inventory.getStack(bestSlot), originallyHeld) && bestScore > 0) {
                bestScore += 1000;
            }

            if (bestScore > currentScore && bestSlot != targetSlot) {
                ItemStack newStack = inventory.getStack(bestSlot).copy();

                if (!currentInTarget.isEmpty()) {
                    int emptyHotbar = getEmptyHotbarSlot(inventory, lockedSlots, preset.slots.keySet(), playerSlot);
                    if (emptyHotbar != -1) {
                        inventory.setStack(emptyHotbar, currentInTarget);
                        inventory.setStack(targetSlot, newStack);
                        inventory.setStack(bestSlot, ItemStack.EMPTY);
                    } else {
                        inventory.setStack(targetSlot, newStack);
                        inventory.setStack(bestSlot, currentInTarget);
                    }
                } else {
                    inventory.setStack(targetSlot, newStack);
                    inventory.setStack(bestSlot, ItemStack.EMPTY);
                }
                lockedSlots.add(targetSlot);
            } else if (currentScore > 0) {
                lockedSlots.add(targetSlot);
            }
        }

        // ── Step 3: off-hand
        if (preset.offHandType != null) {
            placeInOffHand(player, preset.offHandType);
        }

        // ── Step 4: select the right slot (respecting player's choice)
        if (preset.allowSlotOverride) {
            ensurePrimarySlotSelected(player, newActivity, preset);
        }

        player.playerScreenHandler.syncState();
    }

    /**
     * Finds the best matching item in the inventory for {@code type} and
     * places it in the off-hand. The current off-hand item swaps into the
     * source slot.
     */
    private static void placeInOffHand(ServerPlayerEntity player, String type) {
        PlayerInventory inv = player.getInventory();
        ItemStack currentOffHand = inv.offHand.get(OFFHAND_SLOT_INDEX);

        int currentScore = currentOffHand.isEmpty()
                ? -1
                : ItemHeuristics.evaluate(currentOffHand, type);

        int bestSlot = -1;
        int bestScore = currentScore;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            int score = ItemHeuristics.evaluate(stack, type);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) return;

        ItemStack newOff = inv.getStack(bestSlot).copy();
        if (currentOffHand.isEmpty()) {
            inv.setStack(bestSlot, ItemStack.EMPTY);
        } else {
            inv.setStack(bestSlot, currentOffHand.copy());
        }
        inv.offHand.set(OFFHAND_SLOT_INDEX, newOff);
    }

    /**
     * Force the player's held slot to the slot containing the activity's
     * primary tool — but only if the player isn't already holding something
     * usable for this activity.
     */
    private static void ensurePrimarySlotSelected(ServerPlayerEntity player,
                                                  ActivityType activity,
                                                  HotbarPreset preset) {
        PlayerInventory inventory = player.getInventory();
        int currentSlot = inventory.selectedSlot;
        ItemStack currentHeld = inventory.getStack(currentSlot);

        String primaryType = preset != null && preset.slots != null
                ? preset.slots.get(0)
                : null;
        if (primaryType == null) primaryType = getPrimaryItemTypeForActivity(activity);

        // If what the player is already holding is a valid primary item,
        // KEEP that slot — don't yank it (this fixes the "scrolled item gets
        // yanked back to slot 0" complaint).
        if (primaryType != null && !currentHeld.isEmpty()
                && ItemHeuristics.evaluate(currentHeld, primaryType) > 0) {
            return;
        }

        int targetSlot = -1;

        if (preset != null && preset.slots != null) {
            String slot0Type = preset.slots.get(0);
            if (slot0Type != null) {
                ItemStack slot0 = inventory.getStack(0);
                if (!slot0.isEmpty() && ItemHeuristics.evaluate(slot0, slot0Type) > 0) {
                    targetSlot = 0;
                }
            }
        }

        if (targetSlot == -1) {
            int found = findBestHotbarSlotForType(inventory, primaryType);
            if (found != -1) targetSlot = found;
        }

        if (targetSlot == -1 && preset != null && preset.slots != null) {
            for (int i = 0; i < 9; i++) {
                if (preset.slots.containsKey(i) && !inventory.getStack(i).isEmpty()) {
                    targetSlot = i;
                    break;
                }
            }
        }

        if (targetSlot < 0 || targetSlot >= 9) return;
        if (targetSlot == inventory.selectedSlot) return;

        inventory.selectedSlot = targetSlot;
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(targetSlot));
    }

    private static int findBestHotbarSlotForType(PlayerInventory inventory, String type) {
        if (type == null) return -1;
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            int score = ItemHeuristics.evaluate(stack, type);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static int findBestItem(PlayerInventory inventory, String type,
                                    Set<Integer> lockedSlots, ItemStack heldItem) {
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < 36; i++) {
            if (lockedSlots.contains(i)) continue;
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            int score = ItemHeuristics.evaluate(stack, type);
            if (score > 0 && ItemStack.canCombine(stack, heldItem)) {
                score += 1000;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static int getEmptyHotbarSlot(PlayerInventory inventory, Set<Integer> lockedSlots,
                                          Set<Integer> presetSlots, int playerSlot) {
        for (int i = 0; i < 9; i++) {
            if (i == playerSlot) continue; // never overwrite the slot the player is using
            if (inventory.getStack(i).isEmpty()
                    && !lockedSlots.contains(i)
                    && !presetSlots.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Maps each activity to the item type that should be in the player's
     * primary hand. Covers EVERY value of {@link ActivityType}.
     */
    private static String getPrimaryItemTypeForActivity(ActivityType activity) {
        switch (activity) {
            // ── Mining family ────────────────────────────────────────
            case MINING:
            case ORE:
            case STONE:
            case DIAMOND:
            case ANCIENT_DEBRIS:
            case COAL_MINING:
            case IRON_MINING:
            case GOLD_MINING:
            case COPPER_MINING:
            case LAPIS_MINING:
            case REDSTONE_MINING:
            case AMETHYST_MINING:
            case QUARTZ_MINING:
            case DEEPSLATE_MINING:
            case OBSIDIAN_MINING:
            case NETHER_ORE:
            case EMERALD:
                return "PICKAXE";
            case DIRT:
            case SAND:
            case GRAVEL:
            case CLAY:
                return "SHOVEL";
            case SCULK_MINING:
                return "HOE";

            // ── Combat family ────────────────────────────────────────
            case COMBAT:
            case SWORD_COMBAT:
            case ZOMBIE:
            case BATTLE:
            case RAID:
            case PVP:
            case SPIDER:
            case HUSK:
            case STRAY:
            case DROWNED:
            case WITHER_SKELETON:
            case PIGLIN:
            case PIGLIN_BRUTE:
            case HOGLIN:
            case ZOGLIN:
            case VINDICATOR:
            case VEX:
            case SILVERFISH:
            case ENDERMITE:
            case MAGMA_CUBE:
            case SLIME:
            case BERSERK:
            case ENDERMAN:
            case RAVAGER:
            case WITHER:
            case DRAGON:
            case WARDEN:
            case EMERGENCY_COMBAT:
                return "SWORD";

            case AXE_COMBAT:
                return "AXE_COMBAT";
            case TRIDENT_COMBAT:
            case GUARDIAN:
                return "TRIDENT";
            case MACE:
                return "MACE";
            case ARCHERY:
            case SNIPER:
            case SKELETON:
            case BLAZE:
            case GHAST:
            case PHANTOM:
            case PILLAGER:
            case SHULKER:
                return "BOW";
            case CROSSBOW_COMBAT:
                return "CROSSBOW";
            case EXPLOSIVES:
            case CREEPER:
                return "TNT";
            case POTION_COMBAT:
            case WITCH:
            case EVOKER:
                return "POTION";
            case BREEZE:
                return "WIND_CHARGE";
            case DEFENSIVE:
                return "SHIELD";

            // ── Building family ──────────────────────────────────────
            case BUILDING:
            case STONEMASONRY:
            case DECORATING:
            case TERRACOTTA:
            case CONCRETE:
            case GLASSWORK:
            case SCULKING:
            case ROOFING:
            case FURNISHING:
            case LANDSCAPING:
            case DECORATION_PAINTER:
            case DECORATION_BANNER:
            case DECORATION_LIGHTS:
                return "BLOCK";
            case WOODWORKING:
                return "AXE_COMBAT";

            // ── Farming family ───────────────────────────────────────
            case FARMING:
            case CROP_FARMING:
            case TREE_FARMING:
            case ANIMAL_FARMING:
            case COW_FARMING:
            case PIG_FARMING:
            case CHICKEN_FARMING:
            case SHEEP_FARMING:
            case BEE_FARMING:
            case MUSHROOM_FARMING:
            case KELP_FARMING:
            case BAMBOO_FARMING:
            case SUGAR_CANE_FARMING:
            case BREEDING:
            case NETHER_FARMING:
                return "HOE";

            // ── Fishing / water ─────────────────────────────────────
            case FISHING:
            case OCEAN_FISHING:
            case JUNK_FISHING:
                return "FISHING_ROD";
            case OCEAN:
            case OCEAN_EXPLORE:
            case SAILING:
            case DIVING:
                return "OCEAN";

            // ── Crafting ────────────────────────────────────────────
            case ENCHANTING:
                return "ENCHANTING";
            case BREWING:
            case ALCHEMY:
                return "BREWING_INGREDIENT";
            case SMITHING:
            case ANVIL:
            case GRINDSTONE:
            case STONECUTTER:
            case LOOM:
            case CARTOGRAPHY:
            case COMPOSTING:
            case COOKING:
            case SMELTING:
            case TRADING:
                return "TOOL";

            // ── Exploration ─────────────────────────────────────────
            case EXPLORING:
            case CAVING:
            case JUNGLE_EXPLORE:
            case DESERT_EXPLORE:
            case SNOWY_EXPLORE:
            case SWAMP_EXPLORE:
            case MOUNTAIN_EXPLORE:
            case BADLANDS_EXPLORE:
            case MUSHROOM_EXPLORE:
            case FOREST_EXPLORE:
            case PLAINS_EXPLORE:
            case SAVANNA_EXPLORE:
            case STRONGHOLD:
            case MONUMENT:
            case MANSION:
            case FORTRESS:
            case BASTION:
            case END_CITY:
            case ANCIENT_CITY:
            case DEEP_DARK:
            case OVERWORLD:
                return "COMPASS";
            case NETHER_EXPLORE:
            case NETHER_RESOURCES:
            case NETHER:
            case NETHERRACK:
                return "NETHER_GEM";
            case END_EXPLORE:
            case END:
            case END_STONE:
            case ENDER:
                return "ENDER";

            // ── Redstone family ─────────────────────────────────────
            case REDSTONE:
            case REDSTONE_LOGIC:
            case REDSTONE_MACHINES:
            case REDSTONE_TRANSPORT:
            case OBSERVER:
            case PISTON:
            case HOPPER:
            case DROPPER:
            case DISPENSER:
                return "REDSTONE";

            // ── Riding / transport ──────────────────────────────────
            case RIDING:
            case HORSE_RIDING:
            case PIG_RIDING:
            case STRIDER_RIDING:
            case CAMEL_RIDING:
            case LLAMA_RIDING:
            case DONKEY_RIDING:
            case MULE_RIDING:
                return "SADDLE";
            case BOAT:
                return "BOAT";
            case MINECART:
                return "MINECART";
            case RAILS:
                return "RAIL";
            case ELYTRA:
            case PARACHUTE:
            case FALLING:
                return "ELYTRA";

            // ── Utility / misc ──────────────────────────────────────
            case UTILITY:
            case TOOL:
            case SADDLE:
                return "TOOL";
            case LIGHTING:
                return "TORCH";
            case MAP:
                return "MAP";
            case COMPASS:
                return "COMPASS";
            case CLOCK:
                return "CLOCK";
            case SPYGLASS:
                return "SPYGLASS";

            // ── Vital / state-driven ────────────────────────────────
            case NUTRITION:
            case FOOD:
            case LOW_HUNGER:
                return "FOOD";
            case HEALING:
            case REGENERATION:
            case LOW_HEALTH:
            case POISONED:
            case WITHERING:
                return "GOLDEN_APPLE";
            case ON_FIRE:
            case IN_LAVA:
                return "WATER_BUCKET";
            case DROWNING:
                return "BUCKET";
            case TELEPORT:
                return "ENDER_PEARL";
            case FIREWORK:
                return "FIREWORK";
            case BANNER:
                return "BANNER";
            case SIGN:
                return "SIGN";
            case BOOK:
            case WRITING:
                return "BOOK";
            case LEASH:
                return "LEAD";
            case BUCKET_USE:
                return "BUCKET";
            case SLEEPING:
                return "BED";
            case AFK:
            case IDLE:
                return "FOOD";
            case GENERAL:
            case UNKNOWN:
            default:
                return "SWORD";
        }
    }
}
