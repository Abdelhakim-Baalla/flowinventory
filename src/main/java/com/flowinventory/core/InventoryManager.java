package com.flowinventory.core;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class InventoryManager {

    public void sortPlayerInventory(PlayerEntity player) {
        if (player == null) return;

        ActivityType activity = FlowInventoryMod.activityDetector.getCurrentActivity();

        FlowInventoryMod.LOGGER.debug(
                "[FlowInventory] Sorting inventory for activity: {}",
                activity.displayName
        );

        PlayerInventory inventory = player.getInventory();

        // Step 1: collect items from slots 9-35 (main inventory, not hotbar)
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }

        if (items.isEmpty()) return;

        // Step 2: sort
        List<ItemStack> sorted = sortByType(items);

        // Step 3: write back
        int slotIndex = 9;
        for (ItemStack stack : sorted) {
            inventory.setStack(slotIndex++, stack);
        }
        while (slotIndex < 36) {
            inventory.setStack(slotIndex++, ItemStack.EMPTY);
        }

        // Step 4: notify player
        player.sendMessage(
                Text.literal("✓ Inventory sorted!")
                        .formatted(Formatting.GREEN),
                true
        );
    }

    private List<ItemStack> sortByType(List<ItemStack> items) {
        List<ItemStack> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> {
            int catA = getCategoryOrder(a.getItem());
            int catB = getCategoryOrder(b.getItem());
            if (catA != catB) return catA - catB;
            return a.getName().getString().compareTo(b.getName().getString());
        });
        return sorted;
    }

    private int getCategoryOrder(Item item) {
        if (item instanceof SwordItem) return 1;
        if (item instanceof PickaxeItem) return 2;
        if (item instanceof AxeItem) return 3;
        if (item instanceof ShovelItem) return 4;
        if (item instanceof HoeItem) return 5;
        if (item instanceof ArmorItem) return 6;
        if (item instanceof ShieldItem) return 7;
        if (item.getFoodComponent() != null) return 8;
        if (item instanceof BlockItem) return 9;
        return 10;
    }
}