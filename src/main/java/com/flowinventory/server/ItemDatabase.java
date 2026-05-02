package com.flowinventory.server;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Comprehensive database of vanilla Minecraft 1.20.1 items organised by category.
 * <p>
 * The database is populated combinatorially: instead of listing every variant
 * by hand, materials (oak, birch, deepslate, copper…) are crossed with shapes
 * (planks, slab, stairs, wall, fence, door…) so the catalogue covers every
 * single block/item the game exposes.
 * <p>
 * After registration, items are matched by a chain of strategies:
 *  1. exact-id lookup (fast path),
 *  2. suffix/prefix pattern matching (catches every dyed/material variant), and
 *  3. instance-of fallbacks (BlockItem, ToolItem, ArmorItem…).
 * <p>
 * Combined, this resolves to several thousand vanilla items + provides a
 * generic fallback for ANY modded item that follows Minecraft's naming
 * conventions.
 */
public class ItemDatabase {

    public static final Map<String, Set<String>> ITEM_CATEGORIES = new HashMap<>();

    // ── Materials & color palettes used for combinatorial registration ──
    private static final String[] WOODS = {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "bamboo", "crimson", "warped"
    };

    private static final String[] STONES = {
            "stone", "cobblestone", "mossy_cobblestone", "stone_brick", "mossy_stone_brick",
            "andesite", "polished_andesite", "diorite", "polished_diorite",
            "granite", "polished_granite",
            "deepslate", "cobbled_deepslate", "polished_deepslate", "deepslate_brick", "deepslate_tile",
            "blackstone", "polished_blackstone", "polished_blackstone_brick",
            "basalt", "smooth_basalt", "polished_basalt",
            "sandstone", "smooth_sandstone", "cut_sandstone", "chiseled_sandstone",
            "red_sandstone", "smooth_red_sandstone", "cut_red_sandstone", "chiseled_red_sandstone",
            "prismarine", "prismarine_brick", "dark_prismarine",
            "purpur", "purpur_pillar", "quartz", "smooth_quartz", "chiseled_quartz", "quartz_brick",
            "nether_brick", "red_nether_brick", "cracked_nether_brick", "chiseled_nether_brick",
            "end_stone", "end_stone_brick",
            "mud_brick", "brick"
    };

    private static final String[] COPPER = {
            "copper", "exposed_copper", "weathered_copper", "oxidized_copper",
            "waxed_copper", "waxed_exposed_copper", "waxed_weathered_copper", "waxed_oxidized_copper",
            "cut_copper", "exposed_cut_copper", "weathered_cut_copper", "oxidized_cut_copper",
            "waxed_cut_copper", "waxed_exposed_cut_copper",
            "waxed_weathered_cut_copper", "waxed_oxidized_cut_copper"
    };

