package com.flowinventory.profiles;

import java.util.HashMap;
import java.util.Map;

public class ProfileManager {
    private static final Map<ActivityType, HotbarPreset> PRESETS = new HashMap<>();

    static {
        // COMBAT: Sword, Bow, Shield, [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.COMBAT, new HotbarPreset()
                .add(0, "SWORD")
                .add(1, "BOW")
                .add(2, "SHIELD")
                .add(3, "ENDER_PEARL")
                .add(4, "POTION")
                .add(8, "FOOD"));

        // MINING: Pickaxe, Shovel, Torches, Blocks, [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.MINING, new HotbarPreset()
                .add(0, "PICKAXE")
                .add(1, "SHOVEL")
                .add(2, "TORCH")
                .add(3, "WATER_BUCKET")
                .add(4, "BLOCK")
                .add(8, "FOOD"));

        // BUILDING: Axe, Block, Block, Block, Block, [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.BUILDING, new HotbarPreset()
                .add(0, "AXE")
                .add(1, "BLOCK")
                .add(2, "BLOCK")
                .add(3, "BLOCK")
                .add(4, "BLOCK")
                .add(8, "FOOD"));

        // FARMING: Hoe, Seeds, Water Bucket, [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.FARMING, new HotbarPreset()
                .add(0, "HOE")
                .add(1, "SEEDS")
                .add(2, "WATER_BUCKET")
                .add(8, "FOOD"));

        // FISHING: Fishing Rod, [gap], [gap], [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.FISHING, new HotbarPreset()
                .add(0, "FISHING_ROD")
                .add(8, "FOOD"));

        // EXPLORING: Compass/Clock, Map, Spyglass, [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.EXPLORING, new HotbarPreset()
                .add(0, "EXPLORATION")
                .add(1, "EXPLORATION")
                .add(2, "EXPLORATION")
                .add(8, "FOOD"));

        // REDSTONE: Redstone, Repeater, Comparator, Piston, Lever, [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.REDSTONE, new HotbarPreset()
                .add(0, "REDSTONE")
                .add(1, "REDSTONE")
                .add(2, "REDSTONE")
                .add(3, "REDSTONE")
                .add(4, "REDSTONE")
                .add(8, "FOOD"));

        // BREWING: Glass Bottle, Ingredient, [gap], [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.BREWING, new HotbarPreset()
                .add(0, "BREWING")
                .add(1, "BREWING")
                .add(8, "FOOD"));

        // ARCHERY: Bow, Ender Pearl, Shield, [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.ARCHERY, new HotbarPreset()
                .add(0, "BOW")
                .add(1, "ENDER_PEARL")
                .add(2, "SHIELD")
                .add(8, "FOOD"));

        // RIDING: Saddle/Elytra/Rocket, Lead, [gap], [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.RIDING, new HotbarPreset()
                .add(0, "RIDING")
                .add(1, "RIDING")
                .add(8, "FOOD"));

        // GENERAL: Sword, Pickaxe, Axe, [gap], [gap], [gap], [gap], [gap], Food
        PRESETS.put(ActivityType.GENERAL, new HotbarPreset()
                .add(0, "SWORD")
                .add(1, "PICKAXE")
                .add(2, "AXE")
                .add(8, "FOOD"));
    }

    public static HotbarPreset getPreset(ActivityType activity) {
        return PRESETS.getOrDefault(activity, PRESETS.get(ActivityType.GENERAL));
    }
}
