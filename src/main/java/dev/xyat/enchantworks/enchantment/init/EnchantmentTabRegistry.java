package dev.xyat.enchantworks.enchantment.init;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.kineticcore.api.runtime.KineticCreativeTabs;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public final class EnchantmentTabRegistry {
    private static boolean initialized;

    private EnchantmentTabRegistry() {
    }

    public static synchronized void register() {
        if (initialized) return;
        KineticCreativeTabs.onBuildContents(context -> {
            if (context.tabKey() == CreativeModeTabs.INGREDIENTS
                    && AnvilEnchantmentConfig.enableBetterChanneling
                    && !AnvilEnchantmentConfig.isEnchantmentDisabled(Enchantments.CHANNELING)) {
                ItemStack channelingBook = EnchantmentInit.book(Enchantments.CHANNELING, 2);
                if (!channelingBook.isEmpty()) context.accept(channelingBook);
            }
        });
        initialized = true;
    }
}
