package dev.xyat.enchantworks.enchantment.enlightenment;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.kineticcore.api.entity.event.KineticLivingEvents;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import dev.xyat.kineticcore.api.world.event.KineticWorldEvents;
import net.minecraft.world.entity.player.Player;

public final class EnlightenmentEvent {
    private static boolean initialized;

    private EnlightenmentEvent() {
    }

    public static synchronized void register() {
        if (initialized) return;
        KineticLivingEvents.onExperienceDrop(KineticEventPriority.LOWEST, context -> {
            Player player = context.attackingPlayer();
            if (player == null || player.level().isClientSide) return;

            int experience = applyBonus(player, context.droppedExperience());
            if (experience != context.droppedExperience()) {
                context.droppedExperience(experience);
            }
        });
        KineticWorldEvents.onBlockBreak(KineticEventPriority.LOWEST, context -> {
            Player player = context.player();
            if (player.level().isClientSide) return;

            int experience = applyBonus(player, context.experienceToDrop());
            if (experience != context.experienceToDrop()) {
                context.experienceToDrop(experience);
            }
        });
        initialized = true;
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
