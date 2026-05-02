package com.flowinventory.profiles;

/**
 * Comprehensive activity catalogue for FlowInventory.
 * <p>
 * Every constant maps to a different "context" the player can be in.
 * The detector picks the activity with the highest confidence score
 * out of these and the swapper builds an optimal hotbar layout for it.
 * <p>
 * Combined with the heuristic scoring, the enchantment/tier multipliers,
 * the dimension/biome modifiers and the mob-specific combat presets,
 * this enum drives well over a million distinct evaluation paths.
 */
public enum ActivityType {

    // ── GENERIC COMBAT ───────────────────────────────────────────────
    COMBAT("Combat", "\u2694"),
    SWORD_COMBAT("Sword Combat", "\uD83D\uDDE1"),
    AXE_COMBAT("Axe Combat", "\uD83E\uDE93"),
    MACE("Mace Combat", "\uD83D\uDD28"),
    TRIDENT_COMBAT("Trident Combat", "\uD83D\uDD31"),
    ARCHERY("Archery", "\uD83C\uDFF9"),
    SNIPER("Sniper", "\uD83C\uDFAF"),
    CROSSBOW_COMBAT("Crossbow", "\uD83C\uDFF9"),
    EXPLOSIVES("Explosives", "\uD83D\uDCA5"),
    POTION_COMBAT("Potion Combat", "\uD83E\uDDEA"),
    PVP("PvP", "\u2694"),
    BATTLE("Battle", "\u2694\uFE0F"),
    RAID("Raiding", "\u2694\uFE0F"),
    DEFENSIVE("Defensive", "\uD83D\uDEE1"),
    BERSERK("Berserk", "\uD83D\uDD25"),

    // ── MOB-SPECIFIC HUNTING ─────────────────────────────────────────
    CREEPER("Creeper Hunting", "\uD83E\uDDE8"),
    SKELETON("Skeleton Hunting", "\uD83D\uDC80"),
    ZOMBIE("Zombie Clearing", "\uD83E\uDDDF"),
    SPIDER("Spider Hunting", "\uD83D\uDD77"),
    ENDERMAN("Enderman", "\uD83E\uDDCD"),
    BLAZE("Blaze Farming", "\uD83D\uDD25"),
    WITCH("Witch Hunting", "\uD83E\uDDD9"),
    GUARDIAN("Guardian", "\uD83D\uDEE1"),
    SHULKER("Shulker", "\uD83D\uDCE6"),
    WITHER("Wither Fight", "\uD83D\uDC80"),
    DRAGON("Dragon Fight", "\uD83D\uDC09"),
    WARDEN("Warden", "\uD83D\uDC41"),
    PIGLIN("Piglin Trade", "\uD83D\uDC37"),
    PIGLIN_BRUTE("Piglin Brute", "\uD83D\uDC37"),
    HOGLIN("Hoglin Hunting", "\uD83D\uDC17"),
    ZOGLIN("Zoglin", "\uD83D\uDC17"),
    GHAST("Ghast Hunting", "\u2620"),
    MAGMA_CUBE("Magma Cube", "\uD83D\uDD32"),
    SLIME("Slime Farming", "\uD83D\uDFE2"),
    PHANTOM("Phantom", "\uD83E\uDD87"),
    RAVAGER("Ravager", "\uD83D\uDC02"),
    PILLAGER("Pillager", "\uD83C\uDFF9"),
    EVOKER("Evoker", "\uD83E\uDDD9"),
    VINDICATOR("Vindicator", "\u2694"),
    VEX("Vex", "\uD83D\uDC7B"),
    DROWNED("Drowned", "\uD83C\uDF0A"),
    HUSK("Husk", "\uD83C\uDFDC"),
    STRAY("Stray", "\u2744"),
    WITHER_SKELETON("Wither Skeleton", "\uD83D\uDC80"),
    SILVERFISH("Silverfish", "\uD83D\uDC1B"),
    ENDERMITE("Endermite", "\uD83D\uDC1B"),
    BREEZE("Breeze", "\uD83C\uDF2C"),

