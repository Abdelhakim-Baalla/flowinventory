package com.flowinventory.profiles;

import com.flowinventory.core.InventoryScanner;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Rules for the G / V cycle only: an activity is offered only if the player
 * carries at least one <b>meaningful</b> item for that mode — not merely any
 * slot on the preset (most presets include {@code FOOD}, which made
 * {@link InventoryScanner#canUseActivity} true for every profile as soon as
 * the player had bread or steak).
 */
public final class ActivityCycleRules {

    private ActivityCycleRules() {}

    /** @return {@code true} if this activity may appear when cycling with G or V. */
    public static boolean canOfferOnCycle(PlayerEntity player, ActivityType activity) {
        if (player == null || activity == null) return false;
        return switch (activity) {
            case GENERAL, IDLE, UNKNOWN -> true;
            case SLEEPING -> hasAny(player, "BED");
            case FOOD -> hasAny(player, "FOOD");
            case COMBAT -> hasAny(player,
                    "SWORD", "AXE_COMBAT", "BOW", "CROSSBOW", "TRIDENT", "MACE", "SHIELD");
            case MINING -> hasAny(player, "PICKAXE", "SHOVEL");
            case BUILDING -> hasAny(player, "BLOCK", "STAIRS", "SLAB", "AXE_TOOL", "PICKAXE");
            case FARMING -> hasAny(player,
                    "HOE", "SEEDS", "WHEAT_SEEDS", "BONE_MEAL", "SHEARS", "FISHING_ROD",
                    "BUCKET", "WATER_BUCKET", "CARROT", "POTATO", "BEETROOT", "SUGAR_CANE");
            case REDSTONE -> hasAny(player,
                    "REDSTONE", "REPEATER", "COMPARATOR", "PISTON", "OBSERVER", "HOPPER",
                    "DISPENSER", "DROPPER", "LEVER");
            case CRAFTING -> hasAny(player,
                    "BOOK", "ENCHANTED_BOOK", "LAPIS_LAZULI", "EXPERIENCE_BOTTLE", "BLAZE_POWDER",
                    "DIAMOND", "EMERALD", "IRON_INGOT", "GOLD_INGOT", "COPPER_INGOT");
            case EXPLORATION -> hasAny(player,
                    "COMPASS", "MAP", "FILLED_MAP", "TORCH", "PICKAXE", "SWORD", "ENDER_PEARL");
            case TRAVEL -> hasAny(player,
                    "ELYTRA", "FIREWORK", "BOAT", "SADDLE", "MINECART", "CARROT_ON_A_STICK",
                    "WARPED_FUNGUS_ON_A_STICK", "LEAD");
            case UTILITY -> hasAny(player,
                    "TOOL", "BUCKET", "WATER_BUCKET", "LAVA_BUCKET", "TORCH", "FLINT_AND_STEEL",
                    "NAME_TAG", "PICKAXE");
            case EMERGENCY_COMBAT, LOW_HEALTH, LOW_HUNGER, ON_FIRE, IN_LAVA, DROWNING, FALLING,
                    POISONED, WITHERING -> InventoryScanner.canUseActivity(player, activity);
        };
    }

    private static boolean hasAny(PlayerEntity player, String... types) {
        for (String t : types) {
            if (t != null && InventoryScanner.hasItem(player, t)) return true;
        }
        return false;
    }
}
