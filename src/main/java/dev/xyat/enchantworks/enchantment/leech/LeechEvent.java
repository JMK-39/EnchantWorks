package dev.xyat.enchantworks.enchantment.leech;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = EnchantWorks.MODID)
public class LeechEvent {
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.LEECH)) return;

        Entity source = event.getSource().getEntity();
        if (!(source instanceof LivingEntity attacker)) return;
        if (event.getSource().isIndirect()) return;

        LivingEntity target = event.getEntity();
        if (target == attacker || !target.isAlive() || event.getAmount() <= 0.0F) return;

        int level = EnchantmentInit.getEntityLevel(EnchantmentInit.LEECH, attacker);
        if (level <= 0 || attacker.getRandom().nextDouble() >= AnvilEnchantmentConfig.leechTriggerChance) return;

        float healAmount = event.getAmount() * (float) AnvilEnchantmentConfig.leechLifestealRatio * level;
        if (healAmount > 0.0F) {
            attacker.heal(healAmount);
        }

        if (attacker.getRandom().nextDouble() < AnvilEnchantmentConfig.leechStealChance) {
            stealEffect(attacker, target);
        }
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
