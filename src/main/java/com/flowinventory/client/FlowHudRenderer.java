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

    public void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!FlowInventoryMod.config.showHudOverlay) return;
        if (client.player == null) return;
        if (client.options.hudHidden) return;

        ActivityType activity = FlowInventoryMod.activityDetector.getCurrentActivity();

        String text = activity.icon + " " + activity.displayName;

        int screenWidth = context.getScaledWindowWidth();

        int x = screenWidth - BOX_WIDTH - MARGIN;
        int y = MARGIN + 2;

        // Draw background
        context.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, BG_COLOR);

        // Draw text
        context.drawText(
                client.textRenderer,
                text,
                x + 5,
                y + 4,
                0xFFFFFFFF,
                true
        );
    }
}