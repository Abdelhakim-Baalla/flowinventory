package com.flowinventory.profiles;

import java.util.HashMap;
import java.util.Map;

/**
 * Declarative description of an "ideal" hotbar layout for one activity.
 * <p>
 * Each entry maps a hotbar slot index (0–8) to a logical item type
 * (e.g. "SWORD", "SHIELD", "BLOCK", "TORCH"). The off-hand can also be
 * managed via {@link #offHandType}, which {@link
 * com.flowinventory.server.HotbarSwapper} uses to fish out the best
 * matching item from the inventory and put it in slot 40.
 */
public class HotbarPreset {

    /** slot index (0-8) → logical item type to place there. */
    public Map<Integer, String> slots = new HashMap<>();

    /** Logical item type for the off-hand slot, or null to leave it alone. */
    public String offHandType;

    /**
     * If true, the swapper is allowed to override the player's currently
     * selected slot to the activity's primary tool. Default true.
     * Combat presets keep this true so that emergencies always drag the
     * sword into the player's hand.
     */
    public boolean allowSlotOverride = true;

    public HotbarPreset() {}

    public HotbarPreset add(int slot, String type) {
        slots.put(slot, type);
        return this;
    }

    public HotbarPreset offHand(String type) {
        this.offHandType = type;
        return this;
    }

    public HotbarPreset noSlotOverride() {
        this.allowSlotOverride = false;
        return this;
    }
}
