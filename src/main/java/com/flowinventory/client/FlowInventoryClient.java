package com.flowinventory.client;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.core.InventoryManager;
import com.flowinventory.core.InventoryScanner;
import com.flowinventory.network.NetworkHandler;
import com.flowinventory.profiles.ActivityCycle;
import com.flowinventory.profiles.ActivityType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FlowInventoryClient implements ClientModInitializer {
    
    public static KeyBinding KEY_SORT;
    public static KeyBinding KEY_NEXT_PROFILE;
    public static KeyBinding KEY_PREV_PROFILE;
    public static KeyBinding KEY_TOGGLE_AUTO;
    
    public static InventoryManager inventoryManager;
    
    @Override
    public void onInitializeClient() {
        FlowInventoryMod.LOGGER.info("[FlowInventory] Client initializing v2.0 - Advanced AI Edition...");
        
        // Register keybinds
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
        
        KEY_PREV_PROFILE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.flowinventory.prev_profile",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.flowinventory.main"
        ));
        
        KEY_TOGGLE_AUTO = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.flowinventory.toggle_auto",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.flowinventory.main"
        ));
        
        // Initialize managers
        inventoryManager = new InventoryManager();
        
        // Tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            // Update activity detector
            FlowInventoryMod.activityDetector.tick(client.player);
            
            // Handle sort keybind
            while (KEY_SORT.wasPressed()) {
                NetworkHandler.sendSortRequest();
                FlowInventoryMod.LOGGER.info("[FlowInventory] Sort packet sent!");
            }
            
            // Next profile (G) — smart cycle that skips activities the player has no items for
            while (KEY_NEXT_PROFILE.wasPressed()) {
                cycleProfile(client.player, true);
            }

            // Previous profile (V) — same smart cycle, reversed
            while (KEY_PREV_PROFILE.wasPressed()) {
                cycleProfile(client.player, false);
            }
            
            // Toggle auto-detect (B)
            while (KEY_TOGGLE_AUTO.wasPressed()) {
                FlowInventoryMod.config.autoDetectActivity = !FlowInventoryMod.config.autoDetectActivity;
                FlowInventoryMod.config.save();
                client.player.sendMessage(
                    Text.literal("Auto-detect: " + (FlowInventoryMod.config.autoDetectActivity ? "ON" : "OFF"))
                        .styled(style -> style.withColor(FlowInventoryMod.config.autoDetectActivity ? 0x44FF44 : 0xFF4444)),
                    true
                );
            }
        });
        
        // Sort key inside inventory screen
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof InventoryScreen) {
                ScreenKeyboardEvents.beforeKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                    if (KEY_SORT.matchesKey(key, scancode)) {
                        NetworkHandler.sendSortRequest();
                        FlowInventoryMod.LOGGER.info("[FlowInventory] Sort packet sent (from inventory screen)!");
                    }
                });
            }
        });
        
        FlowInventoryMod.LOGGER.info("[FlowInventory] Client ready with advanced detection!");
        
        // HUD Renderer
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT
            .register((context, tickDelta) -> {
                new FlowHudRenderer().render(context, tickDelta);
            });
    }

    /**
     * Walks the curated cycle list in the requested direction and stops at
     * the first activity for which the player owns at least the primary
     * item. Falls back to GENERAL if literally nothing matches.
     */
    private static void cycleProfile(PlayerEntity player, boolean forward) {
        if (player == null) return;

        ActivityType current = FlowInventoryMod.activityDetector.getCurrentActivity();
        int size = ActivityCycle.DEFAULT.size();
        int startIdx = ActivityCycle.indexOf(current);
        if (startIdx == -1) startIdx = forward ? -1 : 0;

        ActivityType picked = null;
        for (int i = 1; i <= size; i++) {
            int idx = forward
                    ? (startIdx + i + size) % size
                    : (startIdx - i + size) % size;
            ActivityType candidate = ActivityCycle.DEFAULT.get(idx);
            if (candidate == current) continue;
            if (InventoryScanner.canUseActivity(player, candidate)) {
                picked = candidate;
                break;
            }
        }

        if (picked == null) {
            // Nothing else in the cycle is usable.
            //   - If the player is already on a usable activity, just say so.
            //   - Otherwise degrade to GENERAL.
            if (InventoryScanner.canUseActivity(player, current)) {
                int matches = InventoryScanner.countMatchingPresetSlots(player, current);
                player.sendMessage(
                        Text.literal("\u2192 " + current.icon + " " + current.displayName
                                + " is the only usable profile (" + matches + " items)")
                                .styled(s -> s.withColor(0xFFAA00)),
                        true
                );
                return;
            }
            picked = ActivityType.GENERAL;
            if (current == ActivityType.GENERAL) {
                player.sendMessage(
                        Text.literal("\u26A0 No usable profile found in your inventory")
                                .styled(s -> s.withColor(0xFFAA00)),
                        true
                );
                return;
            }
        }

        FlowInventoryMod.activityDetector.forceSetActivity(picked);

        int matches = InventoryScanner.countMatchingPresetSlots(player, picked);
        String label = "Profile: " + picked.icon + " " + picked.displayName
                + " (" + matches + " items ready)";
        player.sendMessage(
                Text.literal(label).styled(s -> s.withColor(0x44FF44)),
                true
        );
    }
}
