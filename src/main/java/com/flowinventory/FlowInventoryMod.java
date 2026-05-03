package com.flowinventory;

import com.flowinventory.config.FlowConfig;
import com.flowinventory.core.ActivityDetector;
import com.flowinventory.profiles.ActivityType;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flowinventory.server.FlowInventoryServer;

public class FlowInventoryMod implements ModInitializer {
    
    public static final String MOD_ID = "flowinventory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static FlowConfig config;
    public static ActivityDetector activityDetector;
    
    @Override
    public void onInitialize() {
        LOGGER.info("[FlowInventory] Initializing mod v2.0 - Advanced AI Edition...");
        
        config = FlowConfig.load();
        activityDetector = new ActivityDetector();
        
        LOGGER.info("[FlowInventory] Loaded {} activities, {} item categories", 
            ActivityType.values().length,
            com.flowinventory.server.ItemDatabase.getCategoryNames().size());
        
        FlowInventoryServer.register();
        
        LOGGER.info("[FlowInventory] Mod loaded! Auto-detect: {}, Auto-apply: {}", 
            config.autoDetectActivity, config.autoApplyProfile);
    }
}
