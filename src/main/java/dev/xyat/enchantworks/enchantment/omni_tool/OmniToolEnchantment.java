package dev.xyat.enchantworks.enchantment.omni_tool;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.NotNull;

public class OmniToolEnchantment extends Enchantment {
    public OmniToolEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack stack) {
        return isToolCompatible(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
        return isToolCompatible(stack);
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return 75;
    }

    private boolean isToolCompatible(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.BOOK || item == Items.ENCHANTED_BOOK) {
            return true;
        }
        if (item instanceof SwordItem) {
            return false;
        }
        if (item instanceof DiggerItem || item instanceof ShearsItem) {
            return true;
        }
        return this.category.canEnchant(item);
    }
}
