package com.flowinventory.client;

import com.flowinventory.FlowInventoryMod;
import com.flowinventory.profiles.ActivityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FlowHudRenderer {

    private static final int BOX_WIDTH = 90;
    private static final int BOX_HEIGHT = 16;
    private static final int MARGIN = 4;
    private static final int BG_COLOR = 0x88000000;

    public void render(DrawContext context, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!FlowInventoryMod.config.showHudOverlay) return;
        if (client.player == null) return;
        if (client.options.hudHidden) return;

        ActivityType activity = FlowInventoryMod.activityDetector.getCurrentActivity();

        // While the player is inside the force-override window (just pressed
        // G/V) prepend a small lock + remaining seconds so they can see why
        // auto-detection isn't fighting them.
        boolean locked = FlowInventoryMod.activityDetector.isInForceOverride();
        int lockSeconds = locked
                ? FlowInventoryMod.activityDetector.getForceOverrideRemainingSeconds()
                : 0;

        String text = (locked ? "\uD83D\uDD12 " : "") + activity.icon + " " + activity.displayName
                + (locked ? " (" + lockSeconds + "s)" : "");

        int screenWidth = context.getScaledWindowWidth();

        int width = Math.max(BOX_WIDTH, client.textRenderer.getWidth(text) + 10);
        int x = screenWidth - width - MARGIN;
        int y = MARGIN + 2;

        // Draw background (slight tint when locked so the player notices)
        int bg = locked ? 0xAA222244 : BG_COLOR;
        context.fill(x, y, x + width, y + BOX_HEIGHT, bg);

        // Draw text
        context.drawText(
                client.textRenderer,
                text,
                x + 5,
                y + 4,
                locked ? 0xFFFFCC55 : 0xFFFFFFFF,
                true
        );
    }
}