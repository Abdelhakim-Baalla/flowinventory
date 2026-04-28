package com.flowinventory;

import com.flowinventory.config.FlowConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowInventoryMod implements ModInitializer {

    public static final String MOD_ID = "flowinventory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static FlowConfig config;

    @Override
    public void onInitialize() {
        config = FlowConfig.load();
        LOGGER.info("[FlowInventory] Mod loaded! Auto-detect: " + config.autoDetectActivity);
    }
}