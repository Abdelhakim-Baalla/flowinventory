package com.flowinventory.client;

import com.flowinventory.FlowInventoryMod;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class FlowConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("FlowInventory Settings"));

        builder.setSavingRunnable(() -> {
            FlowInventoryMod.config.save();
        });

        ConfigCategory sorting = builder.getOrCreateCategory(Text.literal("Sorting"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        sorting.addEntry(entryBuilder.startSelector(
                        Text.literal("Sort Mode"),
                        new String[]{"SMART", "ALPHABETICAL"},
                        FlowInventoryMod.config.sortMode)
                .setDefaultValue("SMART")
                .setTooltip(Text.literal("SMART groups by item type. ALPHABETICAL is A-Z."))
                .setSaveConsumer(newValue -> FlowInventoryMod.config.sortMode = newValue)
                .build());

        sorting.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Lock Hotbar"),
                        FlowInventoryMod.config.lockHotbar)
                .setDefaultValue(false)
                .setTooltip(Text.literal("If true, slots 0-8 (hotbar) will not be sorted."))
                .setSaveConsumer(newValue -> FlowInventoryMod.config.lockHotbar = newValue)
                .build());

        sorting.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Merge Stacks"),
                        FlowInventoryMod.config.mergeStacks)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Merge scattered stacks of the same item before sorting."))
                .setSaveConsumer(newValue -> FlowInventoryMod.config.mergeStacks = newValue)
                .build());

        ConfigCategory activity = builder.getOrCreateCategory(Text.literal("Activity Detection"));

        activity.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Auto Detect Activity"),
                        FlowInventoryMod.config.autoDetectActivity)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> FlowInventoryMod.config.autoDetectActivity = newValue)
                .build());

        activity.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Show HUD Overlay"),
                        FlowInventoryMod.config.showHudOverlay)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> FlowInventoryMod.config.showHudOverlay = newValue)
                .build());

        return builder.build();
    }
}
