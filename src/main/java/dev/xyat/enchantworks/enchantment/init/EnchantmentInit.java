package dev.xyat.enchantworks.enchantment.init;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.enlightenment.EnlightenmentEnchantment;
import dev.xyat.enchantworks.enchantment.leech.LeechEnchantment;
import dev.xyat.enchantworks.enchantment.omni_tool.OmniToolEnchantment;
import dev.xyat.enchantworks.enchantment.sixth_sense.SixthSenseEnchantment;
import dev.xyat.enchantworks.enchantment.smelter.SmelterEnchantment;
import dev.xyat.kineticcore.api.registry.KineticEnchantments;
import dev.xyat.kineticcore.api.registry.KineticRegistryHandle;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

public class EnchantmentInit {
    @Nullable public static KineticRegistryHandle<Enchantment> SMELTER;
    @Nullable public static KineticRegistryHandle<Enchantment> LEECH;
    @Nullable public static KineticRegistryHandle<Enchantment> SIXTH_SENSE;
    @Nullable public static KineticRegistryHandle<Enchantment> ENLIGHTENMENT;
    @Nullable public static KineticRegistryHandle<Enchantment> OMNI_TOOL;

    private static boolean initialized;

    public static synchronized void register() {
        if (initialized) return;

        Set<String> disabled = AnvilEnchantmentConfig.getRegistryDisabledModEnchantments();
        SMELTER = registerModEnchantment(disabled, "smelter",
                () -> new SmelterEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.DIGGER, EquipmentSlot.MAINHAND));
        LEECH = registerModEnchantment(disabled, "leech",
                () -> new LeechEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND));
        SIXTH_SENSE = registerModEnchantment(disabled, "sixth_sense",
                () -> new SixthSenseEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, EquipmentSlot.HEAD));
        ENLIGHTENMENT = registerModEnchantment(disabled, "enlightenment",
                () -> new EnlightenmentEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR, EquipmentSlot.values()));
        OMNI_TOOL = registerModEnchantment(disabled, "omni_tool",
                () -> new OmniToolEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.DIGGER, EquipmentSlot.MAINHAND));

        initialized = true;
    }

    @Nullable
    private static KineticRegistryHandle<Enchantment> registerModEnchantment(
            Set<String> disabled,
            String path,
            Supplier<Enchantment> supplier
    ) {
        String id = EnchantWorks.MODID + ":" + path;
        if (disabled.contains(id)) return null;
        return KineticEnchantments.register(KineticResourceIds.of(EnchantWorks.MODID, path), supplier);
    }

    public static boolean isRegistered(@Nullable KineticRegistryHandle<Enchantment> object) {
        return object != null && object.isPresent();
    }

    public static boolean isEnabled(@Nullable KineticRegistryHandle<Enchantment> object) {
        Enchantment enchantment = get(object);
        return enchantment != null && !AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment);
    }

    @Nullable
    public static Enchantment get(@Nullable KineticRegistryHandle<Enchantment> object) {
        return isRegistered(object) ? object.get() : null;
    }

    public static int getLevel(@Nullable KineticRegistryHandle<Enchantment> object, ItemStack stack) {
        Enchantment enchantment = get(object);
        if (enchantment == null || stack.isEmpty() || AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment)) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    public static int getEntityLevel(@Nullable KineticRegistryHandle<Enchantment> object, LivingEntity entity) {
        Enchantment enchantment = get(object);
        if (enchantment == null || entity == null || AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment)) {
            return 0;
        }
        return EnchantmentHelper.getEnchantmentLevel(enchantment, entity);
    }

    public static boolean isSame(Enchantment enchantment, @Nullable KineticRegistryHandle<Enchantment> object) {
        Enchantment registered = get(object);
        return registered != null && enchantment == registered;
    }
}
