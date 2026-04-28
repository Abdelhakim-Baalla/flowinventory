package com.flowinventory.core;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.*;

public class ActivityDetector {

    private ActivityType currentActivity = ActivityType.GENERAL;

    private static final int HISTORY_SIZE = 60;
    private static final int SWITCH_THRESHOLD = 30;

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
        currentActivity = activity;
        detectedActivity = activity;
        stabilityCounter = SWITCH_THRESHOLD;
        // Reset history to new activity
        for (int i = 0; i < HISTORY_SIZE; i++) {
            history[i] = activity;
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
            }
        } else {
            detectedActivity = dominant;
            stabilityCounter = 0;
        }
    }

    private ActivityType analyzePlayer(ClientPlayerEntity player) {
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