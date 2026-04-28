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
        List<ItemStack> sorted = sortByType(mergeStacks(items));

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

    private List<ItemStack> mergeStacks(List<ItemStack> items) {
        Map<String, ItemStack> merged = new LinkedHashMap<>();

        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;

            String key = getStackKey(stack);

            if (merged.containsKey(key)) {
                ItemStack existing = merged.get(key);
                int canAdd = existing.getMaxCount() - existing.getCount();
                int toAdd = Math.min(canAdd, stack.getCount());
                existing.increment(toAdd);

                int leftover = stack.getCount() - toAdd;
                if (leftover > 0) {
                    ItemStack leftoverStack = stack.copy();
                    leftoverStack.setCount(leftover);
                    merged.put(key + "_" + System.nanoTime(), leftoverStack);
                }
            } else {
                merged.put(key, stack.copy());
            }
        }

        return new ArrayList<>(merged.values());
    }

    private String getStackKey(ItemStack stack) {
        if (stack.hasNbt()) {
            return stack.getItem().toString() + "_" + stack.getNbt().hashCode();
        }
        return stack.getItem().toString();
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