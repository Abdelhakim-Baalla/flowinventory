package com.flowinventory.server;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.network.ActivityChangePacket;
import com.flowinventory.network.SortInventoryPacket;
import com.flowinventory.profiles.ActivityType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class FlowInventoryServer {
    
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(
            SortInventoryPacket.ID,
            (server, player, handler, buf, responseSender) -> {
                server.execute(() -> {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        sortInventory(serverPlayer);
                    }
                });
            }
        );
        
        ServerPlayNetworking.registerGlobalReceiver(
            ActivityChangePacket.ID,
            (server, player, handler, buf, responseSender) -> {
                String activityName = buf.readString();
                server.execute(() -> {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        try {
                            ActivityType newActivity = ActivityType.valueOf(activityName);
                            HotbarSwapper.swapHotbar(serverPlayer, newActivity);
                            FlowInventoryMod.LOGGER.debug(
                                "[FlowInventory] Player {} switched to activity: {}", 
                                player.getName().getString(), 
                                newActivity.displayName
                            );
                        } catch (IllegalArgumentException e) {
                            FlowInventoryMod.LOGGER.warn(
                                "[FlowInventory] Invalid activity requested: {}", activityName
                            );
                        }
                    }
                });
            }
        );
        
        FlowInventoryMod.LOGGER.info("[FlowInventory] Server packet handlers registered");
    }

    /** Lower = earlier when sorting by {@code sortMode TIER}. */
    private static int tierSortRank(Item item) {
        if (item == null) return 99;
        return switch (ItemDatabase.getTierName(item)) {
            case "Netherite" -> 0;
            case "Diamond" -> 1;
            case "Iron" -> 2;
            case "Chainmail" -> 3;
            case "Gold" -> 4;
            case "Stone" -> 5;
            case "Leather" -> 6;
            case "Wood" -> 7;
            default -> 50;
        };
    }
    
    private static void sortInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        
        int startSlot = FlowInventoryMod.config.lockHotbar ? 9 : 0;
        
        List<ItemStack> items = new ArrayList<>();
        for (int slot = startSlot; slot < 36; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
                inventory.setStack(slot, ItemStack.EMPTY);
            }
        }
        
        if (items.isEmpty()) return;

        if (FlowInventoryMod.config.mergeStacks) {
            items = mergeStacksAdvanced(items);
        }
        
        String mode = FlowInventoryMod.config.sortMode;
        boolean isAlphabetical = "ALPHABETICAL".equalsIgnoreCase(mode);
        boolean isTier = "TIER".equalsIgnoreCase(mode);

        items.sort((a, b) -> {
            if (isAlphabetical) {
                return a.getName().getString().compareToIgnoreCase(b.getName().getString());
            }
            if (isTier) {
                int tierA = tierSortRank(a.getItem());
                int tierB = tierSortRank(b.getItem());
                if (tierA != tierB) return Integer.compare(tierA, tierB);
                return a.getName().getString().compareToIgnoreCase(b.getName().getString());
            }
            String catA = ItemDatabase.getPrimaryCategory(a.getItem());
            String catB = ItemDatabase.getPrimaryCategory(b.getItem());
            int orderA = getCategoryPriority(catA);
            int orderB = getCategoryPriority(catB);
            if (orderA != orderB) return orderA - orderB;
            return a.getName().getString().compareToIgnoreCase(b.getName().getString());
        });
        
        int slot = startSlot;
        for (ItemStack stack : items) {
            if (slot < 36) {
                inventory.setStack(slot++, stack);
            }
        }
        while (slot < 36) {
            inventory.setStack(slot++, ItemStack.EMPTY);
        }
        
        player.playerScreenHandler.syncState();
        
        player.sendMessage(
            net.minecraft.text.Text.literal("✓ Inventory sorted!")
                .styled(style -> style.withColor(0x44FF44)),
            true
        );
    }
    
    private static List<ItemStack> mergeStacksAdvanced(List<ItemStack> items) {
        items.sort((a, b) -> {
            String idA = getStackKey(a);
            String idB = getStackKey(b);
            return idA.compareTo(idB);
        });
        
        List<ItemStack> result = new ArrayList<>();
        ItemStack current = null;
        
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            
            if (current == null) {
                current = stack.copy();
                current.setCount(stack.getCount());
            } else if (canMerge(current, stack)) {
                int spaceLeft = current.getMaxCount() - current.getCount();
                int toAdd = Math.min(spaceLeft, stack.getCount());
                current.increment(toAdd);
                
                int leftover = stack.getCount() - toAdd;
                if (current.getCount() >= current.getMaxCount()) {
                    result.add(current);
                    if (leftover > 0) {
                        current = stack.copy();
                        current.setCount(leftover);
                    } else {
                        current = null;
                    }
                } else if (leftover > 0) {
                    ItemStack leftoverStack = stack.copy();
                    leftoverStack.setCount(leftover);
                    result.add(current);
                    current = leftoverStack;
                }
            } else {
                result.add(current);
                current = stack.copy();
            }
        }
        
        if (current != null && !current.isEmpty()) {
            result.add(current);
        }
        
        return result;
    }
    
    private static boolean canMerge(ItemStack a, ItemStack b) {
        return ItemStack.canCombine(a, b);
    }
    
    private static String getStackKey(ItemStack stack) {
        if (stack.hasNbt()) {
            return stack.getItem().toString() + "_nbt_" + stack.getNbt().hashCode();
        }
        return stack.getItem().toString();
    }
    
    private static int getCategoryPriority(String category) {
        switch (category) {
            case "SWORD": return 1;
            case "AXE_COMBAT": return 2;
            case "TRIDENT": return 3;
            case "BOW": return 4;
            case "PICKAXE": return 5;
            case "SHOVEL": return 6;
            case "AXE_TOOL": return 7;
            case "HOE": return 8;
            case "HELMET": return 9;
            case "CHESTPLATE": return 10;
            case "LEGGINGS": return 11;
            case "BOOTS": return 12;
            case "SHIELD": return 13;
            case "FOOD": return 14;
            case "BLOCK": return 15;
            case "REDSTONE": return 16;
            case "POTION": return 17;
            case "TORCH": return 18;
            case "GEM": return 19;
            case "INGOT": return 20;
            case "RAW_MATERIAL": return 21;
            default: return 50;
        }
    }
}
