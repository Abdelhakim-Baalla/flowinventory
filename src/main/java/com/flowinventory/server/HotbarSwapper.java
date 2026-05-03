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
 * Applies a hotbar layout for an {@link ActivityType} in two clear phases:
 * <ol>
 *   <li><b>Layout</b> — For each preset slot 0→8, place the best matching stack
 *       from the inventory (deterministic order).</li>
 *   <li><b>Main hand</b> — Select the first slot in index order whose contents
 *       actually match that slot’s preset type (slot 0 first). That way
 *       “Building” reliably puts a block in hand when blocks were placed in
 *       slot 0, instead of leaving a pickaxe selected from a previous mode.</li>
 * </ol>
 * Older versions tried to “respect” the held item with locks and combine
 * bonuses; that produced unrelated items staying selected after G / auto-detect.
 */
public final class HotbarSwapper {

    private static final int OFFHAND_SLOT_INDEX = 0;

    /** Ignore weak heuristic matches so e.g. snacks do not occupy tool slots. */
    private static final int PRESET_FIT_MIN_SCORE = 48;

    private HotbarSwapper() {}

    public static void swapHotbar(ServerPlayerEntity player, ActivityType newActivity) {
        if (player == null || newActivity == null) return;

        HotbarPreset preset = ProfileManager.getPreset(newActivity);

        if (preset == null || preset.slots == null || preset.slots.isEmpty()) {
            if (preset == null || preset.allowSlotOverride) {
                selectSlotForPrimaryTypeOnly(player, newActivity, null);
            }
            player.playerScreenHandler.syncState();
            return;
        }

        PlayerInventory inventory = player.getInventory();
        Set<Integer> lockedSlots = new HashSet<>();
        int swapsPerformed = 0;
        boolean foundAnyMatch = false;

        for (int targetSlot = 0; targetSlot < 9; targetSlot++) {
            if (!preset.slots.containsKey(targetSlot)) continue;
            String desiredType = preset.slots.get(targetSlot);
            if (desiredType == null) continue;

            if (lockedSlots.contains(targetSlot)) continue;

            ItemStack currentInTarget = inventory.getStack(targetSlot);
            int currentScore = currentInTarget.isEmpty()
                    ? -1
                    : ItemHeuristics.evaluate(currentInTarget, desiredType);
            if (currentScore >= PRESET_FIT_MIN_SCORE) foundAnyMatch = true;

            int bestSlot = findBestInventorySlotForType(inventory, desiredType, lockedSlots);
            if (bestSlot == -1 || ItemHeuristics.evaluate(inventory.getStack(bestSlot), desiredType) < PRESET_FIT_MIN_SCORE) {
                if (currentScore >= PRESET_FIT_MIN_SCORE) lockedSlots.add(targetSlot);
                continue;
            }

            int bestScore = ItemHeuristics.evaluate(inventory.getStack(bestSlot), desiredType);
            if (bestScore >= PRESET_FIT_MIN_SCORE) foundAnyMatch = true;

            if (bestScore >= PRESET_FIT_MIN_SCORE && bestScore > currentScore && bestSlot != targetSlot) {
                ItemStack newStack = inventory.getStack(bestSlot).copy();

                if (!currentInTarget.isEmpty()) {
                    int emptyHotbar = findDisplacementsHotbarSlot(
                            inventory, lockedSlots, preset.slots.keySet());
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
            } else if (currentScore >= PRESET_FIT_MIN_SCORE) {
                lockedSlots.add(targetSlot);
            }
        }

        if (preset.offHandType != null) {
            placeInOffHand(player, preset.offHandType);
        }

        if (preset.allowSlotOverride) {
            alignSelectedSlotToPreset(player, preset, newActivity);
        }

        if (swapsPerformed > 0) {
            mergeStacksInPlace(inventory);
        }

        if (!foundAnyMatch) {
            FlowInventoryMod.LOGGER.debug(
                    "[FlowInventory] Activity {} — no heuristic matches in inventory; hotbar unchanged except selection",
                    newActivity.displayName
            );
        }

        player.playerScreenHandler.syncState();
    }

    /**
     * Walk slots 0→8 in order; pick the first whose stack scores for its preset
     * type. Falls back to legacy primary-type search if preset slots are empty.
     */
    private static void alignSelectedSlotToPreset(ServerPlayerEntity player,
                                                   HotbarPreset preset,
                                                   ActivityType activity) {
        PlayerInventory inv = player.getInventory();

        for (int slot = 0; slot < 9; slot++) {
            if (preset.slots == null || !preset.slots.containsKey(slot)) continue;
            String want = preset.slots.get(slot);
            if (want == null) continue;

            if (deferSnackSlotForHandSelection(want, activity)) continue;

            ItemStack st = inv.getStack(slot);
            if (st.isEmpty()) continue;

            int ev = ItemHeuristics.evaluate(st, want);
            if (ev >= PRESET_FIT_MIN_SCORE) {
                applySelectedSlot(player, slot);
                return;
            }
        }

        selectSlotForPrimaryTypeOnly(player, activity, preset);
    }

    /**
     * Avoid selecting a food / snack lane when the profile is meant for tools
     * (mining still had “FOOD” on slots 5–8 — players ended on steak instead of a pick).
     */
    private static boolean deferSnackSlotForHandSelection(String presetType, ActivityType activity) {
        if (presetType == null) return false;
        if (activity == ActivityType.FOOD || activity == ActivityType.GENERAL) return false;
        // Emergency / vitals: golden apple & food slots are the point — never defer
        if (activity == ActivityType.LOW_HEALTH || activity == ActivityType.LOW_HUNGER
                || activity == ActivityType.ON_FIRE || activity == ActivityType.IN_LAVA
                || activity == ActivityType.DROWNING || activity == ActivityType.FALLING
                || activity == ActivityType.POISONED || activity == ActivityType.WITHERING
                || activity == ActivityType.EMERGENCY_COMBAT) {
            return false;
        }
        return switch (presetType) {
            case "FOOD", "GOLDEN_APPLE", "GOLDEN_CARROT", "STEAK", "BREAD", "COOKIE",
                    "HONEY_BOTTLE", "PUMPKIN_PIE", "BOWL" -> true;
            case "POTION" -> activity != ActivityType.COMBAT && activity != ActivityType.EMERGENCY_COMBAT;
            default -> false;
        };
    }

    /** When the preset has no slot map, move hand to the best hotbar match for the activity’s primary type. */
    private static void selectSlotForPrimaryTypeOnly(ServerPlayerEntity player,
                                                    ActivityType activity,
                                                    HotbarPreset preset) {
        PlayerInventory inv = player.getInventory();
        String primaryType = preset != null && preset.slots != null
                ? preset.slots.get(0)
                : null;
        if (primaryType == null) {
            primaryType = getPrimaryItemTypeForActivity(activity);
        }
        if (primaryType == null) return;

        ItemStack held = inv.getStack(inv.selectedSlot);
        if (!held.isEmpty() && ItemHeuristics.evaluate(held, primaryType) >= PRESET_FIT_MIN_SCORE) {
            return;
        }

        int targetSlot = -1;
        ItemStack s0 = inv.getStack(0);
        if (!s0.isEmpty() && ItemHeuristics.evaluate(s0, primaryType) >= PRESET_FIT_MIN_SCORE) {
            targetSlot = 0;
        }
        if (targetSlot < 0) {
            targetSlot = findBestHotbarSlotForType(inv, primaryType, PRESET_FIT_MIN_SCORE);
        }
        if (targetSlot < 0 && preset != null && preset.slots != null) {
            for (int i = 0; i < 9; i++) {
                if (!preset.slots.containsKey(i)) continue;
                String w = preset.slots.get(i);
                if (deferSnackSlotForHandSelection(w, activity)) continue;
                ItemStack st = inv.getStack(i);
                if (st.isEmpty() || w == null) continue;
                if (ItemHeuristics.evaluate(st, w) >= PRESET_FIT_MIN_SCORE) {
                    targetSlot = i;
                    break;
                }
            }
        }

        if (targetSlot >= 0 && targetSlot < 9 && targetSlot != inv.selectedSlot) {
            applySelectedSlot(player, targetSlot);
        }
    }

    private static void applySelectedSlot(ServerPlayerEntity player, int slot) {
        PlayerInventory inv = player.getInventory();
        if (slot == inv.selectedSlot) return;
        inv.selectedSlot = slot;
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(slot));
    }

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

