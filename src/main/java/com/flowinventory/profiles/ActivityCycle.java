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

            // ── Combat family ───────────────────────────────────────────
            ActivityType.COMBAT,
            ActivityType.SWORD_COMBAT,
            ActivityType.AXE_COMBAT,
            ActivityType.MACE,
            ActivityType.ARCHERY,
            ActivityType.CROSSBOW_COMBAT,
            ActivityType.TRIDENT_COMBAT,
            ActivityType.DEFENSIVE,
            ActivityType.POTION_COMBAT,
            ActivityType.EXPLOSIVES,

            // ── Mining family ───────────────────────────────────────────
            ActivityType.MINING,
            ActivityType.CAVING,
            ActivityType.DEEPSLATE_MINING,
            ActivityType.OBSIDIAN_MINING,
            ActivityType.ANCIENT_DEBRIS,

            // ── Building family ─────────────────────────────────────────
            ActivityType.BUILDING,
            ActivityType.WOODWORKING,
            ActivityType.STONEMASONRY,
            ActivityType.DECORATING,
            ActivityType.GLASSWORK,
            ActivityType.TERRACOTTA,
            ActivityType.CONCRETE,
            ActivityType.ROOFING,
            ActivityType.FURNISHING,
            ActivityType.LANDSCAPING,
            ActivityType.DECORATION_BANNER,
            ActivityType.DECORATION_PAINTER,
            ActivityType.SCULKING,

            // ── Redstone & engineering ──────────────────────────────────
            ActivityType.REDSTONE,
            ActivityType.REDSTONE_LOGIC,
            ActivityType.REDSTONE_MACHINES,
            ActivityType.REDSTONE_TRANSPORT,
            ActivityType.RAILS,

            // ── Farming family ──────────────────────────────────────────
            ActivityType.FARMING,
            ActivityType.CROP_FARMING,
            ActivityType.ANIMAL_FARMING,
            ActivityType.TREE_FARMING,
            ActivityType.BEE_FARMING,
            ActivityType.MUSHROOM_FARMING,
            ActivityType.SUGAR_CANE_FARMING,
            ActivityType.BAMBOO_FARMING,
            ActivityType.KELP_FARMING,
            ActivityType.NETHER_FARMING,

            // ── Fishing / water ─────────────────────────────────────────
            ActivityType.FISHING,
            ActivityType.OCEAN_FISHING,
            ActivityType.OCEAN,
            ActivityType.DIVING,

            // ── Exploration ─────────────────────────────────────────────
            ActivityType.EXPLORING,
            ActivityType.NETHER,
            ActivityType.NETHER_EXPLORE,
            ActivityType.END,
            ActivityType.END_EXPLORE,
            ActivityType.STRONGHOLD,
            ActivityType.MONUMENT,
            ActivityType.MANSION,
            ActivityType.FORTRESS,
            ActivityType.BASTION,
            ActivityType.END_CITY,
            ActivityType.ANCIENT_CITY,
            ActivityType.DEEP_DARK,

            // ── Crafting / workshops ────────────────────────────────────
            ActivityType.ENCHANTING,
            ActivityType.BREWING,
            ActivityType.ALCHEMY,
            ActivityType.SMITHING,
            ActivityType.ANVIL,
            ActivityType.GRINDSTONE,
            ActivityType.STONECUTTER,
            ActivityType.LOOM,
            ActivityType.CARTOGRAPHY,
            ActivityType.COMPOSTING,
            ActivityType.COOKING,
            ActivityType.SMELTING,
            ActivityType.TRADING,

            // ── Riding / transport ──────────────────────────────────────
            ActivityType.RIDING,
            ActivityType.HORSE_RIDING,
            ActivityType.BOAT,
            ActivityType.MINECART,
            ActivityType.ELYTRA,
            ActivityType.PIG_RIDING,
            ActivityType.STRIDER_RIDING,
            ActivityType.CAMEL_RIDING,
            ActivityType.LLAMA_RIDING,

            // ── Utility / misc ──────────────────────────────────────────
            ActivityType.LIGHTING,
            ActivityType.HEALING,
            ActivityType.FOOD,
            ActivityType.TELEPORT,
            ActivityType.ENDER,
            ActivityType.MAP,
            ActivityType.COMPASS,
            ActivityType.SPYGLASS,
            ActivityType.BOOK,
            ActivityType.BANNER,
            ActivityType.SIGN,
            ActivityType.BUCKET_USE,
            ActivityType.LEASH,
            ActivityType.FIREWORK,
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
