package com.flowinventory.profiles;

import java.util.HashMap;
import java.util.Map;

public class HotbarPreset {
    // Maps hotbar slot (0-8) to a generic item type identifier
    // "SWORD", "PICKAXE", "AXE", "SHOVEL", "HOE", "BOW", "SHIELD", "FOOD", "BLOCK", "TORCH", "SEEDS", "WATER_BUCKET"
    public Map<Integer, String> slots = new HashMap<>();

    public HotbarPreset() {}

    public HotbarPreset add(int slot, String type) {
        slots.put(slot, type);
        return this;
    }
}