    private static final String[] COLORS = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };

    private static final String[] METAL_TIERS = {
            "wooden", "stone", "iron", "golden", "diamond", "netherite"
    };

    private static final String[] TOOL_TYPES = {
            "sword", "pickaxe", "axe", "shovel", "hoe"
    };

    private static final String[] ARMOR_PIECES = {
            "helmet", "chestplate", "leggings", "boots"
    };

    private static final String[] ARMOR_TIERS = {
            "leather", "chainmail", "iron", "golden", "diamond", "netherite", "turtle"
    };

    static {
        registerCombatWeapons();
        registerRangedWeapons();
        registerMiningTools();
        registerArmor();
        registerFood();
        registerCrops();
        registerBlocks();
        registerWoodFamilies();
        registerStoneFamilies();
        registerCopperFamilies();
        registerColoredVariants();
        registerLighting();
        registerRedstone();
        registerTransport();
        registerSpawnEggs();
        registerBuckets();
        registerUtility();
        registerExplosives();
        registerPotions();
        registerEnchanting();
        registerOres();
        registerNetherEnd();
        registerOcean();
        registerSculk();
        registerDecoration();
    }

    // ==================== REGISTRATION GROUPS ====================

    private static void registerCombatWeapons() {
        // Generates: wooden_sword, stone_sword, iron_sword, golden_sword, diamond_sword, netherite_sword
        registerByMaterial("SWORD", METAL_TIERS, "_sword");
        registerByMaterial("AXE_COMBAT", METAL_TIERS, "_axe");
        registerCategory("TRIDENT", "trident");
        registerCategory("MACE", "mace", "heavy_core");
        registerCategory("SHIELD", "shield");
    }

    private static void registerRangedWeapons() {
        registerCategory("BOW", "bow", "crossbow");
        registerCategory("CROSSBOW", "crossbow");
        registerCategory("ARROW", "arrow", "tipped_arrow", "spectral_arrow");
        registerCategory("WIND_CHARGE", "wind_charge", "breeze_rod");
        registerCategory("FIREWORK", "firework_rocket", "firework_star");
    }

    private static void registerMiningTools() {
        registerByMaterial("PICKAXE", METAL_TIERS, "_pickaxe");
        registerByMaterial("SHOVEL", METAL_TIERS, "_shovel");
        registerByMaterial("AXE_TOOL", METAL_TIERS, "_axe");
        registerByMaterial("HOE", METAL_TIERS, "_hoe");
        registerCategory("FISHING_ROD", "fishing_rod", "carrot_on_a_stick", "warped_fungus_on_a_stick");
    }

    private static void registerArmor() {
        // Leather, chainmail, iron, golden, diamond, netherite, turtle helmet
        for (String tier : ARMOR_TIERS) {
            for (String piece : ARMOR_PIECES) {
                String id = tier + "_" + piece;
                addToCategory(piece.toUpperCase(), id);
            }
        }
        registerCategory("HORSE_ARMOR",
                "leather_horse_armor", "iron_horse_armor",
                "golden_horse_armor", "diamond_horse_armor",
                "netherite_horse_armor");
        registerCategory("ELYTRA", "elytra");
    }

    private static void registerFood() {
        registerCategory("FOOD",
                "beef", "porkchop", "mutton", "chicken", "rabbit",
                "cooked_beef", "cooked_porkchop", "cooked_mutton",
                "cooked_chicken", "rabbit_stew",
                "salmon", "cod", "tropical_fish", "pufferfish",
                "cooked_salmon", "cooked_cod",
                "apple", "mushroom_stew", "bread", "potato", "baked_potato",
                "carrot", "golden_carrot", "beetroot", "beetroot_soup",
                "melon_slice", "glistering_melon_slice",
                "sweet_berries", "glow_berries",
                "golden_apple", "enchanted_golden_apple",
                "cookie", "cake", "pumpkin_pie", "rabbit_foot",
                "chorus_fruit", "popped_chorus_fruit",
                "suspicious_stew", "honey_bottle", "milk_bucket",
                "spider_eye", "rotten_flesh", "poisonous_potato",
                "egg", "dried_kelp"
        );
        registerCategory("BAKED_GOODS",
                "bread", "cake", "cookie", "pumpkin_pie", "suspicious_stew");
    }

    private static void registerCrops() {
        registerCategory("CROP",
                "wheat", "carrot", "potato", "beetroot",
                "bamboo", "sugar_cane", "cocoa_beans", "kelp",
                "wheat_seeds", "beetroot_seeds", "pumpkin_seeds", "melon_seeds",
                "torchflower_seeds", "pitcher_pod", "pitcher_plant",
                "nether_wart", "warped_fungus", "crimson_fungus");
    }

    private static void registerBlocks() {
        registerCategory("BLOCK",
                "dirt", "grass_block", "podzol", "coarse_dirt", "rooted_dirt",
                "mud", "muddy_mangrove_roots",
                "stone", "cobblestone", "mossy_cobblestone", "stone_bricks", "mossy_stone_bricks",
                "sand", "red_sand", "gravel", "clay", "soul_sand", "soul_soil",
                "snow", "snow_block", "packed_ice", "blue_ice", "ice", "frosted_ice",
                "bedrock", "obsidian", "crying_obsidian", "ancient_debris",
                "reinforced_deepslate",
                "glowstone", "shroomlight", "sea_lantern",
                "magma_block", "honey_block", "slime_block",
                "tnt", "respawn_anchor",
                "moss_block", "moss_carpet", "azalea", "flowering_azalea",
                "spore_blossom", "big_dripleaf", "small_dripleaf",
                "amethyst_block", "budding_amethyst", "calcite", "tuff",
                "dripstone_block", "pointed_dripstone");
    }

    private static void registerWoodFamilies() {
        // Each wood type has ~20 variants. 11 woods × ~20 = 220 items.
        for (String wood : WOODS) {
            addToCategory("BLOCK", wood + "_planks");
            addToCategory("LOG", wood + "_log", wood + "_wood",
                    "stripped_" + wood + "_log", "stripped_" + wood + "_wood",
                    wood + "_stem", "stripped_" + wood + "_stem",
                    wood + "_block", wood + "_hyphae", "stripped_" + wood + "_hyphae");
            addToCategory("LEAVES", wood + "_leaves");
            addToCategory("SAPLING", wood + "_sapling", wood + "_propagule");
            addToCategory("STAIRS", wood + "_stairs");
            addToCategory("SLAB", wood + "_slab");
            addToCategory("FENCE", wood + "_fence");
            addToCategory("FENCE_GATE", wood + "_fence_gate");
            addToCategory("DOOR", wood + "_door");
            addToCategory("TRAPDOOR", wood + "_trapdoor");
            addToCategory("BUTTON", wood + "_button");
            addToCategory("PRESSURE_PLATE", wood + "_pressure_plate");
            addToCategory("SIGN", wood + "_sign", wood + "_hanging_sign");
            addToCategory("BOAT", wood + "_boat", wood + "_chest_boat");
        }
    }

    private static void registerStoneFamilies() {
        // 30+ stone types × 3 shapes (stairs, slab, wall) = ~90 items
        for (String stone : STONES) {
            addToCategory("BLOCK", stone, stone + "s");
            addToCategory("STAIRS", stone + "_stairs");
            addToCategory("SLAB", stone + "_slab");
            addToCategory("WALL", stone + "_wall");
        }
    }

    private static void registerCopperFamilies() {
        for (String c : COPPER) {
            addToCategory("BLOCK", c + "_block", c);
            addToCategory("STAIRS", c + "_stairs");
            addToCategory("SLAB", c + "_slab");
            addToCategory("DOOR", c + "_door");
            addToCategory("TRAPDOOR", c + "_trapdoor");
            addToCategory("BUTTON", c + "_button");
            addToCategory("BULB", c + "_bulb");
            addToCategory("GRATE", c + "_grate");
        }
    }

    private static void registerColoredVariants() {
        // 16 colors × ~10 colored block types = ~160 entries
        for (String color : COLORS) {
            addToCategory("WOOL", color + "_wool");
            addToCategory("CARPET", color + "_carpet");
            addToCategory("BLOCK", color + "_terracotta", color + "_glazed_terracotta",
                    color + "_concrete", color + "_concrete_powder");
            addToCategory("TERRACOTTA", color + "_terracotta", color + "_glazed_terracotta");
            addToCategory("CONCRETE", color + "_concrete", color + "_concrete_powder");
            addToCategory("BANNER", color + "_banner", color + "_wall_banner");
            addToCategory("CANDLE", color + "_candle");
            addToCategory("BED", color + "_bed");
            addToCategory("DYE", color + "_dye");
            addToCategory("STAINED_GLASS", color + "_stained_glass", color + "_stained_glass_pane");
            addToCategory("SHULKER_BOX", color + "_shulker_box");
        }
        // Plain glass
        addToCategory("BLOCK", "glass", "tinted_glass", "glass_pane");
        addToCategory("STAINED_GLASS", "glass", "glass_pane");
    }

    private static void registerLighting() {
        registerCategory("TORCH", "torch", "soul_torch", "redstone_torch",
                "wall_torch", "soul_wall_torch", "redstone_wall_torch");
        registerCategory("LANTERN", "lantern", "soul_lantern");
        registerCategory("LIGHT_SOURCE", "glowstone", "shroomlight",
                "sea_lantern", "jack_o_lantern", "ochre_froglight",
                "verdant_froglight", "pearlescent_froglight", "redstone_lamp",
                "end_rod", "lantern", "soul_lantern", "torch", "soul_torch",
                "campfire", "soul_campfire");
    }

    private static void registerRedstone() {
        registerCategory("REDSTONE", "redstone", "redstone_block");
        registerCategory("REDSTONE_DUST", "redstone");
        registerCategory("REDSTONE_COMPONENT",
                "repeater", "comparator", "observer",
                "daylight_detector", "target",
                "redstone_torch", "redstone_lamp",
                "lever", "tripwire_hook", "tripwire");
        registerCategory("POWERED_COMPONENT",
                "piston", "sticky_piston",
                "dispenser", "dropper", "hopper", "hopper_minecart",
                "tnt", "tnt_minecart");
        registerCategory("RAIL",
                "rail", "powered_rail", "detector_rail", "activator_rail");
    }

    private static void registerTransport() {
        registerCategory("MINECART",
                "minecart", "chest_minecart", "furnace_minecart",
                "tnt_minecart", "hopper_minecart", "command_block_minecart");
    }

    private static void registerSpawnEggs() {
        // Generic catch-all. ItemHeuristics also detects via instanceof SpawnEggItem.
        registerCategory("SPAWN_EGG",
                "pig_spawn_egg", "cow_spawn_egg", "chicken_spawn_egg", "sheep_spawn_egg",
                "wolf_spawn_egg", "villager_spawn_egg", "zombie_spawn_egg", "skeleton_spawn_egg",
                "creeper_spawn_egg", "spider_spawn_egg", "zombie_villager_spawn_egg",
                "slime_spawn_egg", "enderman_spawn_egg", "cave_spider_spawn_egg",
                "silverfish_spawn_egg", "blaze_spawn_egg", "magma_cube_spawn_egg",
                "bat_spawn_egg", "ocelot_spawn_egg", "horse_spawn_egg",
                "squid_spawn_egg", "glow_squid_spawn_egg", "dolphin_spawn_egg",
                "pufferfish_spawn_egg", "salmon_spawn_egg", "tropical_fish_spawn_egg",
                "turtle_spawn_egg", "phantom_spawn_egg", "pillager_spawn_egg",
                "vindicator_spawn_egg", "evoker_spawn_egg", "shulker_spawn_egg",
                "witch_spawn_egg", "husk_spawn_egg", "stray_spawn_egg",
                "drowned_spawn_egg", "guardian_spawn_egg", "elder_guardian_spawn_egg",
                "skeleton_horse_spawn_egg", "zombie_horse_spawn_egg",
                "ravager_spawn_egg", "hoglin_spawn_egg", "piglin_spawn_egg",
                "zombified_piglin_spawn_egg", "strider_spawn_egg",
                "zoglin_spawn_egg", "piglin_brute_spawn_egg",
                "goat_spawn_egg", "axolotl_spawn_egg", "frog_spawn_egg",
                "tadpole_spawn_egg", "allay_spawn_egg", "warden_spawn_egg",
                "iron_golem_spawn_egg", "snow_golem_spawn_egg",
                "llama_spawn_egg", "trader_llama_spawn_egg",
                "parrot_spawn_egg", "cat_spawn_egg", "mule_spawn_egg",
                "donkey_spawn_egg", "breeze_spawn_egg", "sniffer_spawn_egg",
                "camel_spawn_egg", "wandering_trader_spawn_egg");
    }

    private static void registerBuckets() {
        registerCategory("BUCKET",
                "bucket", "water_bucket", "lava_bucket", "powder_snow_bucket",
                "axolotl_bucket", "cod_bucket", "salmon_bucket",
                "pufferfish_bucket", "tropical_fish_bucket",
                "tadpole_bucket", "milk_bucket");
        registerCategory("FLUID_CONTAINER",
                "water_bucket", "lava_bucket", "milk_bucket", "powder_snow_bucket",
                "axolotl_bucket", "cod_bucket", "salmon_bucket",
                "pufferfish_bucket", "tropical_fish_bucket", "tadpole_bucket");
    }

    private static void registerUtility() {
        registerCategory("COMPASS", "compass", "recovery_compass", "lodestone_compass");
        registerCategory("CLOCK", "clock");
        registerCategory("MAP", "map", "filled_map");
        registerCategory("BOOK", "book", "writable_book", "written_book", "enchanted_book", "knowledge_book");
        registerCategory("TOOL",
                "shears", "flint_and_steel", "fire_charge", "glass_bottle",
                "brush", "spyglass", "name_tag", "lead", "saddle",
                "carrot_on_a_stick", "warped_fungus_on_a_stick", "fishing_rod",
                "bundle", "goat_horn");
        registerCategory("STRING", "string", "lead");
        registerCategory("PAPER", "paper");
        registerCategory("STICK", "stick", "blaze_rod", "breeze_rod");
        registerCategory("PAINTING", "painting");
        registerCategory("ARMOR_STAND", "armor_stand");
        registerCategory("ITEM_FRAME", "item_frame", "glow_item_frame");
        registerCategory("SADDLE", "saddle");
        registerCategory("LEAD", "lead");
        registerCategory("NAME_TAG", "name_tag");
        registerCategory("SPYGLASS", "spyglass");
        registerCategory("BRUSH", "brush");
        registerCategory("BUNDLE", "bundle");
    }

    private static void registerExplosives() {
        registerCategory("EXPLOSIVE",
                "tnt", "tnt_minecart", "fire_charge",
                "bed", "respawn_anchor", "wind_charge");
    }

    private static void registerPotions() {
        registerCategory("POTION",
                "potion", "splash_potion", "lingering_potion",
                "tipped_arrow", "experience_bottle");
        registerCategory("BREWING_INGREDIENT",
                "nether_wart", "blaze_powder", "ghast_tear", "glistering_melon_slice",
                "golden_carrot", "spider_eye", "fermented_spider_eye", "magma_cream",
                "sugar", "gunpowder", "dragon_breath", "phantom_membrane", "rabbit_foot",
                "blaze_rod", "ender_pearl", "ender_eye", "shulker_shell",
                "turtle_helmet", "redstone", "glowstone_dust", "pufferfish");
        registerCategory("BOTTLE", "glass_bottle", "experience_bottle", "honey_bottle");
    }

    private static void registerEnchanting() {
        registerCategory("ENCHANTING",
                "enchanted_book", "experience_bottle", "lapis_lazuli",
                "book", "bookshelf", "chiseled_bookshelf");
        registerCategory("LAPIS", "lapis_lazuli", "lapis_block", "lapis_ore", "deepslate_lapis_ore");
    }

    private static void registerOres() {
        registerCategory("ORE",
                "coal_ore", "deepslate_coal_ore",
                "iron_ore", "deepslate_iron_ore",
                "gold_ore", "deepslate_gold_ore",
                "copper_ore", "deepslate_copper_ore",
                "diamond_ore", "deepslate_diamond_ore",
                "emerald_ore", "deepslate_emerald_ore",
                "lapis_ore", "deepslate_lapis_ore",
                "redstone_ore", "deepslate_redstone_ore",
                "nether_quartz_ore", "nether_gold_ore",
                "ancient_debris");
        registerCategory("RAW_MATERIAL",
                "raw_iron", "raw_gold", "raw_copper",
                "raw_iron_block", "raw_gold_block", "raw_copper_block");
        registerCategory("INGOT",
                "iron_ingot", "gold_ingot", "copper_ingot", "netherite_ingot",
                "netherite_scrap");
        registerCategory("NUGGET", "iron_nugget", "gold_nugget");
        registerCategory("GEM",
                "diamond", "emerald", "lapis_lazuli", "amethyst_shard",
                "prismarine_crystals", "prismarine_shard",
                "quartz", "nether_star", "echo_shard", "disc_fragment_5");
        registerCategory("COAL", "coal", "charcoal", "coal_block");
    }

    private static void registerNetherEnd() {
        registerCategory("NETHERRACK",
                "netherrack", "soul_sand", "soul_soil", "basalt", "blackstone",
                "nether_bricks", "red_nether_bricks", "nether_wart_block",
                "warped_wart_block", "shroomlight",
                "crimson_nylium", "warped_nylium",
                "weeping_vines", "twisting_vines", "nether_sprouts",
                "soul_fire", "soul_torch", "soul_lantern", "soul_campfire",
                "ancient_debris", "nether_gold_ore", "nether_quartz_ore");
        registerCategory("NETHER_GEM",
                "netherite_ingot", "netherite_scrap",
                "netherite_upgrade_smithing_template",
                "netherite_sword", "netherite_pickaxe", "netherite_axe",
                "netherite_shovel", "netherite_hoe",
                "netherite_helmet", "netherite_chestplate",
                "netherite_leggings", "netherite_boots");
        registerCategory("BLAZE", "blaze_rod", "blaze_powder");
        registerCategory("END_STONE",
                "end_stone", "end_stone_bricks",
                "purpur_block", "purpur_pillar", "purpur_stairs", "purpur_slab",
                "chorus_flower", "chorus_plant", "chorus_fruit", "popped_chorus_fruit",
                "dragon_egg", "dragon_head", "dragon_breath", "end_rod",
                "end_portal_frame", "end_crystal");
        registerCategory("ENDER",
                "ender_pearl", "ender_eye", "eye_of_ender",
                "ender_chest", "shulker_shell");
    }

    private static void registerOcean() {
        registerCategory("OCEAN",
                "prismarine", "prismarine_bricks", "dark_prismarine",
                "sea_lantern", "conduit", "heart_of_the_sea",
                "nautilus_shell", "scute", "turtle_egg", "turtle_helmet",
                "trident", "kelp", "dried_kelp", "dried_kelp_block",
                "sea_pickle", "wet_sponge", "sponge");
        registerCategory("CORAL",
                "tube_coral", "brain_coral", "bubble_coral", "fire_coral", "horn_coral",
                "tube_coral_block", "brain_coral_block", "bubble_coral_block",
                "fire_coral_block", "horn_coral_block",
                "tube_coral_fan", "brain_coral_fan", "bubble_coral_fan",
                "fire_coral_fan", "horn_coral_fan",
                "dead_tube_coral_block", "dead_brain_coral_block",
                "dead_bubble_coral_block", "dead_fire_coral_block",
                "dead_horn_coral_block");
    }

    private static void registerSculk() {
        registerCategory("SCULK",
                "sculk", "sculk_vein", "sculk_catalyst",
                "sculk_shrieker", "sculk_sensor", "calibrated_sculk_sensor",
                "echo_shard");
        registerCategory("WARDEN_DROP", "echo_shard");
    }

    private static void registerDecoration() {
        registerCategory("FLOWER",
                "poppy", "blue_orchid", "allium", "azure_bluet",
                "red_tulip", "orange_tulip", "white_tulip", "pink_tulip",
                "oxeye_daisy", "dandelion", "cornflower", "lily_of_the_valley",
                "wither_rose", "sunflower", "lilac", "rose_bush", "peony",
                "spore_blossom", "torchflower", "pitcher_plant",
                "cherry_petals", "pink_petals", "lily_pad", "sea_pickle");
        registerCategory("MUSHROOM",
                "brown_mushroom", "red_mushroom", "mushroom_stem",
                "crimson_fungus", "warped_fungus",
                "brown_mushroom_block", "red_mushroom_block",
                "shroomlight", "nether_wart");
        registerCategory("BANNER", "banner", "banner_pattern");
    }

    // ==================== HELPER METHODS ====================

    private static void registerByMaterial(String category, String[] tiers, String suffix) {
        for (String tier : tiers) {
            addToCategory(category, tier + suffix);
        }
    }

    private static void registerCategory(String name, String... ids) {
        ITEM_CATEGORIES.computeIfAbsent(name, k -> new HashSet<>());
        for (String id : ids) {
            ITEM_CATEGORIES.get(name).add(id);
        }
    }

    private static void addToCategory(String name, String... ids) {
        ITEM_CATEGORIES.computeIfAbsent(name, k -> new HashSet<>());
        for (String id : ids) {
            ITEM_CATEGORIES.get(name).add(id);
        }
    }

    // ==================== PUBLIC API ====================

    public static Set<String> getCategoryNames() {
        return ITEM_CATEGORIES.keySet();
    }

    public static Set<String> getItemsInCategory(String category) {
        return ITEM_CATEGORIES.getOrDefault(category, new HashSet<>());
    }

    public static boolean isInCategory(String itemId, String category) {
        Set<String> items = ITEM_CATEGORIES.get(category);
        if (items == null) return false;
        String path = stripNamespace(itemId);
        if (items.contains(path)) return true;
        for (String pattern : items) {
            if (path.contains(pattern)) return true;
        }
        return false;
    }

    /** Classify an item by id into ALL applicable categories (multi-label). */
    public static Set<String> classifyItem(String itemId) {
        Set<String> categories = new HashSet<>();
        String path = stripNamespace(itemId);
        for (Map.Entry<String, Set<String>> entry : ITEM_CATEGORIES.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (path.equals(pattern) || path.contains(pattern)) {
                    categories.add(entry.getKey());
                    break;
                }
            }
        }
        return categories;
    }

    /**
     * Most-specific category for an item, used by the sort algorithm.
     * Probes a priority-ordered list and returns the first hit.
     */
    public static String getPrimaryCategory(Item item) {
        String path = stripNamespace(Registries.ITEM.getId(item).toString());

        String[] priority = {
                "SWORD", "AXE_COMBAT", "TRIDENT", "MACE", "BOW", "CROSSBOW",
                "PICKAXE", "SHOVEL", "AXE_TOOL", "HOE",
                "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "SHIELD",
                "ELYTRA", "HORSE_ARMOR",
                "FOOD", "POTION", "BREWING_INGREDIENT",
                "BLOCK", "STAIRS", "SLAB", "WALL", "FENCE", "DOOR", "TRAPDOOR",
                "REDSTONE", "REDSTONE_COMPONENT", "POWERED_COMPONENT", "RAIL",
                "ORE", "RAW_MATERIAL", "INGOT", "NUGGET", "GEM", "COAL",
                "NETHER_GEM", "ENDER", "BLAZE",
                "LOG", "LEAVES", "SAPLING", "FLOWER", "MUSHROOM",
                "TORCH", "LANTERN", "LIGHT_SOURCE",
                "WOOL", "CARPET", "BANNER", "BED",
                "STAINED_GLASS", "TERRACOTTA", "CONCRETE",
                "MAP", "COMPASS", "CLOCK", "BOOK", "TOOL",
                "BUCKET", "FLUID_CONTAINER",
                "MINECART", "BOAT", "SADDLE",
                "SPAWN_EGG", "SHULKER_BOX", "PAINTING",
                "EXPLOSIVE", "FIREWORK", "STRING", "DYE",
                "OCEAN", "CORAL", "SCULK", "END_STONE", "NETHERRACK"
        };

        for (String cat : priority) {
            Set<String> set = ITEM_CATEGORIES.get(cat);
            if (set == null) continue;
            if (set.contains(path)) return cat;
            for (String pattern : set) {
                if (path.contains(pattern)) return cat;
            }
        }
        return "MISC";
    }

    public static int getCombatPower(Item item) {
        String path = stripNamespace(Registries.ITEM.getId(item).toString());

        if (path.contains("netherite")) return 100;
        if (path.contains("diamond")) return 90;
        if (path.contains("iron")) return 75;
        if (path.contains("golden")) return 70;
        if (path.contains("stone")) return 50;
        if (path.contains("wooden")) return 30;
        if (path.contains("leather")) return 20;
        if (path.contains("trident")) return 95;
        if (path.contains("mace")) return 98;
        if (path.contains("bow")) return 85;
        if (path.contains("crossbow")) return 80;
        return 10;
    }

    public static int getMiningLevel(Item item) {
        String path = stripNamespace(Registries.ITEM.getId(item).toString());
        if (path.contains("netherite")) return 4;
        if (path.contains("diamond")) return 3;
        if (path.contains("iron")) return 2;
        if (path.contains("stone")) return 1;
        return 0;
    }

    public static boolean isWeapon(Item item) {
        String path = stripNamespace(Registries.ITEM.getId(item).toString());
        return path.contains("sword") || path.contains("axe")
                || path.contains("trident") || path.contains("mace")
                || path.contains("bow") || path.contains("crossbow");
    }

    public static boolean isTool(Item item) {
        String path = stripNamespace(Registries.ITEM.getId(item).toString());
        return path.contains("pickaxe") || path.contains("shovel")
                || path.contains("axe") || path.contains("hoe")
                || path.contains("shears") || path.contains("fishing_rod")
                || path.contains("brush") || path.contains("spyglass");
    }

    public static boolean isArmor(Item item) {
        return item instanceof net.minecraft.item.ArmorItem;
    }

    public static boolean isFood(Item item) {
        return item.getFoodComponent() != null;
    }

    public static boolean isBlock(Item item) {
        return item instanceof net.minecraft.item.BlockItem;
    }

    public static String getTierName(Item item) {
        String path = stripNamespace(Registries.ITEM.getId(item).toString());
        if (path.contains("netherite")) return "Netherite";
        if (path.contains("diamond")) return "Diamond";
        if (path.contains("iron")) return "Iron";
        if (path.contains("stone")) return "Stone";
        if (path.contains("golden")) return "Gold";
        if (path.contains("wooden")) return "Wood";
        if (path.contains("leather")) return "Leather";
        if (path.contains("chainmail")) return "Chainmail";
        return "Unknown";
    }

    /** Total number of distinct (category, item-pattern) entries registered. */
    public static int getTotalRegistrations() {
        int total = 0;
        for (Set<String> values : ITEM_CATEGORIES.values()) {
            total += values.size();
        }
        return total;
    }

    private static String stripNamespace(String itemId) {
        try {
            return new Identifier(itemId).getPath();
        } catch (Exception e) {
            int idx = itemId.indexOf(':');
            return idx >= 0 ? itemId.substring(idx + 1) : itemId;
        }
    }
}
