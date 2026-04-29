package com.flowinventory.server;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class ItemHeuristics {

    public static int evaluate(ItemStack stack, String type) {
        if (stack.isEmpty()) return -1;
        Item item = stack.getItem();
        String id = Registries.ITEM.getId(item).toString();

        switch (type) {
            case "SWORD":
                return getCombatScore(stack);
            case "PICKAXE":
                return getMiningToolScore(stack, PickaxeItem.class);
            case "AXE":
                return getMiningToolScore(stack, AxeItem.class);
            case "SHOVEL":
                return getMiningToolScore(stack, ShovelItem.class);
            case "HOE":
                return getMiningToolScore(stack, HoeItem.class);
            case "BOW":
                return getRangedScore(stack);
            case "SHIELD":
                return getShieldScore(stack);
            case "FOOD":
                return getFoodScore(stack);
            case "BLOCK":
                return getBlockScore(stack);
            case "TORCH":
                return getLightScore(stack);
            case "SEEDS":
                return getFarmingScore(stack);
            case "WATER_BUCKET":
                return getUtilityBucketScore(stack);
            case "ENDER_PEARL":
                return getTeleportScore(stack);
            case "POTION":
                return getPotionScore(stack);
            case "FISHING_ROD":
                return getFishingScore(stack);
            case "EXPLORATION":
                return getExplorationScore(stack);
            case "REDSTONE":
                return getRedstoneScore(stack);
            case "BREWING":
                return getBrewingScore(stack);
            case "RIDING":
                return getRidingScore(stack);
        }
        return -1;
    }

    private static int getCombatScore(ItemStack stack) {
        Item item = stack.getItem();
        int score = 0;

        if (item instanceof SwordItem sword) {
            score = 100 + (sword.getMaterial().getMiningLevel() * 20);
            score += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) * 15;
            score += EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack) * 10;
            score += EnchantmentHelper.getLevel(Enchantments.LOOTING, stack) * 5;
            score += EnchantmentHelper.getLevel(Enchantments.KNOCKBACK, stack) * 5;
        } else if (item instanceof AxeItem axe) {
            score = 60 + (axe.getMaterial().getMiningLevel() * 15);
            score += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) * 10;
        } else if (item instanceof TridentItem) {
            score = 80;
            score += EnchantmentHelper.getLevel(Enchantments.IMPALING, stack) * 10;
        }

        // Penalty for broken items
        if (stack.isDamageable() && stack.getDamage() > stack.getMaxDamage() * 0.9) {
            score -= 30;
        }

        return score > 0 ? score : (item instanceof SwordItem ? 1 : -1);
    }

    private static int getMiningToolScore(ItemStack stack, Class<? extends ToolItem> toolClass) {
        if (!toolClass.isInstance(stack.getItem())) return -1;
        ToolItem tool = (ToolItem) stack.getItem();
        
        int score = 100 + (tool.getMaterial().getMiningLevel() * 25);
        score += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack) * 20;
        score += EnchantmentHelper.getLevel(Enchantments.UNBREAKING, stack) * 5;
        
        if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0) score += 10;
        if (EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack) > 0) score += 15;

        return score;
    }

    private static int getRangedScore(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BowItem) {
            int score = 100;
            score += EnchantmentHelper.getLevel(Enchantments.POWER, stack) * 15;
            score += EnchantmentHelper.getLevel(Enchantments.PUNCH, stack) * 10;
            if (EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) score += 50;
            return score;
        }
        if (item instanceof CrossbowItem) {
            int score = 90;
            score += EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, stack) * 15;
            if (EnchantmentHelper.getLevel(Enchantments.MULTISHOT, stack) > 0) score += 20;
            return score;
        }
        return -1;
    }

    private static int getShieldScore(ItemStack stack) {
        if (stack.getItem() instanceof ShieldItem) {
            int score = 100;
            score += EnchantmentHelper.getLevel(Enchantments.UNBREAKING, stack) * 10;
            return score;
        }
        return -1;
    }

    private static int getFoodScore(ItemStack stack) {
        if (stack.getItem().getFoodComponent() != null) {
            return stack.getItem().getFoodComponent().getHunger() * 10 + 
                   (int)(stack.getItem().getFoodComponent().getSaturationModifier() * 10);
        }
        return -1;
    }

    private static int getBlockScore(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            int score = stack.getCount(); // Favor larger stacks
            if (stack.isIn(ItemTags.LOGS) || stack.isIn(ItemTags.PLANKS) || stack.isIn(ItemTags.STONE_BRICKS)) score += 50;
            String id = Registries.ITEM.getId(stack.getItem()).getPath();
            if (!id.contains("slab") && !id.contains("stairs") && !id.contains("wall")) score += 50;
            return score;
        }
        return -1;
    }

    private static int getLightScore(ItemStack stack) {
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        if (id.contains("torch")) return 100 + stack.getCount();
        if (id.contains("lantern")) return 90;
        if (id.contains("glowstone")) return 80;
        return -1;
    }

    private static int getFarmingScore(ItemStack stack) {
        Item item = stack.getItem();
        String id = Registries.ITEM.getId(item).toString();
        
        if (item instanceof AliasedBlockItem) return 100;
        if (stack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS) || id.contains("seeds")) return 90;
        if (id.contains("sugar_cane") || id.contains("bamboo") || id.contains("nether_wart")) return 80;
        if (stack.isIn(ItemTags.SAPLINGS) || stack.isIn(ItemTags.LEAVES) || id.contains("berry")) return 70;
        if (item instanceof BoneMealItem) return 50;
        
        return -1;
    }

    private static int getUtilityBucketScore(ItemStack stack) {
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        if (id.equals("minecraft:water_bucket")) return 100;
        if (id.equals("minecraft:lava_bucket")) return 80;
        if (id.equals("minecraft:bucket")) return 30;
        return -1;
    }

    private static int getTeleportScore(ItemStack stack) {
        if (stack.getItem() instanceof EnderPearlItem) return 100 + stack.getCount();
        if (Registries.ITEM.getId(stack.getItem()).getPath().contains("chorus_fruit")) return 50;
        return -1;
    }

    private static int getPotionScore(ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) return 100;
        if (stack.getItem() instanceof ExperienceBottleItem) return 50;
        return -1;
    }

    private static int getFishingScore(ItemStack stack) {
        if (stack.getItem() instanceof FishingRodItem) {
            int score = 100;
            score += EnchantmentHelper.getLevel(Enchantments.LUCK_OF_THE_SEA, stack) * 20;
            score += EnchantmentHelper.getLevel(Enchantments.LURE, stack) * 15;
            return score;
        }
        return -1;
    }

    private static int getExplorationScore(ItemStack stack) {
        String id = Registries.ITEM.getId(stack.getItem()).getPath();
        if (id.contains("compass") || id.contains("clock")) return 100;
        if (id.contains("map")) return 90;
        if (id.contains("spyglass")) return 80;
        return -1;
    }

    private static int getRedstoneScore(ItemStack stack) {
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        if (id.contains("redstone_dust")) return 100 + stack.getCount();
        if (id.contains("repeater") || id.contains("comparator")) return 95;
        if (id.contains("observer") || id.contains("piston")) return 90;
        if (id.contains("lever") || id.contains("button") || id.contains("pressure_plate")) return 80;
        return -1;
    }

    private static int getBrewingScore(ItemStack stack) {
        Item item = stack.getItem();
        String id = Registries.ITEM.getId(item).toString();
        if (id.contains("glass_bottle")) return 100;
        if (id.contains("blaze_powder") || id.contains("nether_wart")) return 90;
        if (id.contains("ghast_tear") || id.contains("magma_cream")) return 80;
        return -1;
    }

    private static int getRidingScore(ItemStack stack) {
        Item item = stack.getItem();
        String id = Registries.ITEM.getId(item).getPath();
        if (item instanceof SaddleItem || id.equals("elytra")) return 100;
        if (id.contains("horse_armor") || id.contains("firework_rocket")) return 90;
        if (id.equals("lead") || id.equals("carrot_on_a_stick") || id.equals("warped_fungus_on_a_stick")) return 80;
        return -1;
    }
}
