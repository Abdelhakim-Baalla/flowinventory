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
                    // يشتغل على الـ server thread
                    server.execute(() -> sortInventory(player));
                }
        );
    }

    private static void sortInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();

        // جمع items من slots 9-35 (main inventory)
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
                inventory.setStack(slot, ItemStack.EMPTY);
            }
        }

        if (items.isEmpty()) return;

        // ترتيب
        items.sort((a, b) -> {
            int catA = getCategoryOrder(a.getItem());
            int catB = getCategoryOrder(b.getItem());
            if (catA != catB) return catA - catB;
            return a.getName().getString()
                    .compareTo(b.getName().getString());
        });

        // كتابة back
        int slot = 9;
        for (ItemStack stack : items) {
            inventory.setStack(slot++, stack);
        }

        // sync مع الـ client تلقائياً لأننا على الـ server
        player.playerScreenHandler.syncState();

        // رسالة للاعب
        player.sendMessage(
                net.minecraft.text.Text.literal("✓ Inventory sorted!")
                        .formatted(net.minecraft.util.Formatting.GREEN),
                true
        );
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