    // ── MINING ───────────────────────────────────────────────────────
    MINING("Mining", "\u26CF"),
    ORE("Ore Mining", "\uD83D\uDC8E"),
    STONE("Stone Mining", "\uD83E\uDEA8"),
    DIRT("Dirt Mining", "\uD83D\uDFEB"),
    SAND("Sand Mining", "\uD83C\uDFD6"),
    GRAVEL("Gravel Mining", "\uD83E\uDEA8"),
    CLAY("Clay Mining", "\uD83D\uDFE6"),
    NETHERRACK("Netherrack", "\uD83D\uDD25"),
    NETHER_ORE("Nether Ore", "\uD83C\uDF0B"),
    ANCIENT_DEBRIS("Ancient Debris", "\u26A1"),
    DIAMOND("Diamond", "\uD83D\uDC8B"),
    EMERALD("Emerald", "\uD83D\uDFE2"),
    COAL_MINING("Coal", "\u2B1B"),
    IRON_MINING("Iron", "\uD83D\uDD32"),
    GOLD_MINING("Gold", "\uD83E\uDD47"),
    COPPER_MINING("Copper", "\uD83E\uDD49"),
    LAPIS_MINING("Lapis", "\uD83D\uDD37"),
    REDSTONE_MINING("Redstone", "\uD83D\uDD34"),
    AMETHYST_MINING("Amethyst", "\uD83D\uDC9C"),
    QUARTZ_MINING("Quartz", "\u26AA"),
    DEEPSLATE_MINING("Deepslate", "\uD83E\uDEA8"),
    SCULK_MINING("Sculk", "\uD83D\uDDFF"),
    OBSIDIAN_MINING("Obsidian", "\u2B1B"),

    // ── BUILDING & DECORATION ────────────────────────────────────────
    BUILDING("Building", "\uD83E\uDDF1"),
    WOODWORKING("Woodworking", "\uD83E\uDEB5"),
    STONEMASONRY("Stonemasonry", "\uD83E\uDEA8"),
    DECORATING("Decorating", "\uD83C\uDFA8"),
    TERRACOTTA("Terracotta", "\uD83C\uDFFA"),
    CONCRETE("Concrete", "\uD83C\uDFA8"),
    GLASSWORK("Glasswork", "\uD83E\uDE9F"),
    SCULKING("Sculking", "\uD83D\uDDFF"),
    ROOFING("Roofing", "\uD83C\uDFE0"),
    FURNISHING("Furnishing", "\uD83E\uDE91"),
    LANDSCAPING("Landscaping", "\uD83C\uDF33"),
    DECORATION_PAINTER("Painter", "\uD83C\uDFA8"),
    DECORATION_BANNER("Banner Crafting", "\uD83D\uDEA9"),
    DECORATION_LIGHTS("Lights", "\uD83D\uDCA1"),

    // ── FARMING & GATHERING ──────────────────────────────────────────
    FARMING("Farming", "\uD83C\uDF3E"),
    ANIMAL_FARMING("Animal Farming", "\uD83D\uDC04"),
    CROP_FARMING("Crop Farming", "\uD83C\uDF31"),
    TREE_FARMING("Tree Farming", "\uD83C\uDF33"),
    SHEEP_FARMING("Sheep Farming", "\uD83D\uDC11"),
    COW_FARMING("Cow Farming", "\uD83D\uDC04"),
    PIG_FARMING("Pig Farming", "\uD83D\uDC16"),
    CHICKEN_FARMING("Chicken Farming", "\uD83D\uDC14"),
    BEE_FARMING("Bee Farming", "\uD83D\uDC1D"),
    MUSHROOM_FARMING("Mushroom Farming", "\uD83C\uDF44"),
    KELP_FARMING("Kelp Farming", "\uD83C\uDF31"),
    BAMBOO_FARMING("Bamboo Farming", "\uD83C\uDF8B"),
    SUGAR_CANE_FARMING("Sugar Cane", "\uD83C\uDF7C"),
    BREEDING("Breeding", "\u2764"),
    FISHING("Fishing", "\uD83C\uDFA3"),
    OCEAN_FISHING("Ocean Fishing", "\uD83C\uDF0A"),
    JUNK_FISHING("Junk Fishing", "\uD83D\uDDD1"),
    OCEAN("Ocean Activities", "\uD83C\uDF0A"),
    SAILING("Sailing", "\u26F5"),
    DIVING("Diving", "\uD83E\uDD3F"),

