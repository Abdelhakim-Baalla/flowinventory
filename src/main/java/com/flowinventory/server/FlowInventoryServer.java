package com.flowinventory.server;

import com.flowinventory.network.SortInventoryPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class FlowInventoryServer {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(
                SortInventoryPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> sortInventory(player));
                }
        );
    }

    private static void sortInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();

        int startSlot = com.flowinventory.FlowInventoryMod.config.lockHotbar ? 9 : 0;

        // Collect items from inventory slots
        List<ItemStack> items = new ArrayList<>();
        for (int slot = startSlot; slot < 36; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
                inventory.setStack(slot, ItemStack.EMPTY);
            }
        }

        if (items.isEmpty()) return;

        // Merge same-type stacks
        items = mergeStacks(items);

        // Sort
        boolean isAlphabetical = "ALPHABETICAL".equalsIgnoreCase(com.flowinventory.FlowInventoryMod.config.sortMode);
        items.sort((a, b) -> {
            if (isAlphabetical) {
                return a.getName().getString().compareToIgnoreCase(b.getName().getString());
            } else {
                int catA = getCategoryOrder(a.getItem());
                int catB = getCategoryOrder(b.getItem());
                if (catA != catB) return catA - catB;
                return a.getName().getString().compareToIgnoreCase(b.getName().getString());
            }
        });

        // Write back to slots
        int slot = startSlot;
        for (ItemStack stack : items) {
            if (slot < 36) {
                inventory.setStack(slot++, stack);
            }
        }
        // Clear remaining slots
        while (slot < 36) {
            inventory.setStack(slot++, ItemStack.EMPTY);
        }

        // Sync with client
        player.playerScreenHandler.syncState();

        player.sendMessage(
                net.minecraft.text.Text.literal("✓ Inventory sorted!")
                        .formatted(net.minecraft.util.Formatting.GREEN),
                true
        );
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> items) {
        Map<String, List<ItemStack>> grouped = new LinkedHashMap<>();

        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            String key = getStackKey(stack);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(stack);
        }

        List<ItemStack> result = new ArrayList<>();
        for (List<ItemStack> group : grouped.values()) {
            // Merge into full stacks
            ItemStack current = group.get(0).copy();
            for (int i = 1; i < group.size(); i++) {
                ItemStack toMerge = group.get(i);
                int canAdd = current.getMaxCount() - current.getCount();
                int toAdd = Math.min(canAdd, toMerge.getCount());
                current.increment(toAdd);

                int leftover = toMerge.getCount() - toAdd;
                if (current.getCount() >= current.getMaxCount()) {
                    result.add(current);
                    if (leftover > 0) {
                        current = toMerge.copy();
                        current.setCount(leftover);
                    } else {
                        current = null;
                        // Continue to next, will be set by next iteration or after loop
                        if (i + 1 < group.size()) {
                            current = group.get(i + 1).copy();
                            i++;
                        }
                    }
                } else if (leftover > 0) {
                    // current has space but leftover exists (shouldn't happen, but safety)
                    ItemStack leftoverStack = toMerge.copy();
                    leftoverStack.setCount(leftover);
                    result.add(current);
                    current = leftoverStack;
                }
            }
            if (current != null && !current.isEmpty()) {
                result.add(current);
            }
        }
        return result;
    }

    private static String getStackKey(ItemStack stack) {
        if (stack.hasNbt()) {
            return stack.getItem().toString() + "_nbt_" + stack.getNbt().hashCode();
        }
        return stack.getItem().toString();
    }

    private static int getCategoryOrder(Item item) {
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