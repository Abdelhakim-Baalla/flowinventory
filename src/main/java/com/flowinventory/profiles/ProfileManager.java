package com.flowinventory.profiles;

import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive hotbar presets for all activity types.
 * Each preset defines the optimal item arrangement for that specific activity.
 */
public class ProfileManager {
    
    private static final Map<ActivityType, HotbarPreset> PRESETS = new HashMap<>();
    
    static {
        // ==================== COMBAT PRESETS ====================
        
        PRESETS.put(ActivityType.COMBAT, new HotbarPreset()
            .add(0, "SWORD")
            .add(1, "BOW")
            .add(2, "SHIELD")
            .add(3, "POTION")
            .add(4, "ENDER_PEARL")
            .add(5, "GOLDEN_APPLE")
            .add(6, "POTION")
            .add(7, "FIREWORK")
            .add(8, "FOOD")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.SWORD_COMBAT, new HotbarPreset()
            .add(0, "SWORD")
            .add(1, "SHIELD")
            .add(2, "POTION")
            .add(3, "GOLDEN_APPLE")
            .add(4, "END_CRYSTAL")
            .add(5, "FIRE_CHARGE")
            .add(6, "TNT")
            .add(7, "ENDER_PEARL")
            .add(8, "FOOD")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.AXE_COMBAT, new HotbarPreset()
            .add(0, "AXE_COMBAT")
            .add(1, "SHIELD")
            .add(2, "POTION")
            .add(3, "ENDER_PEARL")
            .add(4, "FIRE_CHARGE")
            .add(5, "TNT")
            .add(6, "GOLDEN_APPLE")
            .add(7, "POTION")
            .add(8, "FOOD")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.TRIDENT_COMBAT, new HotbarPreset()
            .add(0, "TRIDENT")
            .add(1, "POTION")
            .add(2, "ENDER_PEARL")
            .add(3, "TNT")
            .add(4, "POTION")
            .add(5, "WATER_BUCKET")
            .add(6, "FOOD")
            .add(7, "SHIELD")
            .add(8, "GOLDEN_APPLE"));
            
        PRESETS.put(ActivityType.ARCHERY, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "ARROW")
            .add(3, "ARROW")
            .add(4, "ARROW")
            .add(5, "ENDER_PEARL")
            .add(6, "POTION")
            .add(7, "SHIELD")
            .add(8, "FOOD")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.SNIPER, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "ARROW")
            .add(3, "ARROW")
            .add(4, "ARROW")
            .add(5, "CROSSBOW")
            .add(6, "FIREWORK")
            .add(7, "SPECTRAL_ARROW")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.EXPLOSIVES, new HotbarPreset()
            .add(0, "TNT")
            .add(1, "TNT_MINECART")
            .add(2, "FIRE_CHARGE")
            .add(3, "BREEZE_EGG")
            .add(4, "BED")
            .add(5, "POTION")
            .add(6, "ENDER_PEARL")
            .add(7, "WATER_BUCKET")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.POTION_COMBAT, new HotbarPreset()
            .add(0, "POTION")
            .add(1, "SPLASH_POTION")
            .add(2, "LINGERING_POTION")
            .add(3, "POTION")
            .add(4, "POTION")
            .add(5, "GOLDEN_APPLE")
            .add(6, "ENDER_PEARL")
            .add(7, "SWORD")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.CREEPER, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "ARROW")
            .add(3, "ENDER_PEARL")
            .add(4, "TNT")
            .add(5, "POTION")
            .add(6, "SHIELD")
            .add(7, "SWORD")
            .add(8, "FOOD")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.SKELETON, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "ARROW")
            .add(3, "ARROW")
            .add(4, "SWORD")
            .add(5, "SHIELD")
            .add(6, "ENDER_PEARL")
            .add(7, "POTION")
            .add(8, "FOOD")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.ZOMBIE, new HotbarPreset()
            .add(0, "SWORD")
            .add(1, "SHIELD")
            .add(2, "GOLDEN_APPLE")
            .add(3, "FOOD")
            .add(4, "TNT")
            .add(5, "POTION")
            .add(6, "BANNER")
            .add(7, "ENDER_PEARL")
            .add(8, "FOOD")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.SPIDER, new HotbarPreset()
            .add(0, "SWORD")
            .add(1, "BOW")
            .add(2, "ARROW")
            .add(3, "SHIELD")
            .add(4, "POTION")
            .add(5, "WEB")
            .add(6, "FIRE_CHARGE")
            .add(7, "TNT")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.ENDERMAN, new HotbarPreset()
            .add(0, "SWORD")
            .add(1, "ENDER_PEARL")
            .add(2, "PUMPKIN")
            .add(3, "BOW")
            .add(4, "ARROW")
            .add(5, "WATER_BUCKET")
            .add(6, "FOOD")
            .add(7, "POTION")
            .add(8, "ENDER_EYE"));
            
        PRESETS.put(ActivityType.BLAZE, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "ARROW")
            .add(3, "SNOW_BALL")
            .add(4, "POTION")
            .add(5, "FIRE_RESISTANCE")
            .add(6, "WATER_BUCKET")
            .add(7, "SWORD")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.WITCH, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "MILK_BUCKET")
            .add(3, "POTION")
            .add(4, "SWORD")
            .add(5, "FOOD")
            .add(6, "GOLDEN_APPLE")
            .add(7, "HEALING_POTION")
            .add(8, "STRENGTH_POTION"));
            
        PRESETS.put(ActivityType.GUARDIAN, new HotbarPreset()
            .add(0, "TRIDENT")
            .add(1, "POTION")
            .add(2, "POTION")
            .add(3, "WATER_BUCKET")
            .add(4, "RESPIRATION_HELMET")
            .add(5, "CONDUIT")
            .add(6, "FOOD")
            .add(7, "ENDER_PEARL")
            .add(8, "POTION"));
            
        PRESETS.put(ActivityType.SHULKER, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "POTION")
            .add(3, "SWORD")
            .add(4, "SHIELD")
            .add(5, "END_CRYSTAL")
            .add(6, "ENDER_PEARL")
            .add(7, "FOOD")
            .add(8, "GOLDEN_APPLE"));
            
        PRESETS.put(ActivityType.WITHER, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "ARROW")
            .add(3, "POTION")
            .add(4, "GOLDEN_APPLE")
            .add(5, "ENCHANTED_GOLDEN_APPLE")
            .add(6, "MILK_BUCKET")
            .add(7, "SWORD")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.DRAGON, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "ARROW")
            .add(3, "POTION")
            .add(4, "GOLDEN_APPLE")
            .add(5, "ENCHANTED_GOLDEN_APPLE")
            .add(6, "END_CRYSTAL")
            .add(7, "SWORD")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.RAID, new HotbarPreset()
            .add(0, "BOW")
            .add(1, "ARROW")
            .add(2, "SHIELD")
            .add(3, "POTION")
            .add(4, "GOLDEN_APPLE")
            .add(5, "FOOD")
            .add(6, "CROSSBOW")
            .add(7, "ENDER_PEARL")
            .add(8, "MILK_BUCKET")
            .offHand("SHIELD"));
            
        PRESETS.put(ActivityType.BATTLE, new HotbarPreset()
            .add(0, "SWORD")
            .add(1, "BOW")
            .add(2, "SHIELD")
            .add(3, "POTION")
            .add(4, "TNT")
            .add(5, "ENDER_PEARL")
            .add(6, "GOLDEN_APPLE")
            .add(7, "FIREWORK")
            .add(8, "FOOD")
            .offHand("SHIELD"));

        // ==================== MINING PRESETS ====================
        
        PRESETS.put(ActivityType.MINING, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "SHOVEL")
            .add(2, "TORCH")
            .add(3, "WATER_BUCKET")
            .add(4, "BLOCK")
            .add(5, "TORCH")
            .add(6, "PICKAXE")
            .add(7, "FOOD")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.ORE, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "TORCH")
            .add(2, "WATER_BUCKET")
            .add(3, "FOOD")
            .add(4, "SHOVEL")
            .add(5, "LADDER")
            .add(6, "BUCKET")
            .add(7, "FLINT_AND_STEEL")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.STONE, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "SHOVEL")
            .add(2, "TORCH")
            .add(3, "TORCH")
            .add(4, "WATER_BUCKET")
            .add(5, "LADDER")
            .add(6, "SCRAPE")
            .add(7, "FOOD")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.DIAMOND, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "TORCH")
            .add(2, "WATER_BUCKET")
            .add(3, "FOOD")
            .add(4, "LADDER")
            .add(5, "SHOVEL")
            .add(6, "BUCKET")
            .add(7, "TORCH")
            .add(8, "ENDER_PEARL"));
            
        PRESETS.put(ActivityType.ANCIENT_DEBRIS, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "TORCH")
            .add(2, "BUCKET")
            .add(3, "FOOD")
            .add(4, "BLAST_RESISTANT_BLOCK")
            .add(5, "LADDER")
            .add(6, "GOLDEN_APPLE")
            .add(7, "POTION")
            .add(8, "ENDER_PEARL"));

        // ==================== BUILDING PRESETS ====================
        
        PRESETS.put(ActivityType.BUILDING, new HotbarPreset()
            .add(0, "BLOCK")
            .add(1, "BLOCK")
            .add(2, "BLOCK")
            .add(3, "STAIRS")
            .add(4, "SLAB")
            .add(5, "AXE")
            .add(6, "PICKAXE")
            .add(7, "SHOVEL")
            .add(8, "FOOD"));

        PRESETS.put(ActivityType.WOODWORKING, new HotbarPreset()
            .add(0, "LOG")
            .add(1, "PLANKS")
            .add(2, "AXE")
            .add(3, "STAIRS")
            .add(4, "SLAB")
            .add(5, "DOOR")
            .add(6, "TRAPDOOR")
            .add(7, "FENCE")
            .add(8, "FOOD"));

        PRESETS.put(ActivityType.STONEMASONRY, new HotbarPreset()
            .add(0, "STONE")
            .add(1, "STONE_BRICKS")
            .add(2, "PICKAXE")
            .add(3, "STAIRS")
            .add(4, "SLAB")
            .add(5, "WALL")
            .add(6, "BUTTON")
            .add(7, "PRESSURE_PLATE")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.DECORATING, new HotbarPreset()
            .add(0, "TERRACOTTA")
            .add(1, "CONCRETE")
            .add(2, "CARPET")
            .add(3, "BANNER")
            .add(4, "GLASS")
            .add(5, "PAINTING")
            .add(6, "FLOWER")
            .add(7, "LEAVES")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.GLASSWORK, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "GLASS")
            .add(2, "STAINED_GLASS")
            .add(3, "GLASS_PANE")
            .add(4, "STAINED_GLASS_PANE")
            .add(5, "TORCH")
            .add(6, "BLOCK")
            .add(7, "FOOD")
            .add(8, "FOOD"));

        // ==================== FARMING PRESETS ====================
        
        PRESETS.put(ActivityType.FARMING, new HotbarPreset()
            .add(0, "HOE")
            .add(1, "SEEDS")
            .add(2, "WATER_BUCKET")
            .add(3, "BONE_MEAL")
            .add(4, "FOOD")
            .add(5, "SHEARS")
            .add(6, "LEAD")
            .add(7, "FOOD")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.ANIMAL_FARMING, new HotbarPreset()
            .add(0, "SHEARS")
            .add(1, "LEAD")
            .add(2, "SADDLE")
            .add(3, "HORSE_ARMOR")
            .add(4, "FOOD")
            .add(5, "BUCKET")
            .add(6, "MILK_BUCKET")
            .add(7, "NAME_TAG")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.CROP_FARMING, new HotbarPreset()
            .add(0, "HOE")
            .add(1, "SEEDS")
            .add(2, "WHEAT")
            .add(3, "CARROT")
            .add(4, "POTATO")
            .add(5, "BEETROOT")
            .add(6, "WATER_BUCKET")
            .add(7, "BONE_MEAL")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.TREE_FARMING, new HotbarPreset()
            .add(0, "AXE")
            .add(1, "AXE")
            .add(2, "TORCH")
            .add(3, "SAPLING")
            .add(4, "BONE_MEAL")
            .add(5, "SHEARS")
            .add(6, "LEAVES")
            .add(7, "FOOD")
            .add(8, "FOOD"));

        // ==================== EXPLORATION PRESETS ====================
        
        PRESETS.put(ActivityType.EXPLORING, new HotbarPreset()
            .add(0, "COMPASS")
            .add(1, "MAP")
            .add(2, "FOOD")
            .add(3, "TORCH")
            .add(4, "WATER_BUCKET")
            .add(5, "ENDER_PEARL")
            .add(6, "POTION")
            .add(7, "BOW")
            .add(8, "SWORD"));
            
        PRESETS.put(ActivityType.CAVING, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "TORCH")
            .add(2, "WATER_BUCKET")
            .add(3, "FOOD")
            .add(4, "SWORD")
            .add(5, "SHIELD")
            .add(6, "LANTERN")
            .add(7, "BUCKET")
            .add(8, "POTION"));
            
        PRESETS.put(ActivityType.NETHER_EXPLORE, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "WATER_BUCKET")
            .add(2, "POTION")
            .add(3, "FOOD")
            .add(4, "GOLDEN_APPLE")
            .add(5, "FIRE_RESISTANCE")
            .add(6, "BLAZE_ROD")
            .add(7, "ENDER_PEARL")
            .add(8, "SWORD"));
            
        PRESETS.put(ActivityType.END_EXPLORE, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "ENDER_PEARL")
            .add(2, "ENDER_EYE")
            .add(3, "WATER_BUCKET")
            .add(4, "FOOD")
            .add(5, "SWORD")
            .add(6, "GOLDEN_APPLE")
            .add(7, "POTION")
            .add(8, "ENDER_CHEST"));

        // ==================== REDSTONE PRESETS ====================
        
        PRESETS.put(ActivityType.REDSTONE, new HotbarPreset()
            .add(0, "REDSTONE")
            .add(1, "REDSTONE")
            .add(2, "REPEATER")
            .add(3, "COMPARATOR")
            .add(4, "PISTON")
            .add(5, "OBSERVER")
            .add(6, "LEVER")
            .add(7, "BUTTON")
            .add(8, "TORCH"));
            
        PRESETS.put(ActivityType.REDSTONE_LOGIC, new HotbarPreset()
            .add(0, "REDSTONE")
            .add(1, "REPEATER")
            .add(2, "COMPARATOR")
            .add(3, "OBSERVER")
            .add(4, "DAYLIGHT_SENSOR")
            .add(5, "TARGET")
            .add(6, "LEVER")
            .add(7, "BUTTON")
            .add(8, "TORCH"));
            
        PRESETS.put(ActivityType.REDSTONE_MACHINES, new HotbarPreset()
            .add(0, "PISTON")
            .add(1, "STICKY_PISTON")
            .add(2, "DISPENSER")
            .add(3, "DROPPER")
            .add(4, "HOPPER")
            .add(5, "HOPPER_MINECART")
            .add(6, "RAIL")
            .add(7, "TNT")
            .add(8, "REDSTONE"));

        // ==================== RIDING PRESETS ====================
        
        PRESETS.put(ActivityType.RIDING, new HotbarPreset()
            .add(0, "SADDLE")
            .add(1, "LEAD")
            .add(2, "HORSE_ARMOR")
            .add(3, "FOOD")
            .add(4, "CARROT_ON_A_STICK") 
            .add(5, "HELMET") // Actually horse armor slot is 0, armor 1-3, saddle 4
            .add(6, "CHESTPLATE")
            .add(7, "LEGGINGS")
            .add(8, "BOOTS"));
            
        PRESETS.put(ActivityType.HORSE_RIDING, new HotbarPreset()
            .add(0, "SADDLE")
            .add(1, "HORSE_ARMOR")
            .add(2, "LEAD")
            .add(3, "FOOD")
            .add(4, "NAME_TAG")
            .add(5, "BUCKET")
            .add(6, "ANVIL")
            .add(7, "ENCHANTED_BOOK")
            .add(8, "ENDER_EYE"));
            
        PRESETS.put(ActivityType.BOAT, new HotbarPreset()
            .add(0, "BOAT")
            .add(1, "OAR")
            .add(2, "COMPASS")
            .add(3, "MAP")
            .add(4, "FOOD")
            .add(5, "FISHING_ROD")
            .add(6, "TORCH")
            .add(7, "BUCKET")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.ELYTRA, new HotbarPreset()
            .add(0, "ELYTRA")
            .add(1, "FIREWORK")
            .add(2, "FIREWORK")
            .add(3, "FIREWORK")
            .add(4, "POTION")
            .add(5, "FOOD")
            .add(6, "ENDER_PEARL")
            .add(7, "MAP")
            .add(8, "COMPASS"));

        // ==================== UTILITY PRESETS ====================
        
        PRESETS.put(ActivityType.UTILITY, new HotbarPreset()
            .add(0, "TOOL")
            .add(1, "TOOL")
            .add(2, "BUCKET")
            .add(3, "WATER_BUCKET")
            .add(4, "LAVA_BUCKET")
            .add(5, "LEAD")
            .add(6, "SADDLE")
            .add(7, "FLINT_AND_STEEL")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.LIGHTING, new HotbarPreset()
            .add(0, "TORCH")
            .add(1, "TORCH")
            .add(2, "LANTERN")
            .add(3, "LANTERN")
            .add(4, "CANDLE")
            .add(5, "CANDLE")
            .add(6, "GLOWSTONE")
            .add(7, "FLINT_AND_STEEL")
            .add(8, "FIRE_CHARGE"));
            
        PRESETS.put(ActivityType.TELEPORT, new HotbarPreset()
            .add(0, "ENDER_PEARL")
            .add(1, "ENDER_EYE")
            .add(2, "CHORUS_FRUIT")
            .add(3, "ENDER_CHEST")
            .add(4, "SHULKER_BOX")
            .add(5, "ENDER_PEARL")
            .add(6, "ENDER_PEARL")
            .add(7, "ENDER_EYE")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.HEALING, new HotbarPreset()
            .add(0, "POTION")
            .add(1, "POTION")
            .add(2, "GOLDEN_APPLE")
            .add(3, "ENCHANTED_GOLDEN_APPLE")
            .add(4, "MILK_BUCKET")
            .add(5, "FOOD")
            .add(6, "FOOD")
            .add(7, "HONEY_BOTTLE")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.NUTRITION, new HotbarPreset()
            .add(0, "STEAK")
            .add(1, "GOLDEN_CARROT")
            .add(2, "BREAD")
            .add(3, "MELON")
            .add(4, "POTATO")
            .add(5, "COOKIE")
            .add(6, "CAKE")
            .add(7, "FOOD")
            .add(8, "FOOD"));

        // ==================== BREWING & ENCHANTING ====================
        
        PRESETS.put(ActivityType.BREWING, new HotbarPreset()
            .add(0, "BOTTLE")
            .add(1, "NETHER_WART")
            .add(2, "BLAZE_POWDER")
            .add(3, "BREWING_INGREDIENT")
            .add(4, "BREWING_INGREDIENT")
            .add(5, "GUNPOWDER")
            .add(6, "REDSTONE")
            .add(7, "GLOWSTONE")
            .add(8, "FOOD"));
            
        PRESETS.put(ActivityType.ENCHANTING, new HotbarPreset()
            .add(0, "BOOK")
            .add(1, "ENCHANTED_BOOK")
            .add(2, "LAPIS")
            .add(3, "EXPERIENCE_BOTTLE")
            .add(4, "DIAMOND")
            .add(5, "EMERALD")
            .add(6, "SWORD")
            .add(7, "PICKAXE")
            .add(8, "BOOK"));
            
        PRESETS.put(ActivityType.ALCHEMY, new HotbarPreset()
            .add(0, "POTION")
            .add(1, "SPLASH_POTION")
            .add(2, "LINGERING_POTION")
            .add(3, "TIP_ARROW")
            .add(4, "BLAZE_POWDER")
            .add(5, "GHAST_TEAR")
            .add(6, "DRAGON_BREATH")
            .add(7, "GUNPOWDER")
            .add(8, "FOOD"));

        // ==================== SPECIALTY MINING ====================
        
        PRESETS.put(ActivityType.NETHER_ORE, new HotbarPreset()
            .add(0, "PICKAXE")
            .add(1, "PICKAXE")
            .add(2, "BUCKET")
            .add(3, "WATER_BUCKET")
            .add(4, "FOOD")
            .add(5, "POTION")
            .add(6, "GOLDEN_APPLE")
            .add(7, "BLAZE_ROD")
            .add(8, "FIRESTANCE_POTION"));
            
        PRESETS.put(ActivityType.NETHER_FARMING, new HotbarPreset()
            .add(0, "HOE")
            .add(1, "NETHER_WART")
            .add(2, "SHEARS")
            .add(3, "BUCKET")
            .add(4, "FIRE_RESISTANCE")
            .add(5, "GOLDEN_APPLE")
            .add(6, "FOOD")
            .add(7, "BLAZE_ROD")
            .add(8, "FOOD"));

        // ==================== EXTENDED MOB-SPECIFIC PRESETS ====================

        PRESETS.put(ActivityType.WITHER_SKELETON, makeMobPreset("SWORD", "BOW", "POTION"));
        PRESETS.put(ActivityType.PIGLIN, makeMobPreset("GOLD_INGOT", "SWORD", "GOLDEN_APPLE"));
        PRESETS.put(ActivityType.PIGLIN_BRUTE, makeMobPreset("SWORD", "SHIELD", "POTION"));
        PRESETS.put(ActivityType.HOGLIN, makeMobPreset("SWORD", "WARPED_FUNGUS", "FOOD"));
        PRESETS.put(ActivityType.ZOGLIN, makeMobPreset("SWORD", "POTION", "SHIELD"));
        PRESETS.put(ActivityType.GHAST, makeMobPreset("BOW", "SHIELD", "FIRE_RESISTANCE"));
        PRESETS.put(ActivityType.MAGMA_CUBE, makeMobPreset("SWORD", "POTION", "FIRE_RESISTANCE"));
        PRESETS.put(ActivityType.SLIME, makeMobPreset("SWORD", "BUCKET", "FOOD"));
        PRESETS.put(ActivityType.PHANTOM, makeMobPreset("BOW", "ARROW", "MILK_BUCKET"));
        PRESETS.put(ActivityType.RAVAGER, makeMobPreset("SWORD", "SHIELD", "POTION"));
        PRESETS.put(ActivityType.PILLAGER, makeMobPreset("BOW", "SHIELD", "ARROW"));
        PRESETS.put(ActivityType.EVOKER, makeMobPreset("BOW", "POTION", "MILK_BUCKET"));
        PRESETS.put(ActivityType.VINDICATOR, makeMobPreset("SWORD", "SHIELD", "POTION"));
        PRESETS.put(ActivityType.VEX, makeMobPreset("SWORD", "SHIELD", "POTION"));
        PRESETS.put(ActivityType.DROWNED, makeMobPreset("SWORD", "TRIDENT", "POTION"));
        PRESETS.put(ActivityType.HUSK, makeMobPreset("SWORD", "MILK_BUCKET", "FOOD"));
        PRESETS.put(ActivityType.STRAY, makeMobPreset("SWORD", "BOW", "MILK_BUCKET"));
        PRESETS.put(ActivityType.SILVERFISH, makeMobPreset("SWORD", "TORCH", "FOOD"));
        PRESETS.put(ActivityType.ENDERMITE, makeMobPreset("SWORD", "TORCH", "FOOD"));
        PRESETS.put(ActivityType.BREEZE, makeMobPreset("WIND_CHARGE", "SHIELD", "BOW"));
        PRESETS.put(ActivityType.WARDEN, makeMobPreset("BOW", "ENDER_PEARL", "POTION"));
        PRESETS.put(ActivityType.PVP, makeMobPreset("SWORD", "SHIELD", "GOLDEN_APPLE"));
        PRESETS.put(ActivityType.MACE, makeMobPreset("MACE", "SHIELD", "GOLDEN_APPLE"));
        PRESETS.put(ActivityType.CROSSBOW_COMBAT, makeMobPreset("CROSSBOW", "ARROW", "FIREWORK"));
        PRESETS.put(ActivityType.DEFENSIVE, makeMobPreset("SHIELD", "SWORD", "GOLDEN_APPLE"));
        PRESETS.put(ActivityType.BERSERK, makeMobPreset("SWORD", "AXE_COMBAT", "GOLDEN_APPLE"));

        // ==================== EXTENDED MINING PRESETS ====================

        PRESETS.put(ActivityType.COAL_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.IRON_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.GOLD_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.COPPER_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.LAPIS_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.REDSTONE_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.AMETHYST_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.QUARTZ_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.DEEPSLATE_MINING, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.OBSIDIAN_MINING, makeMiningPreset("PICKAXE", "WATER_BUCKET"));
        PRESETS.put(ActivityType.SCULK_MINING, makeMiningPreset("HOE", "WOOL"));
        PRESETS.put(ActivityType.EMERALD, makeMiningPreset("PICKAXE", "TORCH"));
        PRESETS.put(ActivityType.NETHERRACK, makeMiningPreset("PICKAXE", "WATER_BUCKET"));

        // ==================== EXTENDED FARMING PRESETS ====================

        PRESETS.put(ActivityType.COW_FARMING, makeFarmPreset("BUCKET", "WHEAT", "MILK_BUCKET"));
        PRESETS.put(ActivityType.PIG_FARMING, makeFarmPreset("CARROT", "POTATO", "BEETROOT"));
        PRESETS.put(ActivityType.CHICKEN_FARMING, makeFarmPreset("WHEAT_SEEDS", "EGG", "FOOD"));
        PRESETS.put(ActivityType.SHEEP_FARMING, makeFarmPreset("SHEARS", "WHEAT", "WOOL"));
        PRESETS.put(ActivityType.BEE_FARMING, makeFarmPreset("GLASS_BOTTLE", "FLOWER", "HONEY_BOTTLE"));
        PRESETS.put(ActivityType.MUSHROOM_FARMING, makeFarmPreset("BONE_MEAL", "MUSHROOM", "BOWL"));
        PRESETS.put(ActivityType.KELP_FARMING, makeFarmPreset("KELP", "BUCKET", "FOOD"));
        PRESETS.put(ActivityType.BAMBOO_FARMING, makeFarmPreset("BAMBOO", "BONE_MEAL", "AXE"));
        PRESETS.put(ActivityType.SUGAR_CANE_FARMING, makeFarmPreset("SUGAR_CANE", "BUCKET", "BONE_MEAL"));
        PRESETS.put(ActivityType.BREEDING, makeFarmPreset("WHEAT", "CARROT", "SEEDS"));

        // ==================== FISHING / WATER PRESETS ====================

        PRESETS.put(ActivityType.FISHING, new HotbarPreset()
                .add(0, "FISHING_ROD")
                .add(1, "BOAT")
                .add(2, "BUCKET")
                .add(3, "FOOD")
                .add(4, "WATER_BUCKET")
                .add(5, "TORCH")
                .add(6, "ENCHANTED_FISHING_ROD")
                .add(7, "POTION")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.OCEAN_FISHING, new HotbarPreset()
                .add(0, "FISHING_ROD")
                .add(1, "BOAT")
                .add(2, "TRIDENT")
                .add(3, "POTION")
                .add(4, "BUCKET")
                .add(5, "RESPIRATION_HELMET")
                .add(6, "FOOD")
                .add(7, "BREAD")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.JUNK_FISHING, new HotbarPreset()
                .add(0, "FISHING_ROD")
                .add(1, "FISHING_ROD")
                .add(2, "FOOD")
                .add(3, "BUCKET")
                .add(4, "TORCH")
                .add(5, "BOOK")
                .add(6, "BOTTLE")
                .add(7, "ENCHANTED_FISHING_ROD")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.OCEAN, new HotbarPreset()
                .add(0, "TRIDENT")
                .add(1, "POTION")
                .add(2, "BUCKET")
                .add(3, "BOAT")
                .add(4, "FOOD")
                .add(5, "RESPIRATION_HELMET")
                .add(6, "DOOR")
                .add(7, "TORCH")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.SAILING, PRESETS.get(ActivityType.BOAT));
        PRESETS.put(ActivityType.DIVING, PRESETS.get(ActivityType.OCEAN));

        // ==================== EXTENDED BUILDING PRESETS ====================

        PRESETS.put(ActivityType.TERRACOTTA, makeBuildingPreset("TERRACOTTA"));
        PRESETS.put(ActivityType.CONCRETE, makeBuildingPreset("CONCRETE"));
        PRESETS.put(ActivityType.SCULKING, makeBuildingPreset("SCULK"));
        PRESETS.put(ActivityType.ROOFING, makeBuildingPreset("STAIRS"));
        PRESETS.put(ActivityType.FURNISHING, makeBuildingPreset("CARPET"));
        PRESETS.put(ActivityType.LANDSCAPING, new HotbarPreset()
                .add(0, "FLOWER")
                .add(1, "SAPLING")
                .add(2, "LEAVES")
                .add(3, "BONE_MEAL")
                .add(4, "DYE")
                .add(5, "SHOVEL")
                .add(6, "AXE")
                .add(7, "BLOCK")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.DECORATION_PAINTER, makeBuildingPreset("PAINTING"));
        PRESETS.put(ActivityType.DECORATION_BANNER, makeBuildingPreset("BANNER"));
        PRESETS.put(ActivityType.DECORATION_LIGHTS, makeBuildingPreset("LANTERN"));

        // ==================== CRAFTING / WORKBENCHES ====================

        PRESETS.put(ActivityType.SMITHING, makeCraftingPreset("NETHERITE_INGOT", "SMITHING_TEMPLATE", "DIAMOND"));
        PRESETS.put(ActivityType.ANVIL, makeCraftingPreset("ANVIL", "ENCHANTED_BOOK", "EXPERIENCE_BOTTLE"));
        PRESETS.put(ActivityType.GRINDSTONE, makeCraftingPreset("GRINDSTONE", "TOOL", "WEAPON"));
        PRESETS.put(ActivityType.STONECUTTER, makeCraftingPreset("STONECUTTER", "STONE", "BLOCK"));
        PRESETS.put(ActivityType.LOOM, makeCraftingPreset("LOOM", "BANNER_PATTERN", "BANNER"));
        PRESETS.put(ActivityType.CARTOGRAPHY, makeCraftingPreset("MAP", "PAPER", "GLASS_PANE"));
        PRESETS.put(ActivityType.COMPOSTING, makeCraftingPreset("COMPOSTER", "BONE_MEAL", "FOOD"));
        PRESETS.put(ActivityType.COOKING, makeCraftingPreset("FURNACE", "FOOD", "COAL"));
        PRESETS.put(ActivityType.SMELTING, makeCraftingPreset("FURNACE", "RAW_IRON", "COAL"));
        PRESETS.put(ActivityType.TRADING, makeCraftingPreset("EMERALD", "FOOD", "BOOK"));

        // ==================== EXTENDED EXPLORATION PRESETS ====================

        PRESETS.put(ActivityType.JUNGLE_EXPLORE, makeExplorePreset("MACHETE", "TORCH"));
        PRESETS.put(ActivityType.DESERT_EXPLORE, makeExplorePreset("WATER_BUCKET", "FOOD"));
        PRESETS.put(ActivityType.SNOWY_EXPLORE, makeExplorePreset("LEATHER_BOOTS", "TORCH"));
        PRESETS.put(ActivityType.SWAMP_EXPLORE, makeExplorePreset("BOAT", "POTION"));
        PRESETS.put(ActivityType.MOUNTAIN_EXPLORE, makeExplorePreset("PICKAXE", "POTION"));
        PRESETS.put(ActivityType.BADLANDS_EXPLORE, makeExplorePreset("WATER_BUCKET", "PICKAXE"));
        PRESETS.put(ActivityType.MUSHROOM_EXPLORE, makeExplorePreset("BOAT", "BUCKET"));
        PRESETS.put(ActivityType.FOREST_EXPLORE, makeExplorePreset("AXE", "FOOD"));
        PRESETS.put(ActivityType.PLAINS_EXPLORE, makeExplorePreset("HORSE_ARMOR", "SADDLE"));
        PRESETS.put(ActivityType.SAVANNA_EXPLORE, makeExplorePreset("SADDLE", "ROPE"));
        PRESETS.put(ActivityType.OCEAN_EXPLORE, PRESETS.get(ActivityType.OCEAN));
        PRESETS.put(ActivityType.DEEP_DARK, makeExplorePreset("WOOL", "ENDER_PEARL"));
        PRESETS.put(ActivityType.ANCIENT_CITY, makeExplorePreset("WOOL", "ENDER_PEARL"));
        PRESETS.put(ActivityType.STRONGHOLD, makeExplorePreset("ENDER_EYE", "POTION"));
        PRESETS.put(ActivityType.MONUMENT, makeExplorePreset("MILK_BUCKET", "TRIDENT"));
        PRESETS.put(ActivityType.MANSION, makeExplorePreset("BOW", "POTION"));
        PRESETS.put(ActivityType.FORTRESS, makeExplorePreset("FIRE_RESISTANCE", "BOW"));
        PRESETS.put(ActivityType.BASTION, makeExplorePreset("GOLD_INGOT", "FIRE_RESISTANCE"));
        PRESETS.put(ActivityType.END_CITY, makeExplorePreset("ENDER_PEARL", "POTION"));

        // ==================== DIMENSION-LEVEL PRESETS ====================

        PRESETS.put(ActivityType.OVERWORLD, PRESETS.get(ActivityType.GENERAL));
        PRESETS.put(ActivityType.NETHER, new HotbarPreset()
                .add(0, "PICKAXE")
                .add(1, "FIRE_RESISTANCE")
                .add(2, "FOOD")
                .add(3, "GOLDEN_APPLE")
                .add(4, "SWORD")
                .add(5, "ENDER_PEARL")
                .add(6, "WATER_BUCKET")
                .add(7, "BOW")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.NETHER_RESOURCES, new HotbarPreset()
                .add(0, "PICKAXE")
                .add(1, "PICKAXE")
                .add(2, "FIRE_RESISTANCE")
                .add(3, "GOLDEN_APPLE")
                .add(4, "FOOD")
                .add(5, "BUCKET")
                .add(6, "BLOCK")
                .add(7, "BLAZE_ROD")
                .add(8, "ENDER_PEARL"));
        PRESETS.put(ActivityType.END, new HotbarPreset()
                .add(0, "SWORD")
                .add(1, "BOW")
                .add(2, "ARROW")
                .add(3, "ENDER_PEARL")
                .add(4, "WATER_BUCKET")
                .add(5, "GOLDEN_APPLE")
                .add(6, "POTION")
                .add(7, "PICKAXE")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.END_STONE, new HotbarPreset()
                .add(0, "PICKAXE")
                .add(1, "ENDER_PEARL")
                .add(2, "BLOCK")
                .add(3, "FOOD")
                .add(4, "SWORD")
                .add(5, "WATER_BUCKET")
                .add(6, "GOLDEN_APPLE")
                .add(7, "POTION")
                .add(8, "BUCKET"));

        // ==================== EXTENDED REDSTONE PRESETS ====================

        PRESETS.put(ActivityType.REDSTONE_TRANSPORT, makeRedstonePreset("HOPPER", "RAIL"));
        PRESETS.put(ActivityType.OBSERVER, makeRedstonePreset("OBSERVER", "PISTON"));
        PRESETS.put(ActivityType.PISTON, makeRedstonePreset("PISTON", "STICKY_PISTON"));
        PRESETS.put(ActivityType.HOPPER, makeRedstonePreset("HOPPER", "CHEST"));
        PRESETS.put(ActivityType.DROPPER, makeRedstonePreset("DROPPER", "CHEST"));
        PRESETS.put(ActivityType.DISPENSER, makeRedstonePreset("DISPENSER", "ARROW"));

        // ==================== EXTENDED RIDING PRESETS ====================

        PRESETS.put(ActivityType.PIG_RIDING, new HotbarPreset()
                .add(0, "CARROT_ON_A_STICK")
                .add(1, "SADDLE")
                .add(2, "CARROT")
                .add(3, "FOOD")
                .add(4, "LEAD")
                .add(5, "TORCH")
                .add(6, "MAP")
                .add(7, "COMPASS")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.STRIDER_RIDING, new HotbarPreset()
                .add(0, "WARPED_FUNGUS_ON_A_STICK")
                .add(1, "SADDLE")
                .add(2, "WARPED_FUNGUS")
                .add(3, "FIRE_RESISTANCE")
                .add(4, "FOOD")
                .add(5, "LEAD")
                .add(6, "MAP")
                .add(7, "COMPASS")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.CAMEL_RIDING, new HotbarPreset()
                .add(0, "SADDLE")
                .add(1, "CACTUS")
                .add(2, "FOOD")
                .add(3, "WATER_BUCKET")
                .add(4, "MAP")
                .add(5, "COMPASS")
                .add(6, "TORCH")
                .add(7, "BOW")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.LLAMA_RIDING, new HotbarPreset()
                .add(0, "LEAD")
                .add(1, "CARPET")
                .add(2, "WHEAT")
                .add(3, "FOOD")
                .add(4, "MAP")
                .add(5, "COMPASS")
                .add(6, "BOW")
                .add(7, "TORCH")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.DONKEY_RIDING, PRESETS.get(ActivityType.HORSE_RIDING));
        PRESETS.put(ActivityType.MULE_RIDING, PRESETS.get(ActivityType.HORSE_RIDING));
        PRESETS.put(ActivityType.MINECART, new HotbarPreset()
                .add(0, "MINECART")
                .add(1, "RAIL")
                .add(2, "POWERED_RAIL")
                .add(3, "REDSTONE_TORCH")
                .add(4, "CHEST_MINECART")
                .add(5, "HOPPER_MINECART")
                .add(6, "FOOD")
                .add(7, "TORCH")
                .add(8, "PICKAXE"));
        PRESETS.put(ActivityType.RAILS, new HotbarPreset()
                .add(0, "RAIL")
                .add(1, "POWERED_RAIL")
                .add(2, "DETECTOR_RAIL")
                .add(3, "ACTIVATOR_RAIL")
                .add(4, "REDSTONE_TORCH")
                .add(5, "MINECART")
                .add(6, "PICKAXE")
                .add(7, "FOOD")
                .add(8, "TORCH"));
        PRESETS.put(ActivityType.PARACHUTE, PRESETS.get(ActivityType.ELYTRA));

        // ==================== UTILITY / MISC PRESETS ====================

        PRESETS.put(ActivityType.SADDLE, PRESETS.get(ActivityType.RIDING));
        PRESETS.put(ActivityType.TOOL, PRESETS.get(ActivityType.UTILITY));
        PRESETS.put(ActivityType.MAP, new HotbarPreset()
                .add(0, "MAP")
                .add(1, "FILLED_MAP")
                .add(2, "COMPASS")
                .add(3, "PAPER")
                .add(4, "GLASS_PANE")
                .add(5, "BOAT")
                .add(6, "FOOD")
                .add(7, "TORCH")
                .add(8, "BANNER"));
        PRESETS.put(ActivityType.COMPASS, new HotbarPreset()
                .add(0, "COMPASS")
                .add(1, "MAP")
                .add(2, "RECOVERY_COMPASS")
                .add(3, "LODESTONE_COMPASS")
                .add(4, "FOOD")
                .add(5, "TORCH")
                .add(6, "ENDER_PEARL")
                .add(7, "BOOK")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.CLOCK, new HotbarPreset()
                .add(0, "CLOCK")
                .add(1, "BED")
                .add(2, "TORCH")
                .add(3, "FOOD")
                .add(4, "BOOK")
                .add(5, "MAP")
                .add(6, "COMPASS")
                .add(7, "ENDER_PEARL")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.SPYGLASS, new HotbarPreset()
                .add(0, "SPYGLASS")
                .add(1, "MAP")
                .add(2, "COMPASS")
                .add(3, "FOOD")
                .add(4, "BOOK")
                .add(5, "ENDER_PEARL")
                .add(6, "BOAT")
                .add(7, "TORCH")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.FOOD, PRESETS.get(ActivityType.NUTRITION));
        PRESETS.put(ActivityType.REGENERATION, PRESETS.get(ActivityType.HEALING));
        PRESETS.put(ActivityType.FIREWORK, new HotbarPreset()
                .add(0, "FIREWORK")
                .add(1, "FIREWORK")
                .add(2, "FIREWORK")
                .add(3, "FIREWORK_STAR")
                .add(4, "GUNPOWDER")
                .add(5, "PAPER")
                .add(6, "ELYTRA")
                .add(7, "FOOD")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.BANNER, new HotbarPreset()
                .add(0, "BANNER")
                .add(1, "BANNER_PATTERN")
                .add(2, "LOOM")
                .add(3, "DYE")
                .add(4, "WOOL")
                .add(5, "STICK")
                .add(6, "BANNER")
                .add(7, "BANNER")
                .add(8, "BANNER"));
        PRESETS.put(ActivityType.SIGN, new HotbarPreset()
                .add(0, "OAK_SIGN")
                .add(1, "SPRUCE_SIGN")
                .add(2, "BIRCH_SIGN")
                .add(3, "DYE")
                .add(4, "GLOW_INK_SAC")
                .add(5, "INK_SAC")
                .add(6, "TORCH")
                .add(7, "BLOCK")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.BOOK, new HotbarPreset()
                .add(0, "BOOK")
                .add(1, "WRITABLE_BOOK")
                .add(2, "WRITTEN_BOOK")
                .add(3, "ENCHANTED_BOOK")
                .add(4, "BOOKSHELF")
                .add(5, "QUILL")
                .add(6, "INK_SAC")
                .add(7, "FOOD")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.WRITING, PRESETS.get(ActivityType.BOOK));
        PRESETS.put(ActivityType.LEASH, new HotbarPreset()
                .add(0, "LEAD")
                .add(1, "LEAD")
                .add(2, "LEAD")
                .add(3, "FENCE")
                .add(4, "WHEAT")
                .add(5, "CARROT")
                .add(6, "NAME_TAG")
                .add(7, "FOOD")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.ENDER, new HotbarPreset()
                .add(0, "ENDER_PEARL")
                .add(1, "ENDER_EYE")
                .add(2, "ENDER_CHEST")
                .add(3, "SHULKER_BOX")
                .add(4, "ELYTRA")
                .add(5, "FOOD")
                .add(6, "PICKAXE")
                .add(7, "BLOCK")
                .add(8, "POTION"));
        PRESETS.put(ActivityType.BUCKET_USE, new HotbarPreset()
                .add(0, "WATER_BUCKET")
                .add(1, "LAVA_BUCKET")
                .add(2, "BUCKET")
                .add(3, "MILK_BUCKET")
                .add(4, "POWDER_SNOW_BUCKET")
                .add(5, "AXOLOTL_BUCKET")
                .add(6, "FOOD")
                .add(7, "TORCH")
                .add(8, "FOOD"));
        PRESETS.put(ActivityType.SLEEPING, new HotbarPreset()
                .add(0, "BED")
                .add(1, "BLOCK")
                .add(2, "TORCH")
                .add(3, "FOOD")
                .add(4, "BOOK")
                .add(5, "MAP")
                .add(6, "COMPASS")
                .add(7, "ENDER_PEARL")
                .add(8, "FOOD"));
        // IDLE / AFK are intentionally no-ops: an empty preset combined with
        // noSlotOverride() means the swapper won't move items or change the
        // selected slot when the player is just standing around.
        PRESETS.put(ActivityType.AFK, new HotbarPreset().noSlotOverride());
        PRESETS.put(ActivityType.IDLE, new HotbarPreset().noSlotOverride());

        // ==================== EMERGENCY / VITAL PRESETS ====================

        // Drag a sword to slot 0, push a shield to off-hand, surround with safety
        PRESETS.put(ActivityType.EMERGENCY_COMBAT, new HotbarPreset()
                .add(0, "SWORD")
                .add(1, "GOLDEN_APPLE")
                .add(2, "POTION")
                .add(3, "ENDER_PEARL")
                .add(4, "MILK_BUCKET")
                .add(5, "BOW")
                .add(6, "ARROW")
                .add(7, "SHIELD")
                .add(8, "FOOD")
                .offHand("SHIELD"));

        PRESETS.put(ActivityType.LOW_HEALTH, new HotbarPreset()
                .add(0, "GOLDEN_APPLE")
                .add(1, "POTION")
                .add(2, "MILK_BUCKET")
                .add(3, "FOOD")
                .add(4, "ENDER_PEARL")
                .add(5, "SHIELD")
                .add(6, "SWORD")
                .add(7, "HONEY_BOTTLE")
                .add(8, "FOOD")
                .offHand("TOTEM"));

        PRESETS.put(ActivityType.LOW_HUNGER, new HotbarPreset()
                .add(0, "STEAK")
                .add(1, "GOLDEN_CARROT")
                .add(2, "BREAD")
                .add(3, "FOOD")
                .add(4, "FOOD")
                .add(5, "GOLDEN_APPLE")
                .add(6, "FOOD")
                .add(7, "FOOD")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.ON_FIRE, new HotbarPreset()
                .add(0, "WATER_BUCKET")
                .add(1, "FIRE_RESISTANCE")
                .add(2, "MILK_BUCKET")
                .add(3, "POTION")
                .add(4, "FOOD")
                .add(5, "GOLDEN_APPLE")
                .add(6, "BLOCK")
                .add(7, "BUCKET")
                .add(8, "SHIELD")
                .offHand("SHIELD"));

        PRESETS.put(ActivityType.IN_LAVA, new HotbarPreset()
                .add(0, "WATER_BUCKET")
                .add(1, "FIRE_RESISTANCE")
                .add(2, "BLOCK")
                .add(3, "GOLDEN_APPLE")
                .add(4, "MILK_BUCKET")
                .add(5, "BUCKET")
                .add(6, "POTION")
                .add(7, "ENDER_PEARL")
                .add(8, "FOOD")
                .offHand("TOTEM"));

        PRESETS.put(ActivityType.DROWNING, new HotbarPreset()
                .add(0, "BUCKET")
                .add(1, "DOOR")
                .add(2, "BLOCK")
                .add(3, "POTION")
                .add(4, "GOLDEN_APPLE")
                .add(5, "FOOD")
                .add(6, "PICKAXE")
                .add(7, "TRIDENT")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.FALLING, new HotbarPreset()
                .add(0, "ELYTRA")
                .add(1, "FIREWORK")
                .add(2, "WATER_BUCKET")
                .add(3, "ENDER_PEARL")
                .add(4, "POTION")
                .add(5, "FOOD")
                .add(6, "GOLDEN_APPLE")
                .add(7, "BLOCK")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.POISONED, new HotbarPreset()
                .add(0, "MILK_BUCKET")
                .add(1, "POTION")
                .add(2, "GOLDEN_APPLE")
                .add(3, "FOOD")
                .add(4, "HONEY_BOTTLE")
                .add(5, "BUCKET")
                .add(6, "SHIELD")
                .add(7, "SWORD")
                .add(8, "FOOD"));

        PRESETS.put(ActivityType.WITHERING, new HotbarPreset()
                .add(0, "MILK_BUCKET")
                .add(1, "GOLDEN_APPLE")
                .add(2, "ENCHANTED_GOLDEN_APPLE")
                .add(3, "POTION")
                .add(4, "FOOD")
                .add(5, "BUCKET")
                .add(6, "SHIELD")
                .add(7, "SWORD")
                .add(8, "FOOD")
                .offHand("TOTEM"));

        // ==================== GENERAL PRESET ====================
            
        PRESETS.put(ActivityType.GENERAL, new HotbarPreset()
            .add(0, "SWORD")
            .add(1, "PICKAXE")
            .add(2, "AXE")
            .add(3, "SHOVEL")
            .add(4, "HOE")
            .add(5, "BOW")
            .add(6, "FOOD")
            .add(7, "TORCH")
            .add(8, "BLOCK"));

        PRESETS.put(ActivityType.UNKNOWN, PRESETS.get(ActivityType.GENERAL));
    }

    public static HotbarPreset getPreset(ActivityType activity) {
        return PRESETS.getOrDefault(activity, PRESETS.get(ActivityType.GENERAL));
    }

    // ==================== PRESET HELPER FACTORIES ====================

    private static HotbarPreset makeMobPreset(String primary, String secondary, String tertiary) {
        return new HotbarPreset()
                .add(0, primary)
                .add(1, secondary)
                .add(2, "SHIELD")
                .add(3, "POTION")
                .add(4, tertiary)
                .add(5, "GOLDEN_APPLE")
                .add(6, "ENDER_PEARL")
                .add(7, "BOW")
                .add(8, "FOOD");
    }

    private static HotbarPreset makeMiningPreset(String primary, String light) {
        return new HotbarPreset()
                .add(0, primary)
                .add(1, light)
                .add(2, "WATER_BUCKET")
                .add(3, "FOOD")
                .add(4, "SHOVEL")
                .add(5, "BLOCK")
                .add(6, "BUCKET")
                .add(7, "LADDER")
                .add(8, "FOOD");
    }

    private static HotbarPreset makeFarmPreset(String primary, String secondary, String produce) {
        return new HotbarPreset()
                .add(0, primary)
                .add(1, secondary)
                .add(2, produce)
                .add(3, "HOE")
                .add(4, "WATER_BUCKET")
                .add(5, "BONE_MEAL")
                .add(6, "LEAD")
                .add(7, "FOOD")
                .add(8, "FOOD");
    }

    private static HotbarPreset makeBuildingPreset(String featured) {
        return new HotbarPreset()
                .add(0, featured)
                .add(1, "BLOCK")
                .add(2, "BLOCK")
                .add(3, "STAIRS")
                .add(4, "SLAB")
                .add(5, "WALL")
                .add(6, "PICKAXE")
                .add(7, "AXE")
                .add(8, "FOOD");
    }

    private static HotbarPreset makeCraftingPreset(String tool, String mat1, String mat2) {
        return new HotbarPreset()
                .add(0, tool)
                .add(1, mat1)
                .add(2, mat2)
                .add(3, "BLOCK")
                .add(4, "FOOD")
                .add(5, "TORCH")
                .add(6, "BOOK")
                .add(7, "EXPERIENCE_BOTTLE")
                .add(8, "FOOD");
    }

    private static HotbarPreset makeExplorePreset(String unique, String secondary) {
        return new HotbarPreset()
                .add(0, "COMPASS")
                .add(1, "MAP")
                .add(2, unique)
                .add(3, secondary)
                .add(4, "FOOD")
                .add(5, "ENDER_PEARL")
                .add(6, "TORCH")
                .add(7, "BOW")
                .add(8, "SWORD");
    }

    private static HotbarPreset makeRedstonePreset(String primary, String secondary) {
        return new HotbarPreset()
                .add(0, primary)
                .add(1, secondary)
                .add(2, "REDSTONE")
                .add(3, "REPEATER")
                .add(4, "COMPARATOR")
                .add(5, "TORCH")
                .add(6, "LEVER")
                .add(7, "BUTTON")
                .add(8, "PICKAXE");
    }
}
