package com.flowinventory.profiles;

import java.util.Arrays;
import java.util.List;

/**
 * Curated cycle order used by the G / V keybinds.
 * <p>
 * The full {@link ActivityType} enum has 160+ values — most of them are
 * mob-specific or state-driven and are useless to scroll through with a
 * key. This list captures the activities a player would actually want to
 * cycle by hand. Anything not in the list is reachable via auto-detection
 * or by editing the player's config.
 */
public final class ActivityCycle {

    public static final List<ActivityType> DEFAULT = Arrays.asList(
            ActivityType.GENERAL,
            ActivityType.COMBAT,
            ActivityType.SWORD_COMBAT,
            ActivityType.AXE_COMBAT,
            ActivityType.ARCHERY,
            ActivityType.CROSSBOW_COMBAT,
            ActivityType.TRIDENT_COMBAT,
            ActivityType.MACE,
            ActivityType.DEFENSIVE,
            ActivityType.MINING,
            ActivityType.CAVING,
            ActivityType.BUILDING,
            ActivityType.WOODWORKING,
            ActivityType.STONEMASONRY,
            ActivityType.DECORATING,
            ActivityType.REDSTONE,
            ActivityType.FARMING,
            ActivityType.CROP_FARMING,
            ActivityType.ANIMAL_FARMING,
            ActivityType.TREE_FARMING,
            ActivityType.BEE_FARMING,
            ActivityType.FISHING,
            ActivityType.OCEAN,
            ActivityType.EXPLORING,
            ActivityType.NETHER,
            ActivityType.END,
            ActivityType.ENCHANTING,
            ActivityType.BREWING,
            ActivityType.SMITHING,
            ActivityType.RIDING,
            ActivityType.HORSE_RIDING,
            ActivityType.BOAT,
            ActivityType.MINECART,
            ActivityType.ELYTRA,
            ActivityType.LIGHTING,
            ActivityType.HEALING,
            ActivityType.FOOD,
            ActivityType.TELEPORT
    );

    private ActivityCycle() {}

    public static int indexOf(ActivityType type) {
        return DEFAULT.indexOf(type);
    }

    public static ActivityType next(ActivityType current) {
        int idx = DEFAULT.indexOf(current);
        return DEFAULT.get((idx == -1 ? 0 : (idx + 1) % DEFAULT.size()));
    }

    public static ActivityType prev(ActivityType current) {
        int idx = DEFAULT.indexOf(current);
        if (idx == -1) return DEFAULT.get(DEFAULT.size() - 1);
        return DEFAULT.get((idx - 1 + DEFAULT.size()) % DEFAULT.size());
    }
}
