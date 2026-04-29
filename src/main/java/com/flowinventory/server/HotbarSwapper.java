package com.flowinventory.server;

import com.flowinventory.profiles.ActivityType;
import com.flowinventory.profiles.HotbarPreset;
import com.flowinventory.profiles.ProfileManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class HotbarSwapper {

    public static void swapHotbar(ServerPlayerEntity player, ActivityType newActivity) {
        HotbarPreset preset = ProfileManager.getPreset(newActivity);
        if (preset == null) return;

        PlayerInventory inventory = player.getInventory();
        int originalSelectedSlot = inventory.selectedSlot;
        ItemStack originallyHeldItem = inventory.getStack(originalSelectedSlot).copy();

        Set<Integer> lockedSlots = new HashSet<>();

        for (Map.Entry<Integer, String> entry : preset.slots.entrySet()) {
            int targetSlot = entry.getKey();
            String desiredType = entry.getValue();

            ItemStack currentInTarget = inventory.getStack(targetSlot);
            // Current score: add a massive bonus if the item is the one we are CURRENTLY holding!
            int currentScore = currentInTarget.isEmpty() ? -1 : ItemHeuristics.evaluate(currentInTarget, desiredType);
            if (!currentInTarget.isEmpty() && ItemStack.canCombine(currentInTarget, originallyHeldItem)) {
                if (currentScore > 0) currentScore += 1000;
            }

            // Find the best item in the entire inventory (0-35) for this type, ignoring locked slots
            int bestSlot = findBestItem(inventory, desiredType, lockedSlots, originallyHeldItem);

            if (bestSlot != -1) {
                int bestScore = ItemHeuristics.evaluate(inventory.getStack(bestSlot), desiredType);
                // If the item in bestSlot is the held item, give it the same bonus
                if (ItemStack.canCombine(inventory.getStack(bestSlot), originallyHeldItem)) {
                    if (bestScore > 0) bestScore += 1000;
                }

                if (bestScore > currentScore && bestSlot != targetSlot) {
                    // We found a better item for this slot
                    ItemStack newStack = inventory.getStack(bestSlot);

                    // Before we just swap, if currentInTarget is NOT empty, try to move it to an EMPTY hotbar slot first
                    // instead of pushing it back to where the newStack came from (which might be the main inventory).
                    if (!currentInTarget.isEmpty()) {
                        int emptyHotbarSlot = getEmptyHotbarSlot(inventory, lockedSlots, preset.slots.keySet());
                        if (emptyHotbarSlot != -1) {
                            inventory.setStack(emptyHotbarSlot, currentInTarget);
                            inventory.setStack(targetSlot, newStack);
                            inventory.setStack(bestSlot, ItemStack.EMPTY);
                        } else {
                            // Standard swap
                            inventory.setStack(targetSlot, newStack);
                            inventory.setStack(bestSlot, currentInTarget);
                        }
                    } else {
                        // Empty slot, just fill it
                        inventory.setStack(targetSlot, newStack);
                        inventory.setStack(bestSlot, ItemStack.EMPTY);
                    }
                    lockedSlots.add(targetSlot);
                } else if (currentScore > 0) {
                    // The item currently here is already optimal (or tied for optimal), lock it
                    lockedSlots.add(targetSlot);
                }
            }
        }

        // AI Intelligence: When switching to a specific activity, ALWAYS select Slot 0 (the primary tool).
        // This ensures that pressing 'G' immediately equips the right tool instead of leaving you holding
        // whatever item you had in your hand before.
        if (newActivity != ActivityType.GENERAL) {
            inventory.selectedSlot = 0;
            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(0));
        }

        // Sync with client
        player.playerScreenHandler.syncState();
    }

    private static int findBestItem(PlayerInventory inventory, String type, Set<Integer> lockedSlots, ItemStack heldItem) {
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 36; i++) {
            if (lockedSlots.contains(i)) continue; // Don't steal from already correct slots

            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            int score = ItemHeuristics.evaluate(stack, type);
            if (score > 0 && ItemStack.canCombine(stack, heldItem)) {
                score += 1000; // Prioritize held item
            }

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private static int getEmptyHotbarSlot(PlayerInventory inventory, Set<Integer> lockedSlots, Set<Integer> presetSlots) {
        for (int i = 0; i < 9; i++) {
            if (inventory.getStack(i).isEmpty() && !lockedSlots.contains(i) && !presetSlots.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    private static int getEmptyMainSlot(PlayerInventory inventory) {
        for (int i = 9; i < 36; i++) {
            if (inventory.getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
