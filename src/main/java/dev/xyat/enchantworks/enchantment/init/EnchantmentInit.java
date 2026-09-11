package dev.xyat.enchantworks.enchantment.init;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.enlightenment.EnlightenmentEnchantment;
import dev.xyat.enchantworks.enchantment.leech.LeechEnchantment;
import dev.xyat.enchantworks.enchantment.omni_tool.OmniToolEnchantment;
import dev.xyat.enchantworks.enchantment.sixth_sense.SixthSenseEnchantment;
import dev.xyat.enchantworks.enchantment.smelter.SmelterEnchantment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

public class EnchantmentInit {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EnchantWorks.MODID);

    public static final RegistryObject<Enchantment> SMELTER = holder("smelter");
    public static final RegistryObject<Enchantment> LEECH = holder("leech");
    public static final RegistryObject<Enchantment> SIXTH_SENSE = holder("sixth_sense");
    public static final RegistryObject<Enchantment> ENLIGHTENMENT = holder("enlightenment");
    public static final RegistryObject<Enchantment> OMNI_TOOL = holder("omni_tool");

    private static boolean initialized;

    public static synchronized void register(IEventBus bus) {
        if (initialized) return;

        Set<String> disabled = AnvilEnchantmentConfig.getRegistryDisabledModEnchantments();
        registerModEnchantment(disabled, "smelter",
                () -> new SmelterEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.DIGGER, EquipmentSlot.MAINHAND));
        registerModEnchantment(disabled, "leech",
                () -> new LeechEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND));
        registerModEnchantment(disabled, "sixth_sense",
                () -> new SixthSenseEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, EquipmentSlot.HEAD));
        registerModEnchantment(disabled, "enlightenment",
                () -> new EnlightenmentEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR, EquipmentSlot.values()));
        registerModEnchantment(disabled, "omni_tool",
                () -> new OmniToolEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.DIGGER, EquipmentSlot.MAINHAND));

        ENCHANTMENTS.register(bus);
        initialized = true;
    }

    private static RegistryObject<Enchantment> holder(String path) {
        return RegistryObject.create(new ResourceLocation(EnchantWorks.MODID, path), ForgeRegistries.ENCHANTMENTS);
    }

    private static void registerModEnchantment(
            Set<String> disabled,
            String path,
            Supplier<Enchantment> supplier
    ) {
        if (!disabled.contains(EnchantWorks.MODID + ":" + path)) {
            ENCHANTMENTS.register(path, supplier);
        }
    }

    public static boolean isRegistered(@Nullable RegistryObject<Enchantment> object) {
        return object != null && object.isPresent();
    }

    public static boolean isEnabled(@Nullable RegistryObject<Enchantment> object) {
        Enchantment enchantment = get(object);
        return enchantment != null && !AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment);
    }

    @Nullable
    public static Enchantment get(@Nullable RegistryObject<Enchantment> object) {
        return isRegistered(object) ? object.get() : null;
    }

    public static int getLevel(@Nullable RegistryObject<Enchantment> object, ItemStack stack) {
        Enchantment enchantment = get(object);
        if (enchantment == null || stack.isEmpty() || AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment)) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    public static int getEntityLevel(@Nullable RegistryObject<Enchantment> object, LivingEntity entity) {
        Enchantment enchantment = get(object);
        if (enchantment == null || entity == null || AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment)) {
            return 0;
        }
        return EnchantmentHelper.getEnchantmentLevel(enchantment, entity);
    }

    public static boolean isSame(Enchantment enchantment, @Nullable RegistryObject<Enchantment> object) {
        Enchantment registered = get(object);
        return registered != null && enchantment == registered;
    }
}
