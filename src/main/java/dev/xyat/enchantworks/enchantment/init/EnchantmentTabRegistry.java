package dev.xyat.enchantworks.enchantment.init;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantWorks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantmentTabRegistry {

    @SubscribeEvent
    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        // 添加到到栏目 INGREDIENTS (原材料)
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS && AnvilEnchantmentConfig.enableBetterChanneling && !AnvilEnchantmentConfig.isEnchantmentDisabled(Enchantments.CHANNELING)) {
            // 创建引雷 II (Channeling 2) 的附魔书
            ItemStack channelingBook = EnchantedBookItem.createForEnchantment(
                    new EnchantmentInstance(Enchantments.CHANNELING, 2)
            );
            event.accept(channelingBook);
        }
    }
}
