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

public class ActivityDetector {

    private ActivityType currentActivity = ActivityType.GENERAL;

    private static final int HISTORY_SIZE = 20;
    private static final int SWITCH_THRESHOLD = 20;

    private ActivityType detectedActivity = ActivityType.GENERAL;
    private int stabilityCounter = 0;
    private final ActivityType[] history = new ActivityType[HISTORY_SIZE];
    private int historyIndex = 0;

    public ActivityDetector() {
        for (int i = 0; i < HISTORY_SIZE; i++) {
            history[i] = ActivityType.GENERAL;
        }
    }

    public ActivityType getCurrentActivity() {
        return currentActivity;
    }

    public void forceSetActivity(ActivityType activity) {
        if (currentActivity == activity) return;
        
        currentActivity = activity;
        detectedActivity = activity;
        stabilityCounter = SWITCH_THRESHOLD;
        // Reset history to new activity
        for (int i = 0; i < HISTORY_SIZE; i++) {
            history[i] = activity;
        }

        if (FlowInventoryMod.config.autoApplyProfile) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(currentActivity.name());
            ClientPlayNetworking.send(ActivityChangePacket.ID, buf);
        }
    }

    public void tick(PlayerEntity player) {
        if (player == null) return;
        if (!FlowInventoryMod.config.autoDetectActivity) return;
        
        // Do not auto-switch if the player is looking at a GUI (chest, inventory, etc.)
        if (net.minecraft.client.MinecraftClient.getInstance().currentScreen != null) return;

        ActivityType detected = analyzePlayer(player);

        history[historyIndex % HISTORY_SIZE] = detected;
        historyIndex++;

        ActivityType dominant = getDominantActivity();

        if (dominant == detectedActivity) {
            stabilityCounter++;
            if (stabilityCounter >= SWITCH_THRESHOLD && dominant != currentActivity) {
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
        } else {
            detectedActivity = dominant;
            stabilityCounter = 0;
        }
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

        // ── Specific Item Detection ──────────────────────────
        
        // Combat items
        if (item instanceof SwordItem) return ActivityType.COMBAT;
        if (item instanceof BowItem || item instanceof CrossbowItem) return ActivityType.COMBAT;
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

        // Farming tools & items
        if (item instanceof HoeItem || item instanceof ShearsItem || item instanceof FishingRodItem) return ActivityType.FARMING;
        if (itemId.contains("seed") || itemId.contains("wheat") || itemId.contains("carrot") || 
            itemId.contains("potato") || itemId.contains("beetroot") || itemId.contains("sugar_cane")) return ActivityType.FARMING;
        if (itemId.contains("sapling") || itemId.contains("bamboo") || itemId.contains("bone_meal") || 
            itemId.contains("cocoa") || itemId.contains("mushroom") || itemId.contains("flower") || itemId.contains("egg")) return ActivityType.FARMING;

        // Building materials
        if (item instanceof BlockItem || item instanceof AxeItem) {
            // Axe defaults to building if no mobs are nearby
            return ActivityType.BUILDING;
        }
        if (itemId.contains("planks") || itemId.contains("stone") || itemId.contains("brick") || 
            itemId.contains("slab") || itemId.contains("stair") || itemId.contains("fence") || 
            itemId.contains("door") || itemId.contains("trapdoor") || itemId.contains("scaffold") || 
            itemId.contains("ladder") || itemId.contains("carpet") || itemId.contains("bed")) return ActivityType.BUILDING;

        return ActivityType.GENERAL;
    }

    private ActivityType getDominantActivity() {
        int[] counts = new int[ActivityType.values().length];

        for (ActivityType a : history) {
            counts[a.ordinal()]++;
        }

        ActivityType dominant = ActivityType.GENERAL;
        int maxCount = 0;

        for (ActivityType a : ActivityType.values()) {
            if (counts[a.ordinal()] > maxCount) {
                maxCount = counts[a.ordinal()];
                dominant = a;
            }
        }

        return dominant;
    }
}