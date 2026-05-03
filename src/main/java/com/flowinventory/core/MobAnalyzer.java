package com.flowinventory.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.registry.Registries;

import java.util.*;

/**
 * Advanced mob analysis for activity detection.
 */
public class MobAnalyzer {
    
    public static String analyzeCombatScenario(List<? extends LivingEntity> mobs) {
        if (mobs.isEmpty()) return "COMBAT";
        
        Map<String, Integer> mobCounts = new HashMap<>();
        boolean hasSkeletons = false;
        boolean hasCreepers = false;
        boolean hasSpiders = false;
        boolean hasBlaze = false;
        boolean hasGuardians = false;
        boolean hasWitches = false;
        boolean hasEnderman = false;
        
        for (LivingEntity mob : mobs) {
            if (mob.isDead()) continue;
            
            String mobType = Registries.ENTITY_TYPE.getId(mob.getType()).getPath();
            mobCounts.merge(mobType, 1, Integer::sum);
            
            if (mobType.contains("skeleton") && mob instanceof SkeletonEntity) {
                hasSkeletons = true;
            }
            if (mobType.contains("creeper")) {
                hasCreepers = true;
            }
            if (mobType.contains("spider")) {
                hasSpiders = true;
            }
            if (mobType.contains("blaze")) {
                hasBlaze = true;
            }
            if (mobType.contains("guardian")) {
                hasGuardians = true;
            }
            if (mobType.contains("witch")) {
                hasWitches = true;
            }
            if (mobType.contains("enderman")) {
                hasEnderman = true;
            }
        }
        
        if (mobCounts.size() >= 4) {
            return "BATTLE";
        }
        
        if (mobCounts.containsKey("pillager") && mobCounts.containsKey("vindicator")) {
            return "RAID";
        }
        
        if (hasSkeletons && mobCounts.size() <= 2) {
            return "SNIPER";
        }
        
        if (hasCreepers && mobCounts.size() == 1) {
            return "EXPLOSIVES";
        }
        
        if (hasSpiders && mobCounts.size() <= 2) {
            return "COMBAT";
        }
        
        if (hasBlaze) {
            return "BLAZE";
        }
        
        if (hasGuardians) {
            return "GUARDIAN";
        }
        
        if (hasWitches) {
            return "POTION_COMBAT";
        }
        
        if (hasEnderman && mobCounts.size() <= 3) {
            return "ENDERMAN";
        }
        
        return "COMBAT";
    }
    
    public static String getOptimalWeapon(List<? extends LivingEntity> mobs) {
        String scenario = analyzeCombatScenario(mobs);
        
        switch (scenario) {
            case "SNIPER": return "BOW";
            case "EXPLOSIVES": return "TNT";
            case "BLAZE": return "BOW";
            case "GUARDIAN": return "TRIDENT";
            case "POTION_COMBAT": return "POTION";
            case "RAID": return "CROSSBOW";
            case "BATTLE": return "SWORD";
            case "ENDERMAN": return "SWORD";
            default: return "SWORD";
        }
    }
    
    public static List<String> getRecommendedPotions(List<? extends LivingEntity> mobs) {
        List<String> potions = new ArrayList<>();
        
        for (LivingEntity mob : mobs) {
            if (mob.isDead()) continue;
            
            String mobType = Registries.ENTITY_TYPE.getId(mob.getType()).getPath();
            
            if (mobType.contains("skeleton") || mobType.contains("stray") || 
                mobType.contains("pillager")) {
                potions.add("REGENERATION");
            }
            if (mobType.contains("blaze")) {
                potions.add("FIRE_RESISTANCE");
            }
            if (mobType.contains("witch")) {
                potions.add("STRENGTH");
            }
        }
        
        if (mobs.size() >= 5) {
            potions.add("REGENERATION");
            potions.add("ABSORPTION");
        }
        
        return potions;
    }
    
    public static int calculateThreatLevel(List<? extends LivingEntity> mobs) {
        if (mobs.isEmpty()) return 0;
        
        int threat = 0;
        for (LivingEntity mob : mobs) {
            if (mob.isDead()) continue;
            
            String mobType = Registries.ENTITY_TYPE.getId(mob.getType()).getPath();
            int mobThreat = 10;
            
            if (mobType.contains("wither")) mobThreat = 100;
            else if (mobType.contains("ender_dragon")) mobThreat = 100;
            else if (mobType.contains("warden")) mobThreat = 90;
            else if (mobType.contains("ravager")) mobThreat = 70;
            else if (mobType.contains("blaze")) mobThreat = 50;
            else if (mobType.contains("creeper")) mobThreat = 45;
            else if (mobType.contains("witch")) mobThreat = 40;
            else if (mobType.contains("skeleton")) mobThreat = 35;
            else if (mobType.contains("spider")) mobThreat = 30;
            else if (mobType.contains("zombie")) mobThreat = 25;
            else mobThreat = 20;
            
            threat += mobThreat;
        }
        
        return Math.min(100, threat);
    }
}
