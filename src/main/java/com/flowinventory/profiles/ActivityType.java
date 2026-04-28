package com.flowinventory.profiles;

public enum ActivityType {

    MINING("Mining", "⛏"),
    COMBAT("Combat", "⚔"),
    BUILDING("Building", "🧱"),
    FARMING("Farming", "🌾"),
    GENERAL("General", "🎒");

    public final String displayName;
    public final String icon;

    ActivityType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public ActivityType next() {
        ActivityType[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}