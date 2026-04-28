package com.flowinventory;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowInventoryMod implements ModInitializer {

    public static final String MOD_ID = "flowinventory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[FlowInventory] Mod loaded successfully!");
    }
}