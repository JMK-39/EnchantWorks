package dev.xyat.enchantworks.enchantment.client;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.Map;

public final class EnchantmentTooltipHandler {
    private static boolean initialized;

    private EnchantmentTooltipHandler() {
    }

    public static synchronized void register() {
        if (initialized) return;
        KineticItemTooltips.onBuild(EnchantmentTooltipHandler::buildTooltip);
        initialized = true;
    }

    private static void buildTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack.isEmpty()) return;

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        Item item = stack.getItem();

        int channelingLevel = enchantments.getOrDefault(Enchantments.CHANNELING, 0);
        if (!AnvilEnchantmentConfig.isEnchantmentDisabled(Enchantments.CHANNELING) && channelingLevel >= 2) {
            tooltip.add(KineticI18n.translatable("enchantment.enchantworks.channeling2.desc"));
        }

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
        tooltip.add(KineticI18n.translatable(
                "enchantment.enchantworks.enhanced.tooltip",
                KineticI18n.translatable("enchantment.enchantworks." + key + ".title"),
                KineticI18n.translatable("enchantment.enchantworks." + key + ".desc")
        ));
    }
}
