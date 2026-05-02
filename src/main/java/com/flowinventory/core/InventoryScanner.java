package com.flowinventory.core;

import com.flowinventory.profiles.ActivityType;
import com.flowinventory.profiles.HotbarPreset;
import com.flowinventory.profiles.ProfileManager;
import com.flowinventory.server.ItemHeuristics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight helper for asking "does the player have anything that scores
 * positively for this item type / activity?".
 * <p>
 * Used by:
 * <ul>
 *   <li>the G/V cycle to skip activities the player can't actually use,</li>
 *   <li>the auto-detector to fall back to GENERAL when the inventory has
 *       nothing relevant,</li>
 *   <li>the tool-durability emergency to find a fresher replacement.</li>
 * </ul>
 */
public final class InventoryScanner {

    private InventoryScanner() {}

    /** True if the player owns ANY item that {@link ItemHeuristics} treats as the given type. */
    public static boolean hasItem(PlayerEntity player, String itemType) {
        if (player == null || itemType == null) return false;

        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (matches(inv.getStack(i), itemType)) return true;
        }
        if (!inv.offHand.isEmpty() && matches(inv.offHand.get(0), itemType)) return true;
        for (ItemStack armor : inv.armor) {
            if (matches(armor, itemType)) return true;
        }
        return false;
    }

    /** Number of items in the entire inventory that match the given type. */
    public static int countItems(PlayerEntity player, String itemType) {
        if (player == null || itemType == null) return 0;
        int count = 0;
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (matches(s, itemType)) count += s.getCount();
        }
        return count;
    }

    /**
     * Returns true if the activity is meaningful for this player right now —
     * either it has no preset (treated as always-usable) or at least the
     * primary item type from the preset exists in the inventory.
     */
    public static boolean canUseActivity(PlayerEntity player, ActivityType activity) {
        if (player == null || activity == null) return false;

        // These never need items
        switch (activity) {
            case GENERAL:
            case UNKNOWN:
            case IDLE:
            case AFK:
            case SLEEPING:
            case OVERWORLD:
            case NETHER:
            case END:
                return true;
            default:
                break;
        }

        HotbarPreset preset = ProfileManager.getPreset(activity);
        if (preset == null || preset.slots == null || preset.slots.isEmpty()) {
            return true;
        }

        String primaryType = preset.slots.get(0);
        if (primaryType == null) return true;
        return hasItem(player, primaryType);
    }

    /** How many distinct preset slots the player has matching items for. */
    public static int countMatchingPresetSlots(PlayerEntity player, ActivityType activity) {
        if (player == null) return 0;
        HotbarPreset preset = ProfileManager.getPreset(activity);
        if (preset == null || preset.slots == null) return 0;

        Set<String> seen = new HashSet<>();
        int matches = 0;
        for (String type : preset.slots.values()) {
            if (type == null) continue;
            if (seen.add(type) && hasItem(player, type)) matches++;
        }
        return matches;
    }

    /**
     * Looks in the inventory for a tool of the same family (e.g. "pickaxe")
     * with significantly more durability remaining than {@code held}. Returns
     * the slot index, or -1 if no better option exists.
     */
    public static int findFresherTool(PlayerEntity player, ItemStack held) {
        if (held == null || held.isEmpty() || !held.isDamageable()) return -1;
        String path = Registries.ITEM.getId(held.getItem()).getPath();
        String family = inferToolFamily(path);
        if (family == null) return -1;

        int heldRemaining = held.getMaxDamage() - held.getDamage();

        int bestSlot = -1;
        int bestRemaining = heldRemaining;
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isEmpty() || !s.isDamageable()) continue;
            String otherPath = Registries.ITEM.getId(s.getItem()).getPath();
            if (!otherPath.contains(family)) continue;
            int rem = s.getMaxDamage() - s.getDamage();
            if (rem > bestRemaining + 50) {
                bestRemaining = rem;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static String inferToolFamily(String path) {
        if (path.contains("pickaxe")) return "pickaxe";
        if (path.contains("shovel")) return "shovel";
        if (path.contains("hoe")) return "hoe";
        if (path.contains("axe") && !path.contains("pickaxe")) return "axe";
        if (path.contains("sword")) return "sword";
        if (path.contains("trident")) return "trident";
        if (path.contains("bow") && !path.contains("crossbow")) return "bow";
        if (path.contains("crossbow")) return "crossbow";
        if (path.contains("fishing_rod")) return "fishing_rod";
        if (path.contains("shears")) return "shears";
        return null;
    }

    private static boolean matches(ItemStack stack, String itemType) {
        if (stack == null || stack.isEmpty()) return false;
        return ItemHeuristics.evaluate(stack, itemType) > 0;
    }
}
