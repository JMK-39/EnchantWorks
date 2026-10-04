package dev.xyat.enchantworks.enchantment.init;

//? if >=1.21 {
/*import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.kineticcore.api.runtime.KineticServerRuntime;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/^*
 * The mod's enchantments. Since 1.21 they are data (data/enchantworks/enchantment/<id>.json) and live in the world's
 * registry, so they are named by key and read straight from an item's enchantments.
 ^/
public class EnchantmentInit {
    public static final ResourceKey<Enchantment> SMELTER = key("smelter");
    public static final ResourceKey<Enchantment> LEECH = key("leech");
    public static final ResourceKey<Enchantment> SIXTH_SENSE = key("sixth_sense");
    public static final ResourceKey<Enchantment> ENLIGHTENMENT = key("enlightenment");
    public static final ResourceKey<Enchantment> OMNI_TOOL = key("omni_tool");

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, KineticResourceIds.of(EnchantWorks.MODID, path));
    }

    /^* Nothing to register in code since 1.21. ^/
    public static void register() {
    }

    /^* Whether the current world has the enchantment (datapacks may remove it). ^/
    public static boolean isRegistered(ResourceKey<Enchantment> key) {
        return get(key) != null;
    }

    public static boolean isEnabled(ResourceKey<Enchantment> key) {
        Enchantment enchantment = get(key);
        return enchantment != null && !AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment);
    }

    @Nullable
    public static Enchantment get(ResourceKey<Enchantment> key) {
        return KineticRegistries.enchantments().get(key.location());
    }

    /^* The enchantment as a registry entry, needed to put it on an item. ^/
    public static Holder<Enchantment> holder(HolderLookup.Provider registries, ResourceKey<Enchantment> key) {
        // lookup(...).orElseThrow(), not lookupOrThrow: the 26.1 rename rule would turn the latter into registryOrThrow here.
        return registries.lookup(Registries.ENCHANTMENT).orElseThrow().getOrThrow(key);
    }

    public static int getLevel(ResourceKey<Enchantment> key, ItemStack stack) {
        if (stack.isEmpty() || !isEnabled(key)) return 0;
        return levelOn(stack, key);
    }

    /^* The highest level among the entity's items in the slots the enchantment works in. ^/
    public static int getEntityLevel(ResourceKey<Enchantment> key, LivingEntity entity) {
        if (entity == null || !isEnabled(key)) return 0;
        int best = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments(stack).entrySet()) {
                if (entry.getKey().is(key) && entry.getKey().value().matchingSlot(slot)) {
                    best = Math.max(best, entry.getIntValue());
                }
            }
        }
        return best;
    }

    public static boolean isSame(Enchantment enchantment, ResourceKey<Enchantment> key) {
        ResourceLocation id = KineticRegistries.enchantments().id(enchantment);
        return id != null && Objects.equals(id, key.location());
    }

    public static boolean isSame(Holder<Enchantment> enchantment, ResourceKey<Enchantment> key) {
        return enchantment.is(key);
    }

    /^* Level of an enchantment applied to the item (not stored in a book). ^/
    public static int levelOn(ItemStack stack, ResourceKey<Enchantment> key) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments(stack).entrySet()) {
            if (entry.getKey().is(key)) return entry.getIntValue();
        }
        return 0;
    }

    /^* Level counting a book's stored enchantments too, as the tooltips and anvil see them. ^/
    public static int levelIncludingStored(ItemStack stack, ResourceKey<Enchantment> key) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            if (entry.getKey().is(key)) return entry.getIntValue();
        }
        return 0;
    }

    /^* The registry entry in the running world, or null outside a world or when a datapack removed it. ^/
    @Nullable
    public static Holder<Enchantment> holder(ResourceKey<Enchantment> key) {
        HolderLookup.Provider registries = currentRegistries();
        if (registries == null) return null;
        return registries.lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(key)).<Holder<Enchantment>>map(h -> h).orElse(null);
    }

    /^* An enchanted book with the enchantment, or an empty stack when it is not available. ^/
    public static ItemStack book(ResourceKey<Enchantment> key, int level) {
        Holder<Enchantment> holder = holder(key);
        return holder == null ? ItemStack.EMPTY : net.minecraft.world.item.EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level));
    }

    /^* Every enchantment of the running world, or none outside a world. ^/
    public static java.util.stream.Stream<Holder<Enchantment>> allHolders() {
        HolderLookup.Provider registries = currentRegistries();
        if (registries == null) return java.util.stream.Stream.empty();
        return registries.lookup(Registries.ENCHANTMENT)
                .map(lookup -> lookup.listElements().<Holder<Enchantment>>map(h -> h))
                .orElseGet(java.util.stream.Stream::empty);
    }

    @Nullable
    private static HolderLookup.Provider currentRegistries() {
        var server = KineticServerRuntime.currentServer();
        if (server != null) return server.registryAccess();
        return KineticPlatform.callOnClient(() -> () -> {
            var level = KineticClientRuntime.currentLevel();
            return level == null ? null : level.registryAccess();
        }, null);
    }

    private static ItemEnchantments enchantments(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }
}
*///?} else {
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
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
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

    /** Level of an enchantment applied to the item (not stored in a book). */
    public static int levelOn(ItemStack stack, Enchantment enchantment) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    /** Level counting a book's stored enchantments too, as the tooltips and anvil see them. */
    public static int levelIncludingStored(ItemStack stack, Enchantment enchantment) {
        return EnchantmentHelper.getEnchantments(stack).getOrDefault(enchantment, 0);
    }

    /** An enchanted book with the enchantment. */
    public static ItemStack book(Enchantment enchantment, int level) {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
    }
}
//?}
