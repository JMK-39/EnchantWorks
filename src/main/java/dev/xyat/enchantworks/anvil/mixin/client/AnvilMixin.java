package dev.xyat.enchantworks.anvil.mixin.client;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

public class AnvilMixin {

    @Mixin(value = AnvilScreen.class, priority = 2000)
    public abstract static class AnvilScreenFix extends ItemCombinerScreen<AnvilMenu> {
        public AnvilScreenFix(AnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle, ResourceLocation pMenuResource) {
            super(pMenu, pPlayerInventory, pTitle, pMenuResource);
        }

        @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 40), require = 0)
        private int enchantworks_enchant$removeGuiLevelCap(int old) {
            ItemStack stack = this.menu.getSlot(0).getItem();
            return AnvilEnchantmentConfig.shouldRemoveAnvilLimit(stack) ? Integer.MAX_VALUE : old;
        }
    }
}
