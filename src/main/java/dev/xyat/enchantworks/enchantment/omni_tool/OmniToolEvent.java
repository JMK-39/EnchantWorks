package dev.xyat.enchantworks.enchantment.omni_tool;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantWorks.MODID)
public class OmniToolEvent {
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return;

        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, stack) <= 0) return;

        BlockState state = event.getState();
        BlockPos pos = event.getPosition().orElse(player.blockPosition());
        float hardness = state.getDestroySpeed(player.level(), pos);
        if (hardness < 0.0F) return;

        float currentSpeed = event.getNewSpeed();
        float multiplier = Math.max(1.0F, (float) AnvilEnchantmentConfig.omniToolBaseSpeedMultiplier);
        float baseSpeedCompensation = currentSpeed * multiplier;
        float hardnessCompensation = hardness * 4.0F;

        event.setNewSpeed(Math.max(currentSpeed, Math.max(baseSpeedCompensation, hardnessCompensation)));
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return;
        Player player = event.getPlayer();
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, stack) <= 0) return;
        if (AnvilEnchantmentConfig.shouldOmniToolForceDrop(event.getState())) {
            event.setExpToDrop(0);
        }
    }
}

