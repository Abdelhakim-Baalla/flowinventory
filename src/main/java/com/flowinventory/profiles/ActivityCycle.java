package com.flowinventory.profiles;

import java.util.Arrays;
import java.util.List;

/**
 * Curated list for the G / V keys: only the gameplay modes players actually
 * want to switch between. State modes (low health, emergency, etc.) are left
 * to auto-detection so they are not clutter in the cycle.
 */
public final class ActivityCycle {

    public static final List<ActivityType> DEFAULT = Arrays.asList(
            ActivityType.GENERAL,
            ActivityType.COMBAT,
            ActivityType.MINING,
            ActivityType.BUILDING,
            ActivityType.FARMING,
            ActivityType.REDSTONE,
            ActivityType.CRAFTING,
            ActivityType.EXPLORATION,
            ActivityType.TRAVEL,
            ActivityType.UTILITY,
            ActivityType.FOOD,
            ActivityType.SLEEPING
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
