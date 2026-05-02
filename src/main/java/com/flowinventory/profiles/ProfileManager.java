package com.flowinventory.profiles;

import java.util.HashMap;
import java.util.Map;

/**
 * One well-rounded hotbar preset per {@link ActivityType}. Item choice within
 * each category (e.g. best sword vs axe) is left to {@link com.flowinventory.server.ItemHeuristics}.
 */
public class ProfileManager {

    private static final Map<ActivityType, HotbarPreset> PRESETS = new HashMap<>();

    static {
        PRESETS.put(ActivityType.COMBAT, new HotbarPreset()
                .add(0, "SWORD")
                .add(1, "BOW")
                .add(2, "SHIELD")
                .add(3, "ARROW")
                .add(4, "CROSSBOW")
                .add(5, "POTION")
                .add(6, "GOLDEN_APPLE")
                .add(7, "ENDER_PEARL")
                .add(8, "FOOD")
                .offHand("SHIELD"));

        PRESETS.put(ActivityType.MINING, new HotbarPreset()
                .add(0, "PICKAXE")
                .add(1, "SHOVEL")
                .add(2, "TORCH")
                .add(3, "WATER_BUCKET")
                .add(4, "BLOCK")
                .add(5, "FOOD")
                .add(6, "LADDER")
                .add(7, "BUCKET")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.BUILDING, new HotbarPreset()
                .add(0, "BLOCK")
                .add(1, "BLOCK")
                .add(2, "BLOCK")
                .add(3, "STAIRS")
                .add(4, "SLAB")
                .add(5, "AXE")
                .add(6, "PICKAXE")
                .add(7, "SHOVEL")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.FARMING, new HotbarPreset()
                .add(0, "HOE")
                .add(1, "SEEDS")
                .add(2, "WATER_BUCKET")
                .add(3, "BONE_MEAL")
                .add(4, "SHEARS")
                .add(5, "LEAD")
                .add(6, "FISHING_ROD")
                .add(7, "BUCKET")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.REDSTONE, new HotbarPreset()
                .add(0, "REDSTONE")
                .add(1, "REPEATER")
                .add(2, "COMPARATOR")
                .add(3, "PISTON")
                .add(4, "OBSERVER")
                .add(5, "LEVER")
                .add(6, "BUTTON")
                .add(7, "TORCH")
                .add(8, "PICKAXE"));

        PRESETS.put(ActivityType.CRAFTING, new HotbarPreset()
                .add(0, "BOOK")
                .add(1, "ENCHANTED_BOOK")
                .add(2, "LAPIS")
                .add(3, "EXPERIENCE_BOTTLE")
                .add(4, "BOTTLE")
                .add(5, "BLAZE_POWDER")
                .add(6, "DIAMOND")
                .add(7, "EMERALD")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.EXPLORATION, new HotbarPreset()
                .add(0, "COMPASS")
                .add(1, "MAP")
                .add(2, "TORCH")
                .add(3, "FOOD")
                .add(4, "WATER_BUCKET")
                .add(5, "ENDER_PEARL")
                .add(6, "PICKAXE")
                .add(7, "SWORD")
                .add(8, "BOW"));

        PRESETS.put(ActivityType.TRAVEL, new HotbarPreset()
                .add(0, "ELYTRA")
                .add(1, "FIREWORK")
                .add(2, "SADDLE")
                .add(3, "BOAT")
                .add(4, "MINECART")
                .add(5, "CARROT_ON_A_STICK")
                .add(6, "FISHING_ROD")
                .add(7, "LEAD")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.UTILITY, new HotbarPreset()
                .add(0, "TOOL")
                .add(1, "BUCKET")
                .add(2, "WATER_BUCKET")
                .add(3, "TORCH")
                .add(4, "FLINT_AND_STEEL")
                .add(5, "LEAD")
                .add(6, "NAME_TAG")
                .add(7, "PICKAXE")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.FOOD, new HotbarPreset()
                .add(0, "FOOD")
                .add(1, "GOLDEN_CARROT")
                .add(2, "BREAD")
                .add(3, "STEAK")
                .add(4, "GOLDEN_APPLE")
                .add(5, "COOKIE")
                .add(6, "HONEY_BOTTLE")
                .add(7, "BOWL")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.GENERAL, new HotbarPreset()
                .add(0, "SWORD")
                .add(1, "PICKAXE")
                .add(2, "AXE")
                .add(3, "SHOVEL")
                .add(4, "HOE")
                .add(5, "BOW")
                .add(6, "FOOD")
                .add(7, "TORCH")
                .add(8, "BLOCK"));

        PRESETS.put(ActivityType.EMERGENCY_COMBAT, new HotbarPreset()
                .add(0, "SWORD")
                .add(1, "GOLDEN_APPLE")
                .add(2, "POTION")
                .add(3, "ENDER_PEARL")
                .add(4, "MILK_BUCKET")
                .add(5, "BOW")
                .add(6, "ARROW")
                .add(7, "SHIELD")
                .add(8, "FOOD")
                .offHand("SHIELD"));

        PRESETS.put(ActivityType.LOW_HEALTH, new HotbarPreset()
                .add(0, "GOLDEN_APPLE")
                .add(1, "POTION")
                .add(2, "MILK_BUCKET")
                .add(3, "FOOD")
                .add(4, "ENDER_PEARL")
                .add(5, "SHIELD")
                .add(6, "SWORD")
                .add(7, "HONEY_BOTTLE")
                .add(8, "FOOD")
                .offHand("TOTEM"));

        PRESETS.put(ActivityType.LOW_HUNGER, new HotbarPreset()
                .add(0, "STEAK")
                .add(1, "GOLDEN_CARROT")
                .add(2, "BREAD")
                .add(3, "FOOD")
                .add(4, "FOOD")
                .add(5, "GOLDEN_APPLE")
                .add(6, "FOOD")
                .add(7, "FOOD")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.ON_FIRE, new HotbarPreset()
                .add(0, "WATER_BUCKET")
                .add(1, "POTION")
                .add(2, "MILK_BUCKET")
                .add(3, "POTION")
                .add(4, "FOOD")
                .add(5, "GOLDEN_APPLE")
                .add(6, "BLOCK")
                .add(7, "BUCKET")
                .add(8, "SHIELD")
                .offHand("SHIELD"));

        PRESETS.put(ActivityType.IN_LAVA, new HotbarPreset()
                .add(0, "WATER_BUCKET")
                .add(1, "POTION")
                .add(2, "BLOCK")
                .add(3, "GOLDEN_APPLE")
                .add(4, "MILK_BUCKET")
                .add(5, "BUCKET")
                .add(6, "POTION")
                .add(7, "ENDER_PEARL")
                .add(8, "FOOD")
                .offHand("TOTEM"));

        PRESETS.put(ActivityType.DROWNING, new HotbarPreset()
                .add(0, "BUCKET")
                .add(1, "DOOR")
                .add(2, "BLOCK")
                .add(3, "POTION")
                .add(4, "GOLDEN_APPLE")
                .add(5, "FOOD")
                .add(6, "PICKAXE")
                .add(7, "TRIDENT")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.FALLING, new HotbarPreset()
                .add(0, "ELYTRA")
                .add(1, "FIREWORK")
                .add(2, "WATER_BUCKET")
                .add(3, "ENDER_PEARL")
                .add(4, "POTION")
                .add(5, "FOOD")
                .add(6, "GOLDEN_APPLE")
                .add(7, "BLOCK")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.POISONED, new HotbarPreset()
                .add(0, "MILK_BUCKET")
                .add(1, "POTION")
                .add(2, "GOLDEN_APPLE")
                .add(3, "FOOD")
                .add(4, "HONEY_BOTTLE")
                .add(5, "BUCKET")
                .add(6, "SHIELD")
                .add(7, "SWORD")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.WITHERING, new HotbarPreset()
                .add(0, "MILK_BUCKET")
                .add(1, "GOLDEN_APPLE")
                .add(2, "ENCHANTED_GOLDEN_APPLE")
                .add(3, "POTION")
                .add(4, "FOOD")
                .add(5, "BUCKET")
                .add(6, "SHIELD")
                .add(7, "SWORD")
                .add(8, "FOOD")
                .offHand("TOTEM"));

        PRESETS.put(ActivityType.SLEEPING, new HotbarPreset()
                .add(0, "BED")
                .add(1, "BLOCK")
                .add(2, "TORCH")
                .add(3, "FOOD")
                .add(4, "BOOK")
                .add(5, "MAP")
                .add(6, "COMPASS")
                .add(7, "ENDER_PEARL")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.IDLE, new HotbarPreset().noSlotOverride());
        PRESETS.put(ActivityType.UNKNOWN, PRESETS.get(ActivityType.GENERAL));
    }

    public static HotbarPreset getPreset(ActivityType activity) {
        return PRESETS.getOrDefault(activity, PRESETS.get(ActivityType.GENERAL));
    }
}
