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

    private static final int HISTORY_SIZE = 10;
    private static final int SWITCH_THRESHOLD = 5;

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
        ItemStack heldItem = player.getMainHandStack();
        Item item = heldItem.getItem();

        // ── Combat: mobs nearby ──────────────────────────────
        boolean mobsNearby = !player.getWorld()
                .getEntitiesByClass(
                        HostileEntity.class,
                        player.getBoundingBox().expand(
                                FlowInventoryMod.config.combatDetectionRange
                        ),
                        e -> !e.isDead()
                ).isEmpty();

        if (mobsNearby) return ActivityType.COMBAT;

        // ── Combat: holding weapon ───────────────────────────
        if (item instanceof SwordItem) return ActivityType.COMBAT;
        if (item instanceof TridentItem) return ActivityType.COMBAT;
        if (item instanceof BowItem) return ActivityType.COMBAT;
        if (item instanceof CrossbowItem) return ActivityType.COMBAT;

        // ── Mining: holding pickaxe or shovel ────────────────
        if (item instanceof PickaxeItem) return ActivityType.MINING;
        if (item instanceof ShovelItem) return ActivityType.MINING;

        // ── Farming: holding farming tools ───────────────────
        if (item instanceof HoeItem) return ActivityType.FARMING;
        if (item instanceof ShearsItem) return ActivityType.FARMING;

        // ── Farming: holding farming items ───────────────────
        String itemId = net.minecraft.registry.Registries.ITEM
                .getId(item).toString();
        if (itemId.contains("seed")) return ActivityType.FARMING;
        if (itemId.contains("wheat")) return ActivityType.FARMING;
        if (itemId.contains("carrot")) return ActivityType.FARMING;
        if (itemId.contains("potato")) return ActivityType.FARMING;
        if (itemId.contains("beetroot")) return ActivityType.FARMING;
        if (itemId.contains("sugar_cane")) return ActivityType.FARMING;
        if (itemId.contains("melon")) return ActivityType.FARMING;
        if (itemId.contains("pumpkin")) return ActivityType.FARMING;
        if (itemId.contains("bone_meal")) return ActivityType.FARMING;
        
        // ── Farming: all crop-related blocks ─────────────────
        if (itemId.contains("sapling")) return ActivityType.FARMING;
        if (itemId.contains("bamboo")) return ActivityType.FARMING;
        if (itemId.contains("cocoa")) return ActivityType.FARMING;
        if (itemId.contains("mushroom")) return ActivityType.FARMING;
        if (itemId.contains("flower")) return ActivityType.FARMING;
        if (itemId.contains("egg")) return ActivityType.FARMING;

        // ── Mining: ores and mining items ─────────────────────
        if (itemId.contains("ore")) return ActivityType.MINING;
        if (itemId.contains("torch")) return ActivityType.MINING;
        if (itemId.contains("tnt")) return ActivityType.MINING;
        if (itemId.contains("chest")) return ActivityType.MINING;

        // ── Combat: armor and potions ─────────────────────────
        if (item instanceof ArmorItem) return ActivityType.COMBAT;
        if (item instanceof ShieldItem) return ActivityType.COMBAT;
        if (itemId.contains("potion")) return ActivityType.COMBAT;
        if (itemId.contains("golden_apple")) return ActivityType.COMBAT;
        if (itemId.contains("totem")) return ActivityType.COMBAT;

        // ── Building: all building materials ──────────────────
        if (itemId.contains("planks")) return ActivityType.BUILDING;
        if (itemId.contains("stone")) return ActivityType.BUILDING;
        if (itemId.contains("brick")) return ActivityType.BUILDING;
        if (itemId.contains("glass")) return ActivityType.BUILDING;
        if (itemId.contains("wool")) return ActivityType.BUILDING;
        if (itemId.contains("concrete")) return ActivityType.BUILDING;
        if (itemId.contains("terracotta")) return ActivityType.BUILDING;
        if (itemId.contains("wood")) return ActivityType.BUILDING;
        if (itemId.contains("log")) return ActivityType.BUILDING;
        if (itemId.contains("slab")) return ActivityType.BUILDING;
        if (itemId.contains("stair")) return ActivityType.BUILDING;
        if (itemId.contains("fence")) return ActivityType.BUILDING;
        if (itemId.contains("door")) return ActivityType.BUILDING;
        if (itemId.contains("trapdoor")) return ActivityType.BUILDING;
        if (itemId.contains("scaffold")) return ActivityType.BUILDING;
        if (itemId.contains("ladder")) return ActivityType.BUILDING;
        if (itemId.contains("carpet")) return ActivityType.BUILDING;
        if (itemId.contains("bed")) return ActivityType.BUILDING;
        if (itemId.contains("torch")) return ActivityType.BUILDING;

        // ── Building: holding blocks ─────────────────────────
        if (item instanceof BlockItem) return ActivityType.BUILDING;

        // ── Axe: could be combat or building ─────────────────
        if (item instanceof AxeItem) return ActivityType.BUILDING;

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