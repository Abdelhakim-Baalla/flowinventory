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
            int currentScore = currentInTarget.isEmpty() ? -1 : evaluateItem(currentInTarget.getItem(), desiredType);

            // Find the best item in the entire inventory (0-35) for this type, ignoring locked slots
            int bestSlot = findBestItem(inventory, desiredType, lockedSlots);

            if (bestSlot != -1) {
                int bestScore = evaluateItem(inventory.getStack(bestSlot).getItem(), desiredType);
                if (bestScore > currentScore && bestSlot != targetSlot) {
                    // We found a better item for this slot
                    ItemStack newStack = inventory.getStack(bestSlot);

                    // Swap them
                    inventory.setStack(targetSlot, newStack);
                    inventory.setStack(bestSlot, currentInTarget);
                    lockedSlots.add(targetSlot);
                } else if (currentScore > 0) {
                    // The item currently here is already optimal (or tied for optimal), lock it
                    lockedSlots.add(targetSlot);
                }
            } else {
                // We did not find anything to put here.
                // If the current item doesn't fit the preset AT ALL (score <= 0), clear it out!
                if (currentScore <= 0 && !currentInTarget.isEmpty()) {
                    int emptySlot = getEmptyMainSlot(inventory);
                    if (emptySlot != -1) {
                        inventory.setStack(emptySlot, currentInTarget);
                        inventory.setStack(targetSlot, ItemStack.EMPTY);
                    }
                }
            }
        }

        // AI Intelligence: If the item the player was holding was moved to a different hotbar slot,
        // automatically change their selected slot to follow the item!
        if (!originallyHeldItem.isEmpty()) {
            boolean found = false;
            for (int i = 0; i < 9; i++) {
                if (ItemStack.canCombine(inventory.getStack(i), originallyHeldItem)) {
                    if (i != originalSelectedSlot) {
                        inventory.selectedSlot = i;
                        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(i));
                    }
                    found = true;
                    break;
                }
            }
            // If the item was pushed entirely out of the hotbar (to slots 9-35),
            // smartly switch them to slot 0 (the primary tool for the new activity).
            if (!found && newActivity != ActivityType.GENERAL) {
                inventory.selectedSlot = 0;
                player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(0));
            }
        } else {
            // If they were holding an empty hand, and we moved to a specific activity, select slot 0
            if (newActivity != ActivityType.GENERAL) {
                inventory.selectedSlot = 0;
                player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(0));
            }
        }

        // Sync with client
        player.playerScreenHandler.syncState();
    }

    private static int findBestItem(PlayerInventory inventory, String type, Set<Integer> lockedSlots) {
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 36; i++) {
            if (lockedSlots.contains(i)) continue; // Don't steal from already correct slots

            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            int score = evaluateItem(stack.getItem(), type);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private static int evaluateItem(Item item, String type) {
        String id = net.minecraft.registry.Registries.ITEM.getId(item).toString();
        switch (type) {
            case "SWORD":
                if (item instanceof SwordItem) return 100 + ((SwordItem)item).getMaterial().getMiningLevel() * 10;
                if (item instanceof AxeItem) return 50 + ((AxeItem)item).getMaterial().getMiningLevel() * 10;
                if (item instanceof TridentItem) return 40;
                break;
            case "PICKAXE":
                if (item instanceof PickaxeItem) return 100 + ((PickaxeItem)item).getMaterial().getMiningLevel() * 10;
                break;
            case "AXE":
                if (item instanceof AxeItem) return 100 + ((AxeItem)item).getMaterial().getMiningLevel() * 10;
                break;
            case "SHOVEL":
                if (item instanceof ShovelItem) return 100 + ((ShovelItem)item).getMaterial().getMiningLevel() * 10;
                break;
            case "HOE":
                if (item instanceof HoeItem) return 100 + ((HoeItem)item).getMaterial().getMiningLevel() * 10;
                break;
            case "BOW":
                if (item instanceof BowItem) return 100;
                if (item instanceof CrossbowItem) return 90;
                if (item instanceof TridentItem) return 80;
                if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) return 50;
                break;
            case "SHIELD":
                if (item instanceof ShieldItem) return 100;
                break;
            case "FOOD":
                if (item.getFoodComponent() != null) {
                    return item.getFoodComponent().getHunger() * 10;
                }
                if (item instanceof PotionItem) return 10; // Potions as fallback
                break;
            case "BLOCK":
                if (item instanceof BlockItem) {
                    // Prefer full solid blocks for building
                    if (!id.contains("slab") && !id.contains("stairs") && !id.contains("wall")) return 100;
                    return 50;
                }
                break;
            case "TORCH":
                if (id.contains("torch") || id.contains("lantern") || id.contains("glowstone")) return 100;
                break;
            case "SEEDS":
                if (item instanceof AliasedBlockItem) return 100; // Carrots/Potatoes
                if (id.contains("seeds")) return 90;
                if (item instanceof BoneMealItem) return 50; // Bone meal as farming backup
                break;
            case "WATER_BUCKET":
                if (id.equals("minecraft:water_bucket")) return 100;
                if (item instanceof BucketItem) return 50; // Empty bucket as backup
                break;
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
