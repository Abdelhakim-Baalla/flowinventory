package com.flowinventory.server;

import com.flowinventory.FlowInventoryMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Advanced heuristics engine for evaluating item suitability for activities.
 * Uses ItemDatabase for comprehensive item classification and scoring.
 */
public class ItemHeuristics {

    // ==================== ENCHANTMENT WEIGHTS ====================
    private static final Map<Enchantment, Integer> COMBAT_ENCHANT_WEIGHTS = new HashMap<>();
    private static final Map<Enchantment, Integer> MINING_ENCHANT_WEIGHTS = new HashMap<>();
    private static final Map<Enchantment, Integer> UTILITY_ENCHANT_WEIGHTS = new HashMap<>();

    static {
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.SHARPNESS, 15);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.SMITE, 20);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.BANE_OF_ARTHROPODS, 20);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.KNOCKBACK, 10);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.FIRE_ASPECT, 12);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.LOOTING, 8);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.MENDING, 25);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.UNBREAKING, 10);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.VANISHING_CURSE, -50);
        COMBAT_ENCHANT_WEIGHTS.put(Enchantments.BINDING_CURSE, -50);

        MINING_ENCHANT_WEIGHTS.put(Enchantments.EFFICIENCY, 25);
        MINING_ENCHANT_WEIGHTS.put(Enchantments.SILK_TOUCH, 30);
        MINING_ENCHANT_WEIGHTS.put(Enchantments.FORTUNE, 40);
        MINING_ENCHANT_WEIGHTS.put(Enchantments.UNBREAKING, 10);
        MINING_ENCHANT_WEIGHTS.put(Enchantments.MENDING, 25);
        MINING_ENCHANT_WEIGHTS.put(Enchantments.VANISHING_CURSE, -50);
        MINING_ENCHANT_WEIGHTS.put(Enchantments.BINDING_CURSE, -50);

        UTILITY_ENCHANT_WEIGHTS.put(Enchantments.UNBREAKING, 15);
        UTILITY_ENCHANT_WEIGHTS.put(Enchantments.MENDING, 30);
        UTILITY_ENCHANT_WEIGHTS.put(Enchantments.VANISHING_CURSE, -50);
        UTILITY_ENCHANT_WEIGHTS.put(Enchantments.BINDING_CURSE, -50);
    }

    // ==================== ENCHANTMENT LEVEL MULTIPLIERS ====================
    private static final Map<Integer, Integer> ENCHANT_LEVEL_MULTIPLIER = new HashMap<>();
    static {
        ENCHANT_LEVEL_MULTIPLIER.put(1, 1);
        ENCHANT_LEVEL_MULTIPLIER.put(2, 2);
        ENCHANT_LEVEL_MULTIPLIER.put(3, 3);
        ENCHANT_LEVEL_MULTIPLIER.put(4, 4);
        ENCHANT_LEVEL_MULTIPLIER.put(5, 5);
        ENCHANT_LEVEL_MULTIPLIER.put(6, 6);
        ENCHANT_LEVEL_MULTIPLIER.put(7, 7);
        ENCHANT_LEVEL_MULTIPLIER.put(8, 8);
        ENCHANT_LEVEL_MULTIPLIER.put(9, 9);
        ENCHANT_LEVEL_MULTIPLIER.put(10, 10);
    }

    /**
     * Main evaluation entry point
     */
    public static int evaluate(ItemStack stack, String type) {
        if (stack.isEmpty()) return -1;

        Item item = stack.getItem();
        String itemId = Registries.ITEM.getId(item).toString();
        String itemPath = new Identifier(itemId).getPath();

        int baseScore = getBaseScoreForType(item, type, itemId, itemPath, stack);
        if (baseScore <= 0) return -1;

        baseScore = applyDurabilityModifier(stack, baseScore);
        baseScore = applyEnchantmentBonuses(stack, type, baseScore);
        baseScore = applyCountModifier(stack, type, baseScore);
        baseScore = applyNBTModifier(stack, baseScore);
        baseScore = applyTierScaling(item, baseScore);

        return baseScore;
    }

    private static int getBaseScoreForType(Item item, String type, String itemId, String itemPath, ItemStack stack) {
        String upperType = type.toUpperCase();

        switch (upperType) {
            case "SWORD":
                if (item instanceof SwordItem) return 100 + getTierBonus(item);
                return -1;

            case "AXE_COMBAT":
                if (item instanceof AxeItem) return 90 + getTierBonus(item);
                return -1;

            case "TRIDENT":
                if (item instanceof TridentItem) return 110;
                return -1;

            case "MACE":
                if (itemPath.contains("mace")) return 120;
                return -1;

            case "SHIELD":
                if (item instanceof ShieldItem) return 100;
                return -1;

            case "BOW":
                if (item instanceof BowItem) return 95;
                return -1;

            case "ARROW":
                if (item instanceof ArrowItem) return 80 + stack.getCount() / 16;
                return -1;

            case "PICKAXE":
                if (item instanceof PickaxeItem) return 100 + getTierBonus(item);
                return -1;

            case "SHOVEL":
                if (item instanceof ShovelItem) return 85 + getTierBonus(item);
                return -1;

            case "AXE_TOOL":
                if (item instanceof AxeItem) return 90 + getTierBonus(item);
                return -1;

            case "HOE":
                if (item instanceof HoeItem) return 50 + getTierBonus(item);
                return -1;

            case "HELMET":
                if (item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.HELMET) 
                    return 70 + getArmorRating(armor);
                return -1;

            case "CHESTPLATE":
                if (item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.CHESTPLATE) 
                    return 75 + getArmorRating(armor);
                return -1;

            case "LEGGINGS":
                if (item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.LEGGINGS) 
                    return 72 + getArmorRating(armor);
                return -1;

            case "BOOTS":
                if (item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.BOOTS) 
                    return 68 + getArmorRating(armor);
                return -1;

            case "FOOD":
                return getFoodScore(stack);

            case "BLOCK":
                if (item instanceof BlockItem) {
                    int score = 50 + getBlockUtilityScore(item);
                    if (stack.getCount() >= 64) score += 20;
                    return score;
                }
                return -1;

            case "POTION":
                if (item instanceof PotionItem) return 100;
                if (item instanceof ExperienceBottleItem) return 70;
                return -1;

            case "BREWING_INGREDIENT":
                if (itemPath.contains("nether_wart")) return 100;
                if (itemPath.contains("blaze_powder")) return 95;
                if (itemPath.contains("ghast_tear")) return 90;
                if (itemPath.contains("magma_cream")) return 88;
                if (itemPath.contains("spider_eye")) return 82;
                if (itemPath.contains("fermented_spider_eye")) return 85;
                if (itemPath.contains("glistering_melon")) return 80;
                if (itemPath.contains("golden_carrot")) return 80;
                if (itemPath.contains("sugar")) return 75;
                if (itemPath.contains("dragon_breath")) return 95;
                return -1;

            case "ENCHANTING":
                if (item instanceof EnchantedBookItem) return 95;
                if (item instanceof ExperienceBottleItem) return 85;
                if (itemPath.contains("lapis_lazuli")) return 90;
                return -1;

            case "HORSE_ARMOR":
                if (itemPath.contains("horse_armor")) return 60;
                return -1;

            case "COMPASS":
                if (itemPath.contains("compass")) return 100;
                return -1;

            case "NETHER_GEM":
                if (itemPath.contains("netherite")) return 100;
                if (itemPath.contains("ancient_debris")) return 98;
                return -1;

            case "ENDER":
                if (itemPath.contains("ender_pearl")) return 100;
                if (itemPath.contains("eye_of_ender")) return 95;
                return -1;

            case "BLAZE":
                if (itemPath.contains("blaze_rod")) return 100;
                if (itemPath.contains("blaze_powder")) return 95;
                return -1;

            case "NETHERRACK":
                if (itemPath.contains("netherrack")) return 75;
                if (itemPath.contains("nether_wart")) return 95;
                return -1;

            case "GEM":
                if (itemPath.contains("diamond")) return 100;
                if (itemPath.contains("emerald")) return 100;
                if (itemPath.contains("amethyst")) return 88;
                return -1;

            case "TOOL":
                if (item instanceof ShearsItem) return 75;
                if (itemPath.contains("flint_and_steel")) return 70;
                if (item instanceof FishingRodItem) return 90;
                if (itemPath.contains("lead")) return 85;
                if (itemPath.contains("saddle")) return 95;
                if (item instanceof SpyglassItem) return 78;
                return -1;

            case "BUCKET":
                if (item instanceof BucketItem) {
                    if (itemPath.contains("water_bucket")) return 100;
                    if (itemPath.contains("lava_bucket")) return 90;
                    if (itemPath.contains("milk_bucket")) return 85;
                    if (itemPath.contains("bucket")) return 60;
                }
                return -1;

            case "TORCH":
                if (itemPath.contains("torch")) return 90;
                return -1;

            case "LANTERN":
                if (itemPath.contains("lantern")) return 88;
                return -1;

            // ──────────────── Extended item type matchers ────────────────

            case "CROSSBOW":
                if (itemPath.equals("crossbow")) return 100;
                return -1;

            case "TNT":
                if (itemPath.equals("tnt")) return 100;
                if (itemPath.equals("tnt_minecart")) return 90;
                if (itemPath.equals("fire_charge")) return 70;
                if (itemPath.equals("wind_charge")) return 65;
                return -1;

            case "WIND_CHARGE":
                if (itemPath.equals("wind_charge")) return 100;
                if (itemPath.equals("breeze_rod")) return 80;
                if (itemPath.equals("tnt")) return 50;
                return -1;

            case "FISHING_ROD":
                if (item instanceof FishingRodItem) return 100;
                if (itemPath.equals("carrot_on_a_stick")) return 70;
                if (itemPath.equals("warped_fungus_on_a_stick")) return 70;
                return -1;

            case "ENCHANTED_FISHING_ROD":
                if (item instanceof FishingRodItem && stack.hasEnchantments()) return 110;
                if (item instanceof FishingRodItem) return 60;
                return -1;

            case "FIREWORK":
                if (itemPath.equals("firework_rocket")) return 100;
                if (itemPath.equals("firework_star")) return 70;
                return -1;

            case "FIREWORK_STAR":
                if (itemPath.equals("firework_star")) return 100;
                return -1;

            case "MAP":
                if (itemPath.equals("map") || itemPath.equals("filled_map")) return 100;
                return -1;

            case "FILLED_MAP":
                if (itemPath.equals("filled_map")) return 100;
                if (itemPath.equals("map")) return 50;
                return -1;

            case "RECOVERY_COMPASS":
                if (itemPath.equals("recovery_compass")) return 100;
                if (itemPath.contains("compass")) return 50;
                return -1;

            case "LODESTONE_COMPASS":
                if (itemPath.equals("lodestone_compass")) return 100;
                if (itemPath.contains("compass")) return 50;
                return -1;

            case "CLOCK":
                if (itemPath.equals("clock")) return 100;
                return -1;

            case "SPYGLASS":
                if (itemPath.equals("spyglass")) return 100;
                return -1;

            case "BANNER":
                if (itemPath.contains("banner") && !itemPath.contains("pattern")) return 100;
                if (itemPath.contains("banner_pattern")) return 80;
                return -1;

            case "BANNER_PATTERN":
                if (itemPath.contains("banner_pattern")) return 100;
                return -1;

            case "SIGN":
            case "OAK_SIGN":
            case "SPRUCE_SIGN":
            case "BIRCH_SIGN":
                if (itemPath.contains("_sign")) return 100;
                return -1;

            case "BOOK":
                if (itemPath.equals("book")) return 100;
                if (itemPath.equals("writable_book")) return 95;
                if (itemPath.equals("written_book")) return 95;
                if (itemPath.equals("enchanted_book")) return 110;
                if (itemPath.equals("knowledge_book")) return 90;
                return -1;

            case "ENCHANTED_BOOK":
                if (itemPath.equals("enchanted_book")) return 100;
                return -1;

            case "WRITABLE_BOOK":
                if (itemPath.equals("writable_book")) return 100;
                if (itemPath.equals("written_book")) return 80;
                return -1;

            case "WRITTEN_BOOK":
                if (itemPath.equals("written_book")) return 100;
                return -1;

            case "BOOKSHELF":
                if (itemPath.contains("bookshelf")) return 100;
                return -1;

            case "QUILL":
                if (itemPath.equals("writable_book")) return 100;
                if (itemPath.equals("feather")) return 60;
                return -1;

            case "INK_SAC":
                if (itemPath.equals("ink_sac")) return 100;
                if (itemPath.equals("glow_ink_sac")) return 90;
                return -1;

            case "GLOW_INK_SAC":
                if (itemPath.equals("glow_ink_sac")) return 100;
                if (itemPath.equals("ink_sac")) return 50;
                return -1;

            case "DYE":
                if (itemPath.endsWith("_dye")) return 100;
                if (itemPath.equals("ink_sac") || itemPath.equals("bone_meal")
                        || itemPath.equals("cocoa_beans") || itemPath.equals("lapis_lazuli")) {
                    return 90;
                }
                return -1;

            case "WOOL":
                if (itemPath.endsWith("_wool")) return 100;
                return -1;

            case "CARPET":
                if (itemPath.endsWith("_carpet")) return 100;
                if (itemPath.equals("moss_carpet")) return 80;
                return -1;

            case "BED":
                if (itemPath.endsWith("_bed")) return 100;
                return -1;

            case "GLASS":
                if (itemPath.equals("glass") || itemPath.equals("tinted_glass")) return 100;
                if (itemPath.endsWith("_stained_glass") || itemPath.endsWith("_stained_glass_pane")) return 90;
                if (itemPath.equals("glass_pane")) return 85;
                return -1;

            case "STAINED_GLASS":
                if (itemPath.endsWith("_stained_glass")) return 100;
                if (itemPath.endsWith("_stained_glass_pane")) return 90;
                if (itemPath.equals("glass") || itemPath.equals("glass_pane")) return 70;
                return -1;

            case "GLASS_PANE":
            case "STAINED_GLASS_PANE":
                if (itemPath.endsWith("_pane")) return 100;
                if (itemPath.contains("glass")) return 60;
                return -1;

            case "GLASS_BOTTLE":
                if (itemPath.equals("glass_bottle")) return 100;
                if (itemPath.equals("honey_bottle")) return 60;
                return -1;

            case "BOTTLE":
                if (itemPath.equals("glass_bottle")) return 100;
                if (itemPath.equals("experience_bottle")) return 90;
                if (itemPath.equals("honey_bottle")) return 70;
                return -1;

            case "PAPER":
                if (itemPath.equals("paper")) return 100;
                return -1;

            case "STICK":
                if (itemPath.equals("stick")) return 100;
                if (itemPath.equals("blaze_rod")) return 90;
                if (itemPath.equals("breeze_rod")) return 90;
                return -1;

            case "PAINTING":
                if (itemPath.equals("painting")) return 100;
                return -1;

            case "FLOWER":
                if (ItemDatabase.isInCategory(itemId, "FLOWER")) return 100;
                return -1;

            case "MUSHROOM":
                if (itemPath.contains("mushroom") || itemPath.contains("fungus")) return 100;
                return -1;

            case "STAIRS":
                if (itemPath.endsWith("_stairs")) return 100;
                return -1;

            case "SLAB":
                if (itemPath.endsWith("_slab")) return 100;
                return -1;

            case "WALL":
                if (itemPath.endsWith("_wall")) return 100;
                return -1;

            case "FENCE":
                if (itemPath.endsWith("_fence")) return 100;
                if (itemPath.endsWith("_fence_gate")) return 80;
                return -1;

            case "DOOR":
                if (itemPath.endsWith("_door")) return 100;
                return -1;

            case "TRAPDOOR":
                if (itemPath.endsWith("_trapdoor")) return 100;
                return -1;

            case "BUTTON":
                if (itemPath.endsWith("_button")) return 100;
                return -1;

            case "PRESSURE_PLATE":
                if (itemPath.endsWith("_pressure_plate")) return 100;
                return -1;

            case "LADDER":
                if (itemPath.equals("ladder")) return 100;
                if (itemPath.equals("scaffolding")) return 80;
                if (itemPath.contains("vine")) return 60;
                return -1;

            case "PISTON":
                if (itemPath.equals("piston")) return 100;
                if (itemPath.equals("sticky_piston")) return 100;
                return -1;

            case "STICKY_PISTON":
                if (itemPath.equals("sticky_piston")) return 100;
                if (itemPath.equals("piston")) return 70;
                return -1;

            case "OBSERVER":
                if (itemPath.equals("observer")) return 100;
                return -1;

            case "REPEATER":
                if (itemPath.equals("repeater")) return 100;
                return -1;

            case "COMPARATOR":
                if (itemPath.equals("comparator")) return 100;
                return -1;

            case "DAYLIGHT_SENSOR":
                if (itemPath.equals("daylight_detector")) return 100;
                return -1;

            case "TARGET":
                if (itemPath.equals("target")) return 100;
                return -1;

            case "LEVER":
                if (itemPath.equals("lever")) return 100;
                return -1;

            case "HOPPER":
                if (itemPath.equals("hopper")) return 100;
                if (itemPath.equals("hopper_minecart")) return 80;
                return -1;

            case "DROPPER":
                if (itemPath.equals("dropper")) return 100;
                return -1;

            case "DISPENSER":
                if (itemPath.equals("dispenser")) return 100;
                return -1;

            case "CHEST":
                if (itemPath.equals("chest")) return 100;
                if (itemPath.equals("trapped_chest")) return 95;
                if (itemPath.contains("ender_chest")) return 90;
                return -1;

            case "FURNACE":
                if (itemPath.equals("furnace")) return 100;
                if (itemPath.equals("blast_furnace")) return 95;
                if (itemPath.equals("smoker")) return 95;
                return -1;

            case "RAIL":
                if (itemPath.contains("rail")) return 100;
                return -1;

            case "POWERED_RAIL":
                if (itemPath.equals("powered_rail")) return 100;
                if (itemPath.contains("rail")) return 60;
                return -1;

            case "DETECTOR_RAIL":
                if (itemPath.equals("detector_rail")) return 100;
                if (itemPath.contains("rail")) return 60;
                return -1;

            case "ACTIVATOR_RAIL":
                if (itemPath.equals("activator_rail")) return 100;
                if (itemPath.contains("rail")) return 60;
                return -1;

            case "MINECART":
                if (itemPath.contains("minecart")) return 100;
                return -1;

            case "CHEST_MINECART":
                if (itemPath.equals("chest_minecart")) return 100;
                if (itemPath.contains("minecart")) return 60;
                return -1;

            case "HOPPER_MINECART":
                if (itemPath.equals("hopper_minecart")) return 100;
                if (itemPath.contains("minecart")) return 60;
                return -1;

            case "BOAT":
                if (itemPath.contains("_boat") || itemPath.contains("_raft")) return 100;
                return -1;

            case "OAR":
                if (itemPath.contains("_boat") || itemPath.contains("_raft")) return 100;
                return -1;

            case "ELYTRA":
                if (itemPath.equals("elytra")) return 100;
                return -1;

            case "SADDLE":
                if (itemPath.equals("saddle")) return 100;
                return -1;

            case "LEAD":
                if (itemPath.equals("lead")) return 100;
                if (itemPath.equals("string")) return 50;
                return -1;

            case "NAME_TAG":
                if (itemPath.equals("name_tag")) return 100;
                return -1;

            case "EGG":
                if (itemPath.equals("egg")) return 100;
                if (itemPath.contains("turtle_egg")) return 60;
                return -1;

            case "CARROT":
                if (itemPath.equals("carrot") || itemPath.equals("golden_carrot")) return 100;
                if (itemPath.equals("carrot_on_a_stick")) return 80;
                return -1;

            case "POTATO":
                if (itemPath.equals("potato") || itemPath.equals("baked_potato")) return 100;
                if (itemPath.equals("poisonous_potato")) return -1;
                return -1;

            case "BEETROOT":
                if (itemPath.equals("beetroot") || itemPath.equals("beetroot_soup")
                        || itemPath.equals("beetroot_seeds")) return 100;
                return -1;

            case "WHEAT":
                if (itemPath.equals("wheat")) return 100;
                if (itemPath.equals("wheat_seeds")) return 90;
                if (itemPath.equals("hay_block")) return 70;
                return -1;

            case "WHEAT_SEEDS":
            case "SEEDS":
                if (itemPath.endsWith("_seeds")) return 100;
                if (itemPath.endsWith("_pod")) return 90;
                if (itemPath.contains("sapling")) return 60;
                return -1;

            case "BAMBOO":
                if (itemPath.equals("bamboo")) return 100;
                if (itemPath.contains("bamboo")) return 70;
                return -1;

            case "KELP":
                if (itemPath.equals("kelp") || itemPath.equals("dried_kelp")
                        || itemPath.equals("dried_kelp_block")) return 100;
                return -1;

            case "SUGAR_CANE":
                if (itemPath.equals("sugar_cane") || itemPath.equals("sugar")) return 100;
                return -1;

            case "BONE_MEAL":
                if (itemPath.equals("bone_meal")) return 100;
                if (itemPath.equals("bone")) return 70;
                if (itemPath.equals("bone_block")) return 50;
                return -1;

            case "MELON":
                if (itemPath.equals("melon") || itemPath.equals("melon_slice")
                        || itemPath.equals("glistering_melon_slice")) return 100;
                return -1;

            case "BREAD":
                if (itemPath.equals("bread")) return 100;
                if (itemPath.equals("wheat")) return 70;
                return -1;

            case "STEAK":
                if (itemPath.equals("cooked_beef")) return 100;
                if (itemPath.equals("cooked_porkchop")) return 95;
                if (itemPath.equals("cooked_mutton")) return 90;
                if (itemPath.equals("cooked_chicken")) return 85;
                return -1;

            case "GOLDEN_CARROT":
                if (itemPath.equals("golden_carrot")) return 100;
                if (itemPath.equals("carrot")) return 50;
                return -1;

            case "GOLDEN_APPLE":
                if (itemPath.equals("enchanted_golden_apple")) return 110;
                if (itemPath.equals("golden_apple")) return 100;
                return -1;

            case "ENCHANTED_GOLDEN_APPLE":
                if (itemPath.equals("enchanted_golden_apple")) return 100;
                if (itemPath.equals("golden_apple")) return 70;
                return -1;

            case "MILK_BUCKET":
                if (itemPath.equals("milk_bucket")) return 100;
                return -1;

            case "WATER_BUCKET":
                if (itemPath.equals("water_bucket")) return 100;
                if (itemPath.equals("bucket")) return 30;
                return -1;

            case "LAVA_BUCKET":
                if (itemPath.equals("lava_bucket")) return 100;
                return -1;

            case "POWDER_SNOW_BUCKET":
                if (itemPath.equals("powder_snow_bucket")) return 100;
                return -1;

            case "AXOLOTL_BUCKET":
                if (itemPath.equals("axolotl_bucket")) return 100;
                return -1;

            case "HONEY_BOTTLE":
                if (itemPath.equals("honey_bottle")) return 100;
                return -1;

            case "COOKIE":
                if (itemPath.equals("cookie")) return 100;
                return -1;

            case "CAKE":
                if (itemPath.equals("cake")) return 100;
                return -1;

            // ── Brewing-specific ─────────────────────────────────────
            case "NETHER_WART":
                if (itemPath.equals("nether_wart")) return 100;
                return -1;
            case "BLAZE_POWDER":
                if (itemPath.equals("blaze_powder")) return 100;
                if (itemPath.equals("blaze_rod")) return 80;
                return -1;
            case "BLAZE_ROD":
                if (itemPath.equals("blaze_rod")) return 100;
                return -1;
            case "GHAST_TEAR":
                if (itemPath.equals("ghast_tear")) return 100;
                return -1;
            case "DRAGON_BREATH":
                if (itemPath.equals("dragon_breath")) return 100;
                return -1;
            case "GUNPOWDER":
                if (itemPath.equals("gunpowder")) return 100;
                return -1;
            case "REDSTONE":
                if (itemPath.equals("redstone")) return 100;
                if (itemPath.equals("redstone_block")) return 80;
                return -1;
            case "GLOWSTONE":
                if (itemPath.equals("glowstone")) return 100;
                if (itemPath.equals("glowstone_dust")) return 90;
                return -1;
            case "TIP_ARROW":
                if (itemPath.equals("tipped_arrow")) return 100;
                if (itemPath.equals("arrow")) return 50;
                return -1;
            case "SPECTRAL_ARROW":
                if (itemPath.equals("spectral_arrow")) return 100;
                if (itemPath.equals("arrow")) return 50;
                return -1;

            // ── Splash variants ──────────────────────────────────────
            case "SPLASH_POTION":
                if (itemPath.equals("splash_potion")) return 100;
                return -1;
            case "LINGERING_POTION":
                if (itemPath.equals("lingering_potion")) return 100;
                return -1;

            // ── Specific potions (looked up by NBT but path catches names) ──
            case "FIRE_RESISTANCE":
            case "HEALING_POTION":
            case "STRENGTH_POTION":
            case "FIRESTANCE_POTION":
                if (itemPath.contains("potion")) return 100;
                if (itemPath.equals("milk_bucket")) return 60;
                return -1;

            case "RESPIRATION_HELMET":
                if (itemPath.contains("helmet") && stack.hasEnchantments()) return 100;
                if (itemPath.equals("turtle_helmet")) return 95;
                if (itemPath.contains("helmet")) return 70;
                return -1;

            // ── Misc / specials ──────────────────────────────────────
            case "SHULKER":
                if (itemPath.contains("shulker_box")) return 100;
                if (itemPath.equals("shulker_shell")) return 90;
                return -1;
            case "SHULKER_BOX":
                if (itemPath.contains("shulker_box")) return 100;
                if (itemPath.equals("shulker_shell")) return 80;
                return -1;
            case "ENDER_CHEST":
                if (itemPath.equals("ender_chest")) return 100;
                if (itemPath.equals("chest")) return 50;
                return -1;
            case "ENDER_PEARL":
                if (itemPath.equals("ender_pearl")) return 100;
                if (itemPath.equals("ender_eye")) return 90;
                return -1;
            case "ENDER_EYE":
                if (itemPath.equals("ender_eye") || itemPath.equals("eye_of_ender")) return 100;
                if (itemPath.equals("ender_pearl")) return 60;
                return -1;
            case "CHORUS_FRUIT":
                if (itemPath.equals("chorus_fruit")) return 100;
                if (itemPath.equals("popped_chorus_fruit")) return 70;
                return -1;
            case "END_CRYSTAL":
                if (itemPath.equals("end_crystal")) return 100;
                return -1;
            case "DIAMOND":
                if (itemPath.equals("diamond")) return 100;
                if (itemPath.equals("diamond_block")) return 95;
                return -1;
            case "EMERALD":
                if (itemPath.equals("emerald")) return 100;
                if (itemPath.equals("emerald_block")) return 95;
                return -1;
            case "GOLD_INGOT":
                if (itemPath.equals("gold_ingot")) return 100;
                if (itemPath.equals("gold_block")) return 90;
                if (itemPath.equals("gold_nugget")) return 60;
                return -1;
            case "RAW_IRON":
                if (itemPath.equals("raw_iron")) return 100;
                if (itemPath.equals("iron_ore")) return 90;
                if (itemPath.equals("deepslate_iron_ore")) return 90;
                return -1;
            case "NETHERITE_INGOT":
                if (itemPath.equals("netherite_ingot")) return 100;
                if (itemPath.equals("netherite_scrap")) return 80;
                return -1;
            case "SMITHING_TEMPLATE":
                if (itemPath.contains("smithing_template")) return 100;
                return -1;

            // ── Crafting tables / workbenches ────────────────────────
            case "ANVIL":
                if (itemPath.contains("anvil")) return 100;
                return -1;
            case "GRINDSTONE":
                if (itemPath.equals("grindstone")) return 100;
                return -1;
            case "STONECUTTER":
                if (itemPath.equals("stonecutter")) return 100;
                return -1;
            case "LOOM":
                if (itemPath.equals("loom")) return 100;
                return -1;
            case "COMPOSTER":
                if (itemPath.equals("composter")) return 100;
                return -1;

            case "WEAPON":
                if (item instanceof SwordItem) return 100;
                if (item instanceof AxeItem) return 90;
                if (item instanceof TridentItem) return 95;
                if (itemPath.contains("mace")) return 95;
                if (item instanceof BowItem) return 85;
                return -1;

            // ── Dimension shortcuts (covered by HotbarSwapper) ───────
            case "AXE":
                if (item instanceof AxeItem) return 100;
                return -1;
            case "PLANKS":
                if (itemPath.endsWith("_planks")) return 100;
                return -1;
            case "STONE_BRICKS":
                if (itemPath.contains("stone_bricks") || itemPath.contains("stone_brick")) return 100;
                if (itemPath.contains("stone")) return 50;
                return -1;
            case "STONE":
                if (itemPath.equals("stone") || itemPath.equals("cobblestone")
                        || itemPath.contains("deepslate")) return 100;
                if (item instanceof BlockItem) return 30;
                return -1;
            case "TERRACOTTA":
                if (itemPath.contains("terracotta")) return 100;
                return -1;
            case "CONCRETE":
                if (itemPath.contains("concrete")) return 100;
                return -1;
            case "SCULK":
                if (itemPath.contains("sculk")) return 100;
                return -1;
            case "WARPED_FUNGUS":
                if (itemPath.equals("warped_fungus")) return 100;
                if (itemPath.equals("warped_fungus_on_a_stick")) return 95;
                return -1;
            case "CACTUS":
                if (itemPath.equals("cactus")) return 100;
                return -1;
            case "ROPE":
                if (itemPath.equals("lead") || itemPath.equals("string")) return 100;
                return -1;
            case "MACHETE":
                // No vanilla machete — best substitute is an axe/sword
                if (item instanceof AxeItem) return 100;
                if (item instanceof SwordItem) return 90;
                return -1;
            case "BUCKET_USE":
                if (item instanceof BucketItem) return 100;
                return -1;
            case "BLAST_RESISTANT_BLOCK":
                if (itemPath.contains("obsidian")) return 100;
                if (itemPath.contains("ancient_debris")) return 95;
                if (itemPath.contains("crying_obsidian")) return 95;
                if (itemPath.contains("netherite_block")) return 90;
                if (item instanceof BlockItem) return 30;
                return -1;
            case "SCRAPE":
                if (itemPath.equals("brush")) return 100;
                return -1;
            case "BOWL":
                if (itemPath.equals("bowl")) return 100;
                if (itemPath.contains("stew") || itemPath.contains("soup")) return 60;
                return -1;
            case "DRAGON_HEAD":
                if (itemPath.equals("dragon_head")) return 100;
                return -1;

            case "TOTEM":
            case "TOTEM_OF_UNDYING":
                if (itemPath.equals("totem_of_undying")) return 200;
                if (itemPath.equals("shield")) return 90; // safe fallback
                if (itemPath.contains("golden_apple")) return 60;
                return -1;

            // ──────────────── Block-material types ──────────────────────
            case "LOG":
                if (itemPath.endsWith("_log") || itemPath.endsWith("_stem")
                        || itemPath.endsWith("_wood") || itemPath.endsWith("_hyphae")) return 100;
                if (itemPath.endsWith("_planks")) return 50;
                return -1;

            case "LEAVES":
                if (itemPath.endsWith("_leaves") || itemPath.equals("azalea_leaves")
                        || itemPath.equals("flowering_azalea_leaves")) return 100;
                return -1;

            case "SAPLING":
                if (itemPath.endsWith("_sapling") || itemPath.endsWith("_propagule")) return 100;
                return -1;

            case "PUMPKIN":
                if (itemPath.equals("pumpkin") || itemPath.equals("carved_pumpkin")
                        || itemPath.equals("jack_o_lantern")) return 100;
                if (itemPath.equals("pumpkin_pie")) return 50;
                return -1;

            case "WEB":
                if (itemPath.equals("cobweb")) return 100;
                if (itemPath.equals("string")) return 60;
                return -1;

            case "CONDUIT":
                if (itemPath.equals("conduit")) return 100;
                if (itemPath.equals("heart_of_the_sea")) return 70;
                if (itemPath.equals("nautilus_shell")) return 50;
                return -1;

            // ──────────────── Light / fire / candle ──────────────────────
            case "FLINT_AND_STEEL":
                if (itemPath.equals("flint_and_steel")) return 100;
                if (itemPath.equals("fire_charge")) return 60;
                return -1;

            case "FIRE_CHARGE":
                if (itemPath.equals("fire_charge")) return 100;
                if (itemPath.equals("flint_and_steel")) return 70;
                return -1;

            case "CANDLE":
                if (itemPath.endsWith("candle") || itemPath.endsWith("candle_cake")) return 100;
                return -1;

            case "REDSTONE_TORCH":
                if (itemPath.equals("redstone_torch")) return 100;
                if (itemPath.equals("torch") || itemPath.equals("soul_torch")) return 40;
                return -1;

            case "LIGHTNING_ROD":
                if (itemPath.equals("lightning_rod")) return 100;
                return -1;

            // ──────────────── Throwables / random ────────────────────────
            case "SNOW_BALL":
            case "SNOWBALL":
                if (itemPath.equals("snowball")) return 100;
                if (itemPath.equals("snow_block") || itemPath.equals("snow")) return 60;
                return -1;

            case "EGG_THROW":
                if (itemPath.equals("egg")) return 100;
                if (itemPath.equals("snowball")) return 60;
                return -1;

            case "BREEZE_EGG":
                if (itemPath.equals("breeze_spawn_egg")) return 100;
                if (itemPath.endsWith("_spawn_egg")) return 60;
                return -1;

            // ──────────────── Lapis / metals ─────────────────────────────
            case "LAPIS":
                if (itemPath.equals("lapis_lazuli")) return 100;
                if (itemPath.equals("lapis_block")) return 90;
                if (itemPath.equals("lapis_ore") || itemPath.equals("deepslate_lapis_ore")) return 60;
                return -1;

            case "INGOT":
                if (itemPath.endsWith("_ingot")) return 100;
                if (itemPath.endsWith("_nugget")) return 50;
                if (itemPath.endsWith("_block") && (
                        itemPath.contains("iron") || itemPath.contains("gold")
                                || itemPath.contains("netherite") || itemPath.contains("copper"))) return 70;
                return -1;

            case "IRON_INGOT":
                if (itemPath.equals("iron_ingot")) return 100;
                if (itemPath.equals("iron_block")) return 85;
                if (itemPath.equals("iron_nugget")) return 55;
                return -1;

            case "COPPER_INGOT":
                if (itemPath.equals("copper_ingot")) return 100;
                if (itemPath.equals("copper_block")) return 85;
                return -1;

            case "COAL":
                if (itemPath.equals("coal")) return 100;
                if (itemPath.equals("charcoal")) return 95;
                if (itemPath.equals("coal_block")) return 80;
                return -1;

            // ──────────────── Armor specifics ────────────────────────────
            case "LEATHER_BOOTS":
                if (itemPath.equals("leather_boots")) return 100;
                if (item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.BOOTS) return 70;
                return -1;

            case "TURTLE_HELMET":
                if (itemPath.equals("turtle_helmet")) return 100;
                if (item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.HELMET) return 60;
                return -1;

            // ──────────────── Workstation block items ────────────────────
            case "CRAFTING_TABLE":
                if (itemPath.equals("crafting_table")) return 100;
                return -1;

            case "ENCHANTING_TABLE":
                if (itemPath.equals("enchanting_table")) return 100;
                return -1;

            case "BREWING_STAND":
                if (itemPath.equals("brewing_stand")) return 100;
                return -1;

            case "SMITHING_TABLE":
                if (itemPath.equals("smithing_table")) return 100;
                return -1;

            case "CARTOGRAPHY_TABLE":
                if (itemPath.equals("cartography_table")) return 100;
                return -1;

            // ──────────────── Misc materials ─────────────────────────────
            case "SHELL":
                if (itemPath.equals("nautilus_shell")) return 100;
                if (itemPath.equals("scute")) return 90;
                if (itemPath.equals("turtle_scute")) return 90;
                return -1;

            case "FEATHER":
                if (itemPath.equals("feather")) return 100;
                return -1;

            case "STRING":
                if (itemPath.equals("string")) return 100;
                if (itemPath.equals("cobweb")) return 60;
                return -1;

            case "BONE":
                if (itemPath.equals("bone")) return 100;
                if (itemPath.equals("bone_meal")) return 80;
                if (itemPath.equals("bone_block")) return 60;
                return -1;

            // ──────────────── Pottery / brushable ────────────────────────
            case "BRUSH":
                if (itemPath.equals("brush")) return 100;
                return -1;

            case "POT_SHARD":
                if (itemPath.endsWith("_pottery_sherd")) return 100;
                return -1;

            default:
                return getFallbackScore(item, type);
        }
    }

    private static int getTierBonus(Item item) {
        String itemId = Registries.ITEM.getId(item).toString();
        String path = new Identifier(itemId).getPath();

        if (path.contains("netherite")) return 15;
        if (path.contains("diamond")) return 10;
        if (path.contains("iron")) return 5;
        if (path.contains("stone")) return 2;
        if (path.contains("wooden")) return 0;
        if (path.contains("golden")) return -5;

        return 0;
    }

    private static int getArmorRating(ArmorItem armor) {
        String itemId = Registries.ITEM.getId(armor).toString();
        String path = new Identifier(itemId).getPath();

        if (path.contains("netherite")) return 8;
        if (path.contains("diamond")) return 8;
        if (path.contains("iron")) return 6;
        if (path.contains("chainmail")) return 5;
        if (path.contains("golden")) return 5;
        if (path.contains("leather")) return 3;
        if (path.contains("turtle")) return 9;

        return 5;
    }

    private static int getFoodScore(ItemStack stack) {
        if (stack.getItem().getFoodComponent() == null) return -1;
        
        int baseScore = 50;
        baseScore += Math.min(stack.getCount() * 2, 20);
        
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        String path = new Identifier(itemId).getPath();
        
        if (path.contains("golden_apple") || path.contains("enchanted_golden_apple")) {
            baseScore += 100;
        }
        if (path.contains("suspicious_stew")) {
            baseScore += 30;
        }
        
        return baseScore;
    }

    private static int getBlockUtilityScore(Item item) {
        String itemId = Registries.ITEM.getId(item).toString();
        String path = new Identifier(itemId).getPath();

        // Farm / grow-style blocks — low priority as "building material" vs wood/stone
        if (path.equals("sugar_cane") || path.equals("bamboo") || path.equals("cactus")
                || path.equals("kelp") || path.equals("vine") || path.equals("cocoa_beans")) {
            return 12;
        }

        if (path.endsWith("_log") || path.endsWith("_stem")
                || path.endsWith("_wood") || path.endsWith("_hyphae")) return 58;
        if (path.endsWith("_planks")) return 54;
        if (path.endsWith("_stairs") || path.endsWith("_slab")) return 52;
        if (path.endsWith("_door") || path.endsWith("_trapdoor")) return 50;
        if (path.endsWith("_fence") || path.endsWith("_wall")) return 48;

        if (path.contains("dirt") || path.contains("grass")) return 30;
        if (path.contains("cobblestone") || path.contains("stone_bricks")) return 40;
        if (path.contains("obsidian")) return 80;
        if (path.contains("terracotta") || path.contains("concrete")) return 55;
        if (path.contains("glass")) return 50;

        return 35;
    }

    private static int applyDurabilityModifier(ItemStack stack, int baseScore) {
        if (!stack.isDamageable()) return baseScore;

        float durabilityPercent = 1.0f - (float)stack.getDamage() / (float)stack.getMaxDamage();
        
        if (durabilityPercent > 0.9) {
            return baseScore;
        } else if (durabilityPercent > 0.5) {
            return (int)(baseScore * 0.95);
        } else if (durabilityPercent > 0.25) {
            return (int)(baseScore * 0.85);
        } else if (durabilityPercent > 0.1) {
            return (int)(baseScore * 0.7);
        } else {
            return (int)(baseScore * 0.4);
        }
    }

    private static int applyEnchantmentBonuses(ItemStack stack, String type, int baseScore) {
        Map<Enchantment, Integer> relevantEnchants = getRelevantEnchantments(type);
        int bonus = 0;

        for (Map.Entry<Enchantment, Integer> entry : relevantEnchants.entrySet()) {
            int level = EnchantmentHelper.getLevel(entry.getKey(), stack);
            if (level > 0) {
                int weight = entry.getValue();
                int multiplier = ENCHANT_LEVEL_MULTIPLIER.getOrDefault(level, 1);
                bonus += weight * level * multiplier;
            }
        }

        if (EnchantmentHelper.hasVanishingCurse(stack)) {
            bonus -= 50;
        }
        if (EnchantmentHelper.hasBindingCurse(stack)) {
            bonus -= 30;
        }

        return baseScore + bonus;
    }

    private static Map<Enchantment, Integer> getRelevantEnchantments(String type) {
        String upperType = type.toUpperCase();
        switch (upperType) {
            case "SWORD":
            case "AXE_COMBAT":
            case "TRIDENT":
            case "MACE":
                return new HashMap<>(COMBAT_ENCHANT_WEIGHTS);
            case "PICKAXE":
            case "SHOVEL":
            case "AXE_TOOL":
            case "HOE":
                return new HashMap<>(MINING_ENCHANT_WEIGHTS);
            case "ARMOR":
            case "HELMET":
            case "CHESTPLATE":
            case "LEGGINGS":
            case "BOOTS":
                Map<Enchantment, Integer> armorEnchants = new HashMap<>(UTILITY_ENCHANT_WEIGHTS);
                armorEnchants.put(Enchantments.PROTECTION, 12);
                armorEnchants.put(Enchantments.FIRE_PROTECTION, 10);
                armorEnchants.put(Enchantments.BLAST_PROTECTION, 11);
                armorEnchants.put(Enchantments.PROJECTILE_PROTECTION, 10);
                armorEnchants.put(Enchantments.THORNS, 8);
                armorEnchants.put(Enchantments.FEATHER_FALLING, 9);
                armorEnchants.put(Enchantments.DEPTH_STRIDER, 10);
                armorEnchants.put(Enchantments.AQUA_AFFINITY, 10);
                return armorEnchants;
            case "BOW":
                Map<Enchantment, Integer> bowEnchants = new HashMap<>(UTILITY_ENCHANT_WEIGHTS);
                bowEnchants.put(Enchantments.POWER, 20);
                bowEnchants.put(Enchantments.PUNCH, 15);
                bowEnchants.put(Enchantments.FLAME, 12);
                bowEnchants.put(Enchantments.INFINITY, 50);
                bowEnchants.put(Enchantments.MENDING, 25);
                bowEnchants.put(Enchantments.UNBREAKING, 10);
                return bowEnchants;
            case "CROSSBOW":
                Map<Enchantment, Integer> crossbowEnchants = new HashMap<>(UTILITY_ENCHANT_WEIGHTS);
                crossbowEnchants.put(Enchantments.QUICK_CHARGE, 25);
                crossbowEnchants.put(Enchantments.MULTISHOT, 30);
                crossbowEnchants.put(Enchantments.PIERCING, 18);
                crossbowEnchants.put(Enchantments.MENDING, 25);
                crossbowEnchants.put(Enchantments.UNBREAKING, 10);
                return crossbowEnchants;
            default:
                return UTILITY_ENCHANT_WEIGHTS;
        }
    }

    private static int applyCountModifier(ItemStack stack, String type, int baseScore) {
        String upperType = type.toUpperCase();

        if (upperType.equals("FOOD") || upperType.equals("POTION") || 
            upperType.equals("ARROW") || upperType.equals("REDSTONE") ||
            upperType.equals("TORCH")) {
            int countBonus = Math.min(stack.getCount(), 64) * 2;
            return Math.min(baseScore + countBonus, baseScore * 2);
        }

        if (upperType.equals("BLOCK") || upperType.equals("STAIRS") || 
            upperType.equals("SLAB") || upperType.equals("WALL")) {
            if (stack.getCount() >= 64) {
                return baseScore + 30;
            } else if (stack.getCount() >= 32) {
                return baseScore + 15;
            } else if (stack.getCount() >= 16) {
                return baseScore + 5;
            }
        }

        return baseScore;
    }

    private static int applyNBTModifier(ItemStack stack, int baseScore) {
        if (!stack.hasNbt()) return baseScore;
        if (stack.hasCustomName()) {
            baseScore += 10;
        }
        return baseScore;
    }

    private static int applyTierScaling(Item item, int baseScore) {
        int tierBonus = getTierBonus(item);
        return baseScore + tierBonus;
    }

    /**
     * Strict, type-aware fallback. Only returns a positive score when the
     * item path actually relates to the requested type — never a blanket
     * positive for unknown types (that was a bug that let, say, a cobblestone
     * pretend to be a "DRAGON_BREATH" because the old fallback returned 30
     * unconditionally).
     */
    private static int getFallbackScore(Item item, String type) {
        if (type == null || type.isEmpty()) return -1;

        String itemId = Registries.ITEM.getId(item).toString();
        String path = new Identifier(itemId).getPath();
        String typeKey = type.toLowerCase();

        // 1. Direct substring match: type appears in the item path
        //    (handles e.g. type "OAK_LOG" against item "oak_log").
        if (path.equals(typeKey)) return 100;
        if (path.contains(typeKey)) return 75;

        // 2. Type appears in the item path with separators stripped
        String pathStripped = path.replace("_", "");
        String typeStripped = typeKey.replace("_", "");
        if (pathStripped.contains(typeStripped)) return 60;

        // 3. Plural / singular forgiveness ("STAIRS" vs "stair")
        if (typeKey.endsWith("s") && pathStripped.contains(typeStripped.substring(0, typeStripped.length() - 1))) {
            return 55;
        }

        // No relationship — explicitly NOT a match. This is intentional;
        // returning a positive score for unrelated items was the bug.
        return -1;
    }
}
