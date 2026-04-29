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
