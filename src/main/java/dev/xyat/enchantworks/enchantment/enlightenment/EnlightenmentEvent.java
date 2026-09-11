package dev.xyat.enchantworks.enchantment.enlightenment;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantWorks.MODID)
public class EnlightenmentEvent {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null || player.level().isClientSide) return;

        int experience = applyBonus(player, event.getDroppedExperience());
        if (experience != event.getDroppedExperience()) {
            event.setDroppedExperience(experience);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) return;

        int experience = applyBonus(player, event.getExpToDrop());
        if (experience != event.getExpToDrop()) {
            event.setExpToDrop(experience);
        }
    }

    private static int applyBonus(Player player, int baseExperience) {
        if (!AnvilEnchantmentConfig.enableEnlightenment || baseExperience <= 0) return baseExperience;

        int level = EnchantmentInit.getEntityLevel(EnchantmentInit.ENLIGHTENMENT, player);
        if (level <= 0) return baseExperience;

        long bonus = Math.round(baseExperience * level * AnvilEnchantmentConfig.enlightenmentExpMult);
        if (bonus <= 0L) return baseExperience;

        return (int) Math.min(Integer.MAX_VALUE, (long) baseExperience + bonus);
    }
}