    // ── CRAFTING & WORKBENCHES ───────────────────────────────────────
    ENCHANTING("Enchanting", "\uD83D\uDCDA"),
    BREWING("Brewing", "\uD83E\uDDEA"),
    ALCHEMY("Alchemy", "\u2697"),
    SMITHING("Smithing", "\u2692"),
    ANVIL("Anvil Work", "\uD83D\uDD28"),
    GRINDSTONE("Grindstone", "\uD83E\uDEA8"),
    STONECUTTER("Stonecutting", "\uD83E\uDE93"),
    LOOM("Loom", "\uD83E\uDDF5"),
    CARTOGRAPHY("Cartography", "\uD83D\uDDFA"),
    COMPOSTING("Composting", "\uD83E\uDEB4"),
    COOKING("Cooking", "\uD83C\uDF73"),
    SMELTING("Smelting", "\uD83D\uDD25"),
    TRADING("Trading", "\uD83D\uDCB0"),

    // ── EXPLORATION & DIMENSIONS ─────────────────────────────────────
    EXPLORING("Exploring", "\uD83E\uDDED"),
    CAVING("Caving", "\uD83D\uDD73"),
    NETHER_EXPLORE("Nether Explore", "\uD83D\uDD25"),
    END_EXPLORE("End Explore", "\uD83D\uDD2E"),
    OCEAN_EXPLORE("Ocean Explore", "\uD83C\uDF0A"),
    JUNGLE_EXPLORE("Jungle Explore", "\uD83C\uDF34"),
    DESERT_EXPLORE("Desert Explore", "\uD83C\uDFDC"),
    SNOWY_EXPLORE("Snowy Explore", "\u2744"),
    SWAMP_EXPLORE("Swamp Explore", "\uD83C\uDF3F"),
    MOUNTAIN_EXPLORE("Mountain Explore", "\u26F0"),
    BADLANDS_EXPLORE("Badlands", "\uD83C\uDFDC"),
    MUSHROOM_EXPLORE("Mushroom Fields", "\uD83C\uDF44"),
    FOREST_EXPLORE("Forest", "\uD83C\uDF32"),
    PLAINS_EXPLORE("Plains", "\uD83C\uDF3E"),
    SAVANNA_EXPLORE("Savanna", "\uD83C\uDFDE"),
    DEEP_DARK("Deep Dark", "\uD83D\uDDFF"),
    ANCIENT_CITY("Ancient City", "\uD83D\uDDFF"),
    STRONGHOLD("Stronghold", "\uD83C\uDFF0"),
    MONUMENT("Monument", "\uD83C\uDFDB"),
    MANSION("Mansion", "\uD83C\uDFEF"),
    FORTRESS("Fortress", "\uD83C\uDFF0"),
    BASTION("Bastion", "\uD83C\uDFF0"),
    END_CITY("End City", "\uD83D\uDD2E"),

    // ── DIMENSION SHORTCUTS (used by HotbarSwapper) ──────────────────
    OVERWORLD("Overworld", "\uD83C\uDF31"),
    NETHER("Nether", "\uD83D\uDD25"),
    NETHER_RESOURCES("Nether Resources", "\uD83C\uDF0B"),
    NETHER_FARMING("Nether Farming", "\uD83C\uDF44"),
    END("End", "\uD83D\uDD2E"),
    END_STONE("End Stone", "\u2B1C"),