    private static int findBestHotbarSlotForType(PlayerInventory inventory, String type, int minScore) {
        if (type == null) return -1;
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            int score = ItemHeuristics.evaluate(stack, type);
            if (score >= minScore && score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static int findBestInventorySlotForType(PlayerInventory inventory, String type,
                                                    Set<Integer> lockedSlots) {
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < 36; i++) {
            if (lockedSlots.contains(i)) continue;
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

    /**
     * Empty hotbar slot to park a displaced stack — any index not in the preset
     * and not locked is allowed (including the player’s current slot if empty).
     */
    private static int findDisplacementsHotbarSlot(PlayerInventory inventory, Set<Integer> lockedSlots,
                                                   Set<Integer> presetSlotIndices) {
        for (int i = 0; i < 9; i++) {
            if (inventory.getStack(i).isEmpty()
                    && !lockedSlots.contains(i)
                    && !presetSlotIndices.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    private static String getPrimaryItemTypeForActivity(ActivityType activity) {
        return switch (activity) {
            case COMBAT, EMERGENCY_COMBAT -> "SWORD";
            case MINING -> "PICKAXE";
            case BUILDING -> "BLOCK";
            case FARMING -> "HOE";
            case REDSTONE -> "REDSTONE";
            case CRAFTING -> "BOOK";
            case EXPLORATION -> "COMPASS";
            case TRAVEL -> "ELYTRA";
            case UTILITY -> "TOOL";
            case FOOD -> "FOOD";
            case LOW_HEALTH -> "GOLDEN_APPLE";
            case LOW_HUNGER -> "STEAK";
            case ON_FIRE, IN_LAVA -> "WATER_BUCKET";
            case DROWNING -> "BUCKET";
            case FALLING -> "ELYTRA";
            case POISONED, WITHERING -> "MILK_BUCKET";
            case SLEEPING -> "BED";
            case IDLE -> null;
            case GENERAL, UNKNOWN -> "SWORD";
        };
    }
}
