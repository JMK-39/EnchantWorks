package dev.xyat.enchantworks.enchantment.omni_tool;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import dev.xyat.kineticcore.api.player.event.KineticPlayerEvents;
import dev.xyat.kineticcore.api.world.event.KineticWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class OmniToolEvent {
    private static boolean initialized;

    private OmniToolEvent() {
    }

    public static synchronized void register() {
        if (initialized) return;
        KineticPlayerEvents.onBreakSpeed(KineticEventPriority.NORMAL, context -> {
            if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return;

            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, stack) <= 0) return;

            BlockState state = context.state();
            BlockPos pos = context.pos();
            float hardness = state.getDestroySpeed(player.level(), pos);
            if (hardness < 0.0F) return;

            float currentSpeed = context.speed();
            float multiplier = Math.max(1.0F, (float) AnvilEnchantmentConfig.omniToolBaseSpeedMultiplier);
            float baseSpeedCompensation = currentSpeed * multiplier;
            float hardnessCompensation = hardness * 4.0F;

            context.speed(Math.max(currentSpeed, Math.max(baseSpeedCompensation, hardnessCompensation)));
        });
        KineticWorldEvents.onBlockBreak(KineticEventPriority.NORMAL, context -> {
            if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return;
            Player player = context.player();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, stack) <= 0) return;
            if (AnvilEnchantmentConfig.shouldOmniToolForceDrop(context.state())) {
                context.experienceToDrop(0);
            }
        });
        initialized = true;
    }
}
