package com.flowinventory.core;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import com.flowinventory.network.ActivityChangePacket;
import net.minecraft.network.PacketByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.registry.tag.ItemTags;

public class ActivityDetector {

    private ActivityType currentActivity = ActivityType.GENERAL;

    private int manualOverrideCooldown = 0;
    private final Map<ActivityType, Integer> activityWeights = new HashMap<>();
    private static final int MOMENTUM_MAX = 100;
    private static final int MOMENTUM_SWITCH_THRESHOLD = 60;

    public ActivityDetector() {
        for (ActivityType type : ActivityType.values()) {
            activityWeights.put(type, 0);
        }
    }

    public ActivityType getCurrentActivity() {
        return currentActivity;
    }

    public void forceSetActivity(ActivityType activity) {
        if (currentActivity == activity) return;
        
        currentActivity = activity;
        manualOverrideCooldown = 100;

        // Boost new activity weight
        activityWeights.put(activity, MOMENTUM_MAX);
        for (ActivityType type : ActivityType.values()) {
            if (type != activity) activityWeights.put(type, 0);
        }

        if (FlowInventoryMod.config.autoApplyProfile) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(currentActivity.name());
            ClientPlayNetworking.send(ActivityChangePacket.ID, buf);
        }

        // Lock in manual mode for 5 seconds (100 ticks)
        manualOverrideCooldown = 100;
    }

    public void tick(PlayerEntity player) {
        if (player == null) return;
        if (!FlowInventoryMod.config.autoDetectActivity) return;
        
        // Do not auto-switch if the player is looking at a GUI (chest, inventory, etc.)
        if (net.minecraft.client.MinecraftClient.getInstance().currentScreen != null) return;

        if (manualOverrideCooldown > 0) {
            manualOverrideCooldown--;
            return;
        }

        ActivityType detected = analyzePlayer(player);
        updateWeights(detected);

        ActivityType dominant = getStrongestActivity();

        if (dominant != currentActivity && activityWeights.getOrDefault(dominant, 0) >= MOMENTUM_SWITCH_THRESHOLD) {
            currentActivity = dominant;
            FlowInventoryMod.LOGGER.debug(
                    "[FlowInventory] Activity changed to: {}",
                    currentActivity.displayName
            );

            if (FlowInventoryMod.config.autoApplyProfile) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(currentActivity.name());
                ClientPlayNetworking.send(ActivityChangePacket.ID, buf);
            }
        }
    }

    private void updateWeights(ActivityType detected) {
        // Decay other weights
        for (ActivityType type : ActivityType.values()) {
            int currentWeight = activityWeights.getOrDefault(type, 0);
            if (type == detected) {
                activityWeights.put(type, Math.min(MOMENTUM_MAX, currentWeight + 10));
            } else {
                activityWeights.put(type, Math.max(0, currentWeight - 5));
            }
        }
    }

    private ActivityType getStrongestActivity() {
        ActivityType strongest = currentActivity;
        int maxWeight = activityWeights.getOrDefault(currentActivity, 0);

        for (Map.Entry<ActivityType, Integer> entry : activityWeights.entrySet()) {
            if (entry.getValue() > maxWeight) {
                maxWeight = entry.getValue();
                strongest = entry.getKey();
            }
        }
        return strongest;
    }

    private ActivityType analyzePlayer(PlayerEntity player) {
        ItemStack heldStack = player.getMainHandStack();
        Item item = heldStack.getItem();
        String itemId = Registries.ITEM.getId(item).toString();

        // ── Combat: mobs nearby ──────────────────────────────
        boolean mobsNearby = !player.getWorld()
                .getEntitiesByClass(
                        HostileEntity.class,
                        player.getBoundingBox().expand(
                                FlowInventoryMod.config.combatDetectionRange
                        ),
                        e -> !e.isDead()
                ).isEmpty();

        if (mobsNearby) {
            // If mobs are nearby and we hold a weapon OR even a tool, switch to Combat
            if (item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || 
                item instanceof BowItem || item instanceof CrossbowItem) {
                return ActivityType.COMBAT;
            }
            return ActivityType.COMBAT; // Default to combat if mobs are very close
        }

        // ── Archery: holding bows/crossbows ──────────────────
        if (item instanceof BowItem || item instanceof CrossbowItem || heldStack.isIn(ItemTags.ARROWS)) {
            return ActivityType.ARCHERY;
        }

        // ── Riding: holding saddles/leads/elytra ──────────────
        if (item instanceof SaddleItem || itemId.equals("minecraft:elytra") || itemId.equals("minecraft:lead") || 
            itemId.contains("horse_armor") || itemId.contains("firework_rocket") || 
            itemId.equals("minecraft:carrot_on_a_stick") || itemId.equals("minecraft:warped_fungus_on_a_stick")) {
            return ActivityType.RIDING;
        }

        // Combat items
        if (item instanceof SwordItem) return ActivityType.COMBAT;
        if (item instanceof ShieldItem) return ActivityType.COMBAT;
        if (item instanceof TridentItem) return ActivityType.COMBAT;
        if (itemId.contains("ender_pearl") || itemId.contains("potion") || 
            itemId.contains("golden_apple") || itemId.contains("totem")) return ActivityType.COMBAT;
        if (item instanceof ArmorItem) return ActivityType.COMBAT;

        // Mining tools
        if (item instanceof PickaxeItem) return ActivityType.MINING;
        if (item instanceof ShovelItem) return ActivityType.MINING;
        if (itemId.contains("torch") || itemId.contains("lantern") || itemId.contains("ore")) return ActivityType.MINING;
        if (itemId.contains("tnt") || itemId.contains("spyglass")) return ActivityType.MINING;

        // Fishing & Exploration
        if (item instanceof FishingRodItem) return ActivityType.FISHING;
        if (itemId.contains("compass") || itemId.contains("clock") || itemId.contains("map")) return ActivityType.EXPLORING;

        // Farming tools & items
        if (item instanceof HoeItem || item instanceof ShearsItem) return ActivityType.FARMING;
        if (heldStack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS) || itemId.contains("seed") || itemId.contains("wheat") || itemId.contains("carrot") || 
            itemId.contains("potato") || itemId.contains("beetroot") || itemId.contains("sugar_cane")) return ActivityType.FARMING;
        if (heldStack.isIn(ItemTags.SAPLINGS) || heldStack.isIn(ItemTags.LEAVES) || heldStack.isIn(ItemTags.FLOWERS) || 
            itemId.contains("bamboo") || itemId.contains("bone_meal") || itemId.contains("cocoa") || 
            itemId.contains("mushroom") || itemId.contains("egg")) return ActivityType.FARMING;

        // Building materials
        if (item instanceof BlockItem || item instanceof AxeItem) {
            // Axe defaults to building if no mobs are nearby
            return ActivityType.BUILDING;
        }
        if (heldStack.isIn(ItemTags.LOGS) || heldStack.isIn(ItemTags.PLANKS) || heldStack.isIn(ItemTags.DOORS) || 
            heldStack.isIn(ItemTags.SIGNS) || heldStack.isIn(ItemTags.BEDS) || heldStack.isIn(ItemTags.STAIRS) || 
            heldStack.isIn(ItemTags.SLABS) || heldStack.isIn(ItemTags.WALLS) || heldStack.isIn(ItemTags.FENCES) || 
            itemId.contains("stone") || itemId.contains("brick") || itemId.contains("scaffold") || 
            itemId.contains("ladder") || itemId.contains("carpet")) return ActivityType.BUILDING;

        // ── Redstone: holding wires or components ────────────
        if (itemId.contains("redstone") || itemId.contains("repeater") || 
            itemId.contains("comparator") || itemId.contains("piston") || 
            itemId.contains("observer") || itemId.contains("lever") || 
            itemId.contains("pressure_plate") || itemId.contains("button")) return ActivityType.REDSTONE;

        // ── Brewing: holding bottles or ingredients ─────────
        if (itemId.contains("glass_bottle") || itemId.contains("blaze_powder") || 
            itemId.contains("nether_wart") || itemId.contains("ghast_tear") || 
            itemId.contains("magma_cream") || itemId.contains("fermented_spider_eye")) return ActivityType.BREWING;

        return ActivityType.GENERAL;
    }
}