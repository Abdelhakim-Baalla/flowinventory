package com.flowinventory.client;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.core.InventoryManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import com.flowinventory.network.SortInventoryPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class FlowInventoryClient implements ClientModInitializer {

    public static KeyBinding KEY_SORT;
    public static KeyBinding KEY_NEXT_PROFILE;

    public static InventoryManager inventoryManager;

    @Override
    public void onInitializeClient() {
        FlowInventoryMod.LOGGER.info("[FlowInventory] Client initializing...");

        // ── Register Keybinds ─────────────────────────────
        KEY_SORT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.flowinventory.sort",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.flowinventory.main"
        ));

        KEY_NEXT_PROFILE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.flowinventory.next_profile",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.flowinventory.main"
        ));

        // ── Initialize Managers ───────────────────────────
        inventoryManager = new InventoryManager();

        // ── Tick Event ────────────────────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Update activity detector every tick
            FlowInventoryMod.activityDetector.tick(client.player);

            // Handle sort keybind
            while (KEY_SORT.wasPressed()) {
                // أرسل packet للـ server بدل ما نعدل locally
                ClientPlayNetworking.send(
                        SortInventoryPacket.ID,
                        PacketByteBufs.empty()
                );
                FlowInventoryMod.LOGGER.info("[FlowInventory] Sort packet sent!");
            }

            // Handle profile cycle keybind (G)
            while (KEY_NEXT_PROFILE.wasPressed()) {
                FlowInventoryMod.activityDetector.forceSetActivity(
                        FlowInventoryMod.activityDetector.getCurrentActivity().next()
                );
                client.player.sendMessage(
                        net.minecraft.text.Text.literal(
                                "Profile: " + FlowInventoryMod.activityDetector.getCurrentActivity().displayName
                        ),
                        true
                );
            }
        });

        FlowInventoryMod.LOGGER.info("[FlowInventory] Client ready!");

        // ── HUD Renderer ──────────────────────────────────
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT
                .register((context, tickDelta) -> {
                    new FlowHudRenderer().render(context, tickDelta);
                });
    }
}