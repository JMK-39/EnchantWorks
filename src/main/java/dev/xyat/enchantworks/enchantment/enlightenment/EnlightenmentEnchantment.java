package dev.xyat.enchantworks.enchantment.enlightenment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class EnlightenmentEnchantment extends Enchantment {
    public EnlightenmentEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinCost(int level) {
        return level * 12 + 6;
    }

    @Override
    public int getMaxCost(int level) {
        return level * 12 + 26;
    }
}