    // ── REDSTONE & AUTOMATION ────────────────────────────────────────
    REDSTONE("Redstone", "\uD83D\uDD0C"),
    REDSTONE_LOGIC("Redstone Logic", "\uD83E\uDDEE"),
    REDSTONE_MACHINES("Redstone Machines", "\u2699"),
    REDSTONE_TRANSPORT("Redstone Transport", "\uD83D\uDCE1"),
    OBSERVER("Observer", "\uD83D\uDC41"),
    PISTON("Piston", "\uD83D\uDD29"),
    HOPPER("Hopper", "\uD83E\uDDFA"),
    DROPPER("Dropper", "\uD83D\uDCE4"),
    DISPENSER("Dispenser", "\uD83D\uDCE5"),

    // ── TRANSPORTATION ───────────────────────────────────────────────
    RIDING("Riding", "\uD83C\uDFC7"),
    HORSE_RIDING("Horse Riding", "\uD83D\uDC34"),
    PIG_RIDING("Pig Riding", "\uD83D\uDC37"),
    STRIDER_RIDING("Strider Riding", "\uD83E\uDD90"),
    CAMEL_RIDING("Camel Riding", "\uD83D\uDC2A"),
    LLAMA_RIDING("Llama Riding", "\uD83E\uDD99"),
    DONKEY_RIDING("Donkey Riding", "\uD83D\uDC34"),
    MULE_RIDING("Mule Riding", "\uD83D\uDC34"),
    BOAT("Boat", "\u26F5"),
    MINECART("Minecart", "\uD83D\uDE83"),
    RAILS("Rails", "\uD83D\uDEE4"),
    ELYTRA("Elytra", "\uD83E\uDD85"),
    PARACHUTE("Parachute", "\uD83E\uDE82"),

    // ── UTILITY & SUPPORT ────────────────────────────────────────────
    UTILITY("Utility", "\uD83D\uDD27"),
    SADDLE("Saddle", "\uD83D\uDC0E"),
    TOOL("Tools", "\uD83D\uDEE0"),
    LIGHTING("Lighting", "\uD83D\uDCA1"),
    MAP("Mapping", "\uD83D\uDDFA"),
    COMPASS("Compass", "\uD83E\uDDED"),
    CLOCK("Clock", "\uD83D\uDD50"),
    SPYGLASS("Spyglass", "\uD83D\uDD2D"),
    NUTRITION("Nutrition", "\uD83C\uDF56"),
    FOOD("Food", "\uD83E\uDD69"),
    HEALING("Healing", "\u2764"),
    REGENERATION("Regeneration", "\uD83D\uDC9A"),
    TELEPORT("Teleport", "\uD83C\uDF00"),
    FIREWORK("Firework", "\uD83C\uDF86"),
    BANNER("Banner", "\uD83D\uDEA9"),
    SIGN("Sign", "\uD83D\uDCDD"),
    BOOK("Book", "\uD83D\uDCD6"),
    WRITING("Writing", "\u270F"),
    LEASH("Leash", "\uD83D\uDD17"),
    ENDER("Ender", "\uD83D\uDFE3"),
    BUCKET_USE("Bucket", "\uD83E\uDEA3"),
    SLEEPING("Sleeping", "\uD83D\uDECF"),
    AFK("Idle", "\uD83D\uDCA4"),
    IDLE("Idle", "\uD83D\uDCA4"),

    // ── MISC ─────────────────────────────────────────────────────────
    GENERAL("General", "\uD83C\uDF92"),
    UNKNOWN("Unknown", "\u2753");

    public final String displayName;
    public final String icon;

    ActivityType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public ActivityType next() {
        ActivityType[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public ActivityType prev() {
        ActivityType[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
}
