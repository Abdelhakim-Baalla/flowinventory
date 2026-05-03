package com.flowinventory.core;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import com.flowinventory.server.ItemDatabase;
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
        
        // Step 1: collect items from ALL slots 0-35 (hotbar + main inventory)
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }
        
        if (items.isEmpty()) return;
        
        // Step 2: sort using enhanced algorithm
        List<ItemStack> sorted = sortByType(mergeStacks(items));
        
        // Step 3: write back to slots 0-35
        int slotIndex = 0;
        for (ItemStack stack : sorted) {
            if (slotIndex < 36) {
                inventory.setStack(slotIndex++, stack);
            }
        }
        while (slotIndex < 36) {
            inventory.setStack(slotIndex++, ItemStack.EMPTY);
        }
        
        // Step 4: notify player
        player.sendMessage(
            Text.literal("✓ Inventory sorted (" + activity.displayName + " style)")
                .styled(style -> style.withColor(0x44FF44)),
            true
        );
    }
    
    private List<ItemStack> sortByType(List<ItemStack> items) {
        List<ItemStack> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> {
            int catA = getCategoryOrder(a.getItem());
            int catB = getCategoryOrder(b.getItem());
            if (catA != catB) return catA - catB;
            return a.getName().getString().compareToIgnoreCase(b.getName().getString());
        });
        return sorted;
    }
    
    private List<ItemStack> mergeStacks(List<ItemStack> items) {
        // Group by item identity (ignoring NBT for stackability)
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
        // Use item identity for merging, but respect custom NBT that prevents stacking
        if (stack.hasNbt()) {
            return stack.getItem().toString() + "_nbt_" + stack.getNbt().hashCode();
        }
        return stack.getItem().toString();
    }
    
    private int getCategoryOrder(Item item) {
        String category = ItemDatabase.getPrimaryCategory(item);
        
        // Priority order (lower = earlier in inventory)
        switch (category) {
            case "SWORD": return 1;
            case "AXE_COMBAT": return 2;
            case "TRIDENT": return 3;
            case "MACE": return 4;
            case "BOW": return 5;
            case "PICKAXE": return 6;
            case "SHOVEL": return 7;
            case "AXE_TOOL": return 8;
            case "HOE": return 9;
            case "HELMET": return 10;
            case "CHESTPLATE": return 11;
            case "LEGGINGS": return 12;
            case "BOOTS": return 13;
            case "SHIELD": return 14;
            case "FOOD": return 15;
            case "BLOCK": return 16;
            case "STAIRS": return 17;
            case "SLAB": return 18;
            case "WALL": return 19;
            case "FENCE": return 20;
            case "DOOR": return 21;
            case "REDSTONE": return 22;
            case "RAIL": return 23;
            case "POTION": return 24;
            case "TORCH": return 25;
            case "GEM": return 26;
            case "INGOT": return 27;
            case "RAW_MATERIAL": return 28;
            case "NETHER_GEM": return 29;
            case "ENDER": return 30;
            case "BLAZE": return 31;
            case "LOG": return 32;
            case "LEAVES": return 33;
            case "SAPLING": return 34;
            case "FLOWER": return 35;
            case "MUSHROOM": return 36;
            case "CROP": return 37;
            case "BAKED_GOODS": return 38;
            case "ENCHANTING": return 39;
            case "BOOK": return 40;
            case "TOOL": return 41;
            case "BUCKET": return 42;
            case "COMPASS": return 43;
            case "CLOCK": return 44;
            case "MAP": return 45;
            case "PAINTING": return 46;
            case "BANNER": return 47;
            case "ARMOR_STAND": return 48;
            case "SPAWN_EGG": return 49;
            case "COMMAND": return 50;
            default: return 99;
        }
    }
}
