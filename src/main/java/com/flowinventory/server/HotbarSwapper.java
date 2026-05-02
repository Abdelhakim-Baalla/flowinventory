package com.flowinventory.server;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import com.flowinventory.profiles.HotbarPreset;
import com.flowinventory.profiles.ProfileManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Re-arranges the player's hotbar to match a {@link HotbarPreset} for the
 * currently detected {@link ActivityType}. Always tries to:
 *
 * <ol>
 *   <li>place the activity's primary tool somewhere on the hotbar
 *       (preferring the player's currently selected slot to avoid
 *       yanking what they were using),</li>
 *   <li>fill secondary preset slots with the best matching items,</li>
 *   <li>install the preset's off-hand item (typically a SHIELD), and</li>
 *   <li>switch the held slot to the primary tool ONLY if the slot the
 *       player has selected doesn't already contain a usable item for
 *       this activity.</li>
 * </ol>
 */
public class HotbarSwapper {

    /** Off-hand slot index inside {@link PlayerInventory#main}-relative API. */
    private static final int OFFHAND_SLOT_INDEX = 0; // PlayerInventory#offHand has size 1

    public static void swapHotbar(ServerPlayerEntity player, ActivityType newActivity) {
        if (player == null || newActivity == null) return;

        HotbarPreset preset = ProfileManager.getPreset(newActivity);

        // Empty preset → only consider selecting a more appropriate slot
        // (and only if the preset explicitly allows it).
        if (preset == null || preset.slots == null || preset.slots.isEmpty()) {
            if (preset == null || preset.allowSlotOverride) {
                ensurePrimarySlotSelected(player, newActivity, null);
            }
            return;
        }

        PlayerInventory inventory = player.getInventory();
        int playerSlot = inventory.selectedSlot;
        ItemStack originallyHeld = inventory.getStack(playerSlot).copy();
        String primaryType = preset.slots.get(0);

        // ── Step 1: if the player is holding a usable primary item, lock it
        //            in their current slot so we don't yank it away.
        boolean playerHoldsValidPrimary = primaryType != null
                && !originallyHeld.isEmpty()
                && ItemHeuristics.evaluate(originallyHeld, primaryType) > 0;

        Set<Integer> lockedSlots = new HashSet<>();
        if (playerHoldsValidPrimary) lockedSlots.add(playerSlot);

        int swapsPerformed = 0;
        boolean foundAnyMatch = false;

        // ── Step 2: arrange every preset slot (0→8 only — HashMap iteration order is undefined)
        for (int targetSlot = 0; targetSlot < 9; targetSlot++) {
            if (!preset.slots.containsKey(targetSlot)) continue;
            String desiredType = preset.slots.get(targetSlot);

            if (lockedSlots.contains(targetSlot)) continue;

            ItemStack currentInTarget = inventory.getStack(targetSlot);
            int currentScore = currentInTarget.isEmpty()
                    ? -1
                    : ItemHeuristics.evaluate(currentInTarget, desiredType);
            if (currentScore > 0) foundAnyMatch = true;
            if (!currentInTarget.isEmpty()
                    && ItemStack.canCombine(currentInTarget, originallyHeld)
                    && currentScore > 0) {
                currentScore += 1000;
            }

            int bestSlot = findBestItem(inventory, desiredType, lockedSlots, originallyHeld);
            if (bestSlot == -1) {
                if (currentScore > 0) lockedSlots.add(targetSlot);
                continue;
            }

            int bestScore = ItemHeuristics.evaluate(inventory.getStack(bestSlot), desiredType);
            if (bestScore > 0) foundAnyMatch = true;
            if (ItemStack.canCombine(inventory.getStack(bestSlot), originallyHeld) && bestScore > 0) {
                bestScore += 1000;
            }

            if (bestScore > currentScore && bestSlot != targetSlot) {
                ItemStack newStack = inventory.getStack(bestSlot).copy();

                if (!currentInTarget.isEmpty()) {
                    int emptyHotbar = getEmptyHotbarSlot(inventory, lockedSlots, preset.slots.keySet(), playerSlot);
                    if (emptyHotbar != -1) {
                        inventory.setStack(emptyHotbar, currentInTarget);
                        inventory.setStack(targetSlot, newStack);
                        inventory.setStack(bestSlot, ItemStack.EMPTY);
                    } else {
                        inventory.setStack(targetSlot, newStack);
                        inventory.setStack(bestSlot, currentInTarget);
                    }
                } else {
                    inventory.setStack(targetSlot, newStack);
                    inventory.setStack(bestSlot, ItemStack.EMPTY);
                }
                lockedSlots.add(targetSlot);
                swapsPerformed++;
            } else if (currentScore > 0) {
                lockedSlots.add(targetSlot);
            }
        }

        // ── Step 3: off-hand
        if (preset.offHandType != null) {
            placeInOffHand(player, preset.offHandType);
        }

        // ── Step 4: select the right slot (respecting player's choice)
        if (preset.allowSlotOverride && foundAnyMatch) {
            ensurePrimarySlotSelected(player, newActivity, preset);
        }

        // ── Step 5: tidy up — merge any partial stacks the swap created
        if (swapsPerformed > 0) {
            mergeStacksInPlace(inventory);
        }

        if (!foundAnyMatch) {
            FlowInventoryMod.LOGGER.debug(
                    "[FlowInventory] Activity {} requested but no matching items in inventory \u2014 leaving hotbar untouched",
                    newActivity.displayName
            );
        }

        player.playerScreenHandler.syncState();
    }

