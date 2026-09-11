package dev.xyat.enchantworks.anvil.util;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import net.minecraft.world.entity.player.Player;

public class TaxFreeLevelsLogic {
    private static final ThreadLocal<Integer> LEVEL_REQUIREMENT = ThreadLocal.withInitial(() -> -1);

    public static void setLevelRequirement(int value) { LEVEL_REQUIREMENT.set(value); }
    public static int getLevelRequirement() { return LEVEL_REQUIREMENT.get(); }
    public static void resetLevelRequirement() { LEVEL_REQUIREMENT.remove(); }

    public static int getXpDifference(Player player, int from, int to) {
        int currentLevel = player.experienceLevel;
        int xpSum = 0;
        for (int l = from; l < to; l++) {
            player.experienceLevel = l;
            xpSum += player.getXpNeededForNextLevel();
        }
        player.experienceLevel = currentLevel;
        return xpSum;
    }

    public static void addNoScoreExperience(Player player, int xp) {
        // 更新经验进度条（xp为负数，减去对应比例）
        player.experienceProgress += (float) xp / (float) player.getXpNeededForNextLevel();
        // 传入0，触发原版的降级计算及网络发包同步
        player.giveExperiencePoints(0);
        resetLevelRequirement();
    }

    public static int getFlattenedXpCost(Player player, int levelCost) {
        int base;
        if (getLevelRequirement() >= 0) {
            base = Math.max(getLevelRequirement(), AnvilEnchantmentConfig.taxFreeLevelBase);
        } else {
            base = AnvilEnchantmentConfig.taxFreeLevelBase;
        }

        int pretendLevel = Math.min(player.experienceLevel, base);
        int from = Math.max(pretendLevel - levelCost, 0);
        return getXpDifference(player, from, from + levelCost);
    }

    public static void applyFlattenedXpCost(Player player, int levelCost) {
        if (levelCost <= 0) return;
        addNoScoreExperience(player, -getFlattenedXpCost(player, levelCost));
    }
}
