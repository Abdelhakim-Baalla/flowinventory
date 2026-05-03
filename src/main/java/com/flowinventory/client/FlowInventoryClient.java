package com.flowinventory.client;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.core.InventoryManager;
import com.flowinventory.core.InventoryScanner;
import com.flowinventory.network.NetworkHandler;
import com.flowinventory.profiles.ActivityCycle;
import com.flowinventory.profiles.ActivityCycleRules;
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

    /**
     * One deliberate step per time window — {@code wasPressed()} can fire several times
     * per tick when OS key-repeat feeds the game, which produced log storms like 8×
     * "Activity manually set" from a single long press on G.
     */
    private static long lastProfileCycleAt;
    private static final long PROFILE_CYCLE_COOLDOWN_MS = 160;
    
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

            // Manual G/V must run before auto-detect tick(): otherwise the detector
            // can change currentActivity in the same frame and cycleProfile() reads
            // the wrong starting point (feels like "G skipped the wrong profile").

            long tickNow = System.currentTimeMillis();
            // Single consumption per tick + cooldown — never burst-cycle with key repeat
            if (KEY_NEXT_PROFILE.wasPressed()
                    && tickNow - lastProfileCycleAt >= PROFILE_CYCLE_COOLDOWN_MS) {
                lastProfileCycleAt = tickNow;
                cycleProfile(client.player, true);
            }
            if (KEY_PREV_PROFILE.wasPressed()
                    && tickNow - lastProfileCycleAt >= PROFILE_CYCLE_COOLDOWN_MS) {
                lastProfileCycleAt = tickNow;
                cycleProfile(client.player, false);
            }

            // Update activity detector (after manual picks so force-override applies same tick)
            FlowInventoryMod.activityDetector.tick(client.player);

            // Handle sort keybind
            if (KEY_SORT.wasPressed()) {
                NetworkHandler.sendSortRequest();
                FlowInventoryMod.LOGGER.info("[FlowInventory] Sort packet sent!");
            }
            
            // Toggle auto-detect (B)
            if (KEY_TOGGLE_AUTO.wasPressed()) {
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
     * G / V: walk the cycle in order and pick the next activity you can actually
     * outfit (see {@link ActivityCycleRules}) — not “any preset slot including FOOD”.
     */
    private static void cycleProfile(PlayerEntity player, boolean forward) {
        if (player == null) return;

        ActivityType current = FlowInventoryMod.activityDetector.getCurrentActivity();
        var order = ActivityCycle.DEFAULT;
        int n = order.size();
        int startIdx = ActivityCycle.indexOf(current);
        if (startIdx < 0) startIdx = 0;

        ActivityType picked = null;
        for (int step = 1; step <= n; step++) {
            int idx = forward
                    ? (startIdx + step) % n
                    : (startIdx - step + n) % n;
            ActivityType candidate = order.get(idx);
            if (candidate == current && step < n) continue;
            if (ActivityCycleRules.canOfferOnCycle(player, candidate)) {
                picked = candidate;
                break;
            }
        }

        if (picked == null) {
            player.sendMessage(
                    Text.literal("\u2192 No other profile in the cycle fits your inventory right now")
                            .styled(s -> s.withColor(0xFFAA00)),
                    true
            );
            return;
        }

        FlowInventoryMod.activityDetector.forceSetActivity(picked);

        int matches = InventoryScanner.countMatchingPresetSlots(player, picked);
        player.sendMessage(
                Text.literal("Profile: " + picked.icon + " " + picked.displayName
                        + " (" + matches + " preset types matched)")
                        .styled(s -> s.withColor(0x44FF44)),
                true
        );
    }
}