    /**
     * Best-effort consolidation pass — combines any partial stacks of the
     * same item that the swap may have created. Doesn't move things across
     * categories, only collapses fragmentation.
     */
    private static void mergeStacksInPlace(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack a = inventory.getStack(i);
            if (a.isEmpty() || a.getCount() >= a.getMaxCount()) continue;
            for (int j = i + 1; j < 36; j++) {
                ItemStack b = inventory.getStack(j);
                if (b.isEmpty()) continue;
                if (!ItemStack.canCombine(a, b)) continue;
                int space = a.getMaxCount() - a.getCount();
                int move = Math.min(space, b.getCount());
                if (move <= 0) continue;
                a.increment(move);
                b.decrement(move);
                if (b.isEmpty()) inventory.setStack(j, ItemStack.EMPTY);
                if (a.getCount() >= a.getMaxCount()) break;
            }
        }
    }

    /**
     * Finds the best matching item in the inventory for {@code type} and
     * places it in the off-hand. The current off-hand item swaps into the
     * source slot.
     */
    private static void placeInOffHand(ServerPlayerEntity player, String type) {
        PlayerInventory inv = player.getInventory();
        ItemStack currentOffHand = inv.offHand.get(OFFHAND_SLOT_INDEX);

        int currentScore = currentOffHand.isEmpty()
                ? -1
                : ItemHeuristics.evaluate(currentOffHand, type);

        int bestSlot = -1;
        int bestScore = currentScore;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            int score = ItemHeuristics.evaluate(stack, type);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) return;

        ItemStack newOff = inv.getStack(bestSlot).copy();
        if (currentOffHand.isEmpty()) {
            inv.setStack(bestSlot, ItemStack.EMPTY);
        } else {
            inv.setStack(bestSlot, currentOffHand.copy());
        }
        inv.offHand.set(OFFHAND_SLOT_INDEX, newOff);
    }

    /**
     * Force the player's held slot to the slot containing the activity's
     * primary tool — but only if the player isn't already holding something
     * usable for this activity.
     */
    private static void ensurePrimarySlotSelected(ServerPlayerEntity player,
                                                  ActivityType activity,
                                                  HotbarPreset preset) {
        PlayerInventory inventory = player.getInventory();
        int currentSlot = inventory.selectedSlot;
        ItemStack currentHeld = inventory.getStack(currentSlot);

        String primaryType = preset != null && preset.slots != null
                ? preset.slots.get(0)
                : null;
        if (primaryType == null) primaryType = getPrimaryItemTypeForActivity(activity);

        // If what the player is already holding is a valid primary item,
        // KEEP that slot — don't yank it (this fixes the "scrolled item gets
        // yanked back to slot 0" complaint).
        if (primaryType != null && !currentHeld.isEmpty()
                && ItemHeuristics.evaluate(currentHeld, primaryType) > 0) {
            return;
        }

        int targetSlot = -1;

        if (preset != null && preset.slots != null) {
            String slot0Type = preset.slots.get(0);
            if (slot0Type != null) {
                ItemStack slot0 = inventory.getStack(0);
                if (!slot0.isEmpty() && ItemHeuristics.evaluate(slot0, slot0Type) > 0) {
                    targetSlot = 0;
                }
            }
        }

        if (targetSlot == -1) {
            int found = findBestHotbarSlotForType(inventory, primaryType);
            if (found != -1) targetSlot = found;
        }

        if (targetSlot == -1 && preset != null && preset.slots != null) {
            for (int i = 0; i < 9; i++) {
                if (preset.slots.containsKey(i) && !inventory.getStack(i).isEmpty()) {
                    targetSlot = i;
                    break;
                }
            }
        }

        if (targetSlot < 0 || targetSlot >= 9) return;
        if (targetSlot == inventory.selectedSlot) return;

        inventory.selectedSlot = targetSlot;
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(targetSlot));
    }

    private static int findBestHotbarSlotForType(PlayerInventory inventory, String type) {
        if (type == null) return -1;
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            int score = ItemHeuristics.evaluate(stack, type);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static int findBestItem(PlayerInventory inventory, String type,
                                    Set<Integer> lockedSlots, ItemStack heldItem) {
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < 36; i++) {
            if (lockedSlots.contains(i)) continue;
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            int score = ItemHeuristics.evaluate(stack, type);
            if (score > 0 && ItemStack.canCombine(stack, heldItem)) {
                score += 1000;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static int getEmptyHotbarSlot(PlayerInventory inventory, Set<Integer> lockedSlots,
                                          Set<Integer> presetSlots, int playerSlot) {
        for (int i = 0; i < 9; i++) {
            if (i == playerSlot) continue; // never overwrite the slot the player is using
            if (inventory.getStack(i).isEmpty()
                    && !lockedSlots.contains(i)
                    && !presetSlots.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Fallback when the preset has no slot 0: matches the first slot of
     * {@link ProfileManager}'s built-in presets for each {@link ActivityType}.
     */
    private static String getPrimaryItemTypeForActivity(ActivityType activity) {
        switch (activity) {
            case COMBAT:
            case EMERGENCY_COMBAT:
                return "SWORD";
            case MINING:
                return "PICKAXE";
            case BUILDING:
                return "BLOCK";
            case FARMING:
                return "HOE";
            case REDSTONE:
                return "REDSTONE";
            case CRAFTING:
                return "BOOK";
            case EXPLORATION:
                return "COMPASS";
            case TRAVEL:
                return "ELYTRA";
            case UTILITY:
                return "TOOL";
            case FOOD:
                return "FOOD";
            case LOW_HEALTH:
                return "GOLDEN_APPLE";
            case LOW_HUNGER:
                return "STEAK";
            case ON_FIRE:
            case IN_LAVA:
                return "WATER_BUCKET";
            case DROWNING:
                return "BUCKET";
            case FALLING:
                return "ELYTRA";
            case POISONED:
            case WITHERING:
                return "MILK_BUCKET";
            case SLEEPING:
                return "BED";
            case IDLE:
                return null;
            case GENERAL:
            case UNKNOWN:
            default:
                return "SWORD";
        }
    }
}
