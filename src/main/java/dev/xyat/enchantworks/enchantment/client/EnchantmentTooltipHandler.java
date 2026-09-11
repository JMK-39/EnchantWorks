package dev.xyat.enchantworks.enchantment.client;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = EnchantWorks.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EnchantmentTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        List<Component> tooltip = event.getToolTip();
        Item item = stack.getItem();

        // --- 引雷 II 处理 ---
        int channelingLevel = enchantments.getOrDefault(Enchantments.CHANNELING, 0);
        if (!AnvilEnchantmentConfig.isEnchantmentDisabled(Enchantments.CHANNELING) && channelingLevel >= 2) {
            tooltip.add(Component.translatable("enchantment.enchantworks.channeling2.desc")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }

        // --- 无限 处理 ---
        int infinityLevel = enchantments.getOrDefault(Enchantments.INFINITY_ARROWS, 0);
        if (!AnvilEnchantmentConfig.isEnchantmentDisabled(Enchantments.INFINITY_ARROWS) && infinityLevel >= 1) {
            if (item instanceof BowItem || item == Items.ENCHANTED_BOOK) {
                addEnhancedTooltip(tooltip, "infinity_bow");
            }
            if (item instanceof BucketItem || item == Items.ENCHANTED_BOOK) {
                addEnhancedTooltip(tooltip, "infinity_bucket");
            }
        }
    }

    private static void addEnhancedTooltip(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable("enchantment.enchantworks." + key + ".title")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("enchantment.enchantworks." + key + ".desc")
                        .withStyle(ChatFormatting.AQUA)));
    }
}
