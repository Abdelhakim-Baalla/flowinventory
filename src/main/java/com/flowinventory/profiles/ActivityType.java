package com.flowinventory.profiles;

/**
 * Production-friendly activity model: a small set of high-level contexts.
 * <p>
 * Fine-grained behaviour (sword vs bow vs mining diamond) is handled by
 * {@link com.flowinventory.server.ItemHeuristics} and hotbar presets, not by
 * exploding the enum into dozens of near-duplicate entries.
 */
public enum ActivityType {

    // ── Core gameplay (cycle with G / auto-detect) ───────────────────
    GENERAL("General", "\uD83C\uDF92"),
    COMBAT("Combat", "\u2694"),
    MINING("Mining", "\u26CF"),
    BUILDING("Building", "\uD83E\uDDF1"),
    FARMING("Farming", "\uD83C\uDF3E"),
    REDSTONE("Redstone", "\uD83D\uDD0C"),
    CRAFTING("Crafting", "\uD83D\uDD28"),
    EXPLORATION("Exploration", "\uD83E\uDDED"),
    TRAVEL("Travel", "\uD83D\uDE83"),
    UTILITY("Utility", "\uD83D\uDD27"),
    FOOD("Food", "\uD83E\uDD69"),

    // ── Emergencies & vitals (auto only, high priority) ───────────────
    EMERGENCY_COMBAT("Emergency!", "\u26A0"),
    LOW_HEALTH("Low Health", "\u2764"),
    LOW_HUNGER("Hungry", "\uD83C\uDF56"),
    ON_FIRE("On Fire!", "\uD83D\uDD25"),
    IN_LAVA("In Lava!", "\uD83C\uDF0B"),
    DROWNING("Drowning", "\uD83C\uDF0A"),
    FALLING("Falling", "\u2B07"),
    POISONED("Poisoned", "\u2620"),
    WITHERING("Withering", "\uD83D\uDC80"),

    // ── Meta ─────────────────────────────────────────────────────────
    SLEEPING("Sleeping", "\uD83D\uDECF"),
    IDLE("Idle", "\uD83D\uDCA4"),
    UNKNOWN("Unknown", "\u2753");

    public final String displayName;
    public final String icon;

    ActivityType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    /** @deprecated Use {@link ActivityCycle} for G/V; kept for compatibility. */
    @Deprecated
    public ActivityType next() {
        ActivityType[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    /** @deprecated Use {@link ActivityCycle} for G/V. */
    @Deprecated
    public ActivityType prev() {
        ActivityType[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
}
