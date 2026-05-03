package com.flowinventory.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

public class FlowConfig {
    
    // ── Activity Detection ─────────────────────────────────────────
    public boolean autoDetectActivity = true;
    public boolean autoApplyProfile = true;
    public int activitySwitchDelay = 30;
    public int combatDetectionRange = 8;
    public boolean enableBiomeDetection = true;
    public boolean enableMobSpecificDetection = true;
    public boolean enableDimensionalContext = true;
    public int activityConfidenceThreshold = 60;
    
    // ── Advanced Detection Settings ───────────────────────────────
    public boolean enableArmorCheck = true;
    public boolean enableEnchantmentPriority = true;
    public boolean enableDurabilityAwareness = true;
    public boolean enableStackSizePreference = true;
    public boolean enableContextAwareness = true;
    
    // ── Sorting ────────────────────────────────────────────────────
    public String sortMode = "SMART"; // Options: "SMART", "ALPHABETICAL", "TIER"
    public boolean mergeStacks = true;
    public boolean lockHotbar = false;
    public boolean preserveHotbarOnSort = false;
    public boolean categoryColorCoding = false;
    
    // ── Hotbar Selection ───────────────────────────────────────────
    public boolean smartSlotSelection = true;
    public int primarySlotIndex = 0;
    public boolean cycleThroughDuplicates = false;
    
    // ── HUD ────────────────────────────────────────────────────────
    public boolean showHudOverlay = true;
    public String hudPosition = "TOP_RIGHT";
    public boolean showActivityMessages = true;
    public boolean showKeyHints = true;
    public boolean showItemRecommendations = false;
    public int hudOpacity = 180;
    
    // ── Pattern Learning ───────────────────────────────────────────
    public boolean enablePatternLearning = true;
    public int patternMemorySize = 1000;
    public boolean learnFromManualSwitches = true;
    
    // ── Persistence ────────────────────────────────────────────────
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("flowinventory");
    private static final String CONFIG_FILE = "flowinventory/config.json";
    
    public static FlowConfig load() {
        Path configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(CONFIG_FILE);
        
        if (!configPath.toFile().exists()) {
            LOGGER.info("[FlowInventory] No config found, creating defaults...");
            FlowConfig defaults = new FlowConfig();
            defaults.save();
            return defaults;
        }
        
        try (Reader reader = new FileReader(configPath.toFile())) {
            FlowConfig loaded = GSON.fromJson(reader, FlowConfig.class);
            LOGGER.info("[FlowInventory] Config loaded successfully.");
            return loaded != null ? loaded : new FlowConfig();
        } catch (IOException e) {
            LOGGER.error("[FlowInventory] Failed to load config!", e);
            return new FlowConfig();
        }
    }
    
    public void save() {
        Path configDir = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("flowinventory");
        
        configDir.toFile().mkdirs();
        
        Path configPath = configDir.resolve("config.json");
        
        try (Writer writer = new FileWriter(configPath.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("[FlowInventory] Failed to save config!", e);
        }
    }
}
