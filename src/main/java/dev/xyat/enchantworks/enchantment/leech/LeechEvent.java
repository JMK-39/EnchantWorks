package dev.xyat.enchantworks.enchantment.leech;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.kineticcore.api.entity.event.KineticLivingEvents;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public final class LeechEvent {
    private static boolean initialized;

    private LeechEvent() {
    }

    public static synchronized void register() {
        if (initialized) return;
        KineticLivingEvents.onDamage(KineticEventPriority.NORMAL, context -> {
            if (!EnchantmentInit.isEnabled(EnchantmentInit.LEECH)) return;

            Entity source = context.source().getEntity();
            if (!(source instanceof LivingEntity attacker)) return;
            if (context.source().isIndirect()) return;

            LivingEntity target = context.entity();
            if (target == attacker || !target.isAlive() || context.amount() <= 0.0F) return;

            int level = EnchantmentInit.getEntityLevel(EnchantmentInit.LEECH, attacker);
            if (level <= 0 || attacker.getRandom().nextDouble() >= AnvilEnchantmentConfig.leechTriggerChance) return;

            float healAmount = context.amount() * (float) AnvilEnchantmentConfig.leechLifestealRatio * level;
            if (healAmount > 0.0F) {
                attacker.heal(healAmount);
            }

            if (attacker.getRandom().nextDouble() < AnvilEnchantmentConfig.leechStealChance) {
                stealEffect(attacker, target);
            }
        });
        initialized = true;
    }

    private static void stealEffect(LivingEntity attacker, LivingEntity target) {
        List<MobEffectInstance> candidates = new ArrayList<>();
        for (MobEffectInstance effect : target.getActiveEffects()) {
            if (AnvilEnchantmentConfig.canLeechEffect(effect.getEffect())) {
                candidates.add(effect);
            }
        }

        if (candidates.isEmpty()) return;

        MobEffectInstance selected = candidates.get(attacker.getRandom().nextInt(candidates.size()));
        MobEffectInstance copied = new MobEffectInstance(selected);
        if (target.removeEffect(selected.getEffect())) {
            attacker.addEffect(copied);
        }
    }
}
