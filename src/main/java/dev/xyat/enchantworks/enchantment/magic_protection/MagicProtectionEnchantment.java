package dev.xyat.enchantworks.enchantment.magic_protection;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import org.jetbrains.annotations.NotNull;

public class MagicProtectionEnchantment extends Enchantment {
    public MagicProtectionEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 8;
    }

    @Override
    public int getDamageProtection(int level, DamageSource source) {
        if (AnvilEnchantmentConfig.isEnchantmentDisabled(this)) {
            return 0;
        }
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) {
            return level * 2;
        }
        return 0;
    }

    @Override
    public boolean checkCompatibility(@NotNull Enchantment other) {
        return !(other instanceof ProtectionEnchantment) && super.checkCompatibility(other);
    }
}
