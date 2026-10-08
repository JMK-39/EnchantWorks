//? if >=1.21 {
/*package dev.xyat.enchantworksvalidation;

import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/^*
 * Test fixture, never shipped: checks the data-driven enchantments, their tags, the item rules and the recipe in a
 * running server. Registries are only complete once the server has started.
 ^/
@Mod("enchantworks_validation")
public final class RuntimeValidation {
    public RuntimeValidation() {
        if (Boolean.getBoolean("enchantworks.guiValidation")) {
            dev.xyat.kineticcore.api.runtime.KineticPlatform.runOnClient(() -> GuiCaptureValidation::install);
        }
        MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> validate(event.getServer()));
    }

    private static void validate(MinecraftServer server) {
        // Through a plain provider: registryAccess().lookup( is a 26.1 rename target and would be rewritten here.
        net.minecraft.core.HolderLookup.Provider registries = server.registryAccess();
        var enchantments = registries.lookup(Registries.ENCHANTMENT).orElseThrow();
        for (ResourceKey<Enchantment> key : List.of(EnchantmentInit.SMELTER, EnchantmentInit.LEECH,
                EnchantmentInit.SIXTH_SENSE, EnchantmentInit.ENLIGHTENMENT, EnchantmentInit.OMNI_TOOL)) {
            require(enchantments.get(key).isPresent(), "registered " + key);
            require(EnchantmentInit.isEnabled(key), "enabled by default " + key);
        }

        Holder<Enchantment> smelter = enchantments.getOrThrow(EnchantmentInit.SMELTER);
        Holder<Enchantment> leech = enchantments.getOrThrow(EnchantmentInit.LEECH);
        Holder<Enchantment> sixthSense = enchantments.getOrThrow(EnchantmentInit.SIXTH_SENSE);
        Holder<Enchantment> omniTool = enchantments.getOrThrow(EnchantmentInit.OMNI_TOOL);
        Holder<Enchantment> enlightenment = enchantments.getOrThrow(EnchantmentInit.ENLIGHTENMENT);

        // Treasure, trading and discovery as the 1.20.1 enchantment classes declared them.
        require(smelter.is(EnchantmentTags.IN_ENCHANTING_TABLE) && smelter.is(EnchantmentTags.TRADEABLE), "smelter is a normal enchantment");
        require(leech.is(EnchantmentTags.IN_ENCHANTING_TABLE) && enlightenment.is(EnchantmentTags.IN_ENCHANTING_TABLE), "leech and enlightenment at the table");
        require(sixthSense.is(EnchantmentTags.TREASURE) && sixthSense.is(EnchantmentTags.TRADEABLE)
                && sixthSense.is(EnchantmentTags.ON_RANDOM_LOOT) && !sixthSense.is(EnchantmentTags.IN_ENCHANTING_TABLE), "sixth sense is tradeable treasure");
        require(omniTool.is(EnchantmentTags.TREASURE) && !omniTool.is(EnchantmentTags.TRADEABLE)
                && omniTool.is(EnchantmentTags.ON_RANDOM_LOOT) && !omniTool.is(EnchantmentTags.IN_ENCHANTING_TABLE), "omni tool is untradeable treasure");
        require(!Enchantment.areCompatible(smelter, enchantments.getOrThrow(Enchantments.SILK_TOUCH)), "smelter excludes silk touch");
        require(smelter.value().getMaxLevel() == 1 && leech.value().getMaxLevel() == 3 && enlightenment.value().getMaxLevel() == 2, "max levels");

        // Items each enchantment goes on, including the infinity bucket.
        require(new ItemStack(Items.DIAMOND_PICKAXE).supportsEnchantment(omniTool), "omni tool on a pickaxe");
        require(new ItemStack(Items.SHEARS).supportsEnchantment(omniTool), "omni tool on shears");
        require(!new ItemStack(Items.DIAMOND_SWORD).supportsEnchantment(omniTool), "no omni tool on a sword");
        require(new ItemStack(Items.DIAMOND_SWORD).supportsEnchantment(leech), "leech on a sword");
        require(new ItemStack(Items.DIAMOND_HELMET).supportsEnchantment(sixthSense), "sixth sense on a helmet");
        require(new ItemStack(Items.BUCKET).supportsEnchantment(enchantments.getOrThrow(Enchantments.INFINITY)), "infinity on a bucket");

        // Levels are read by key from the item's enchantments.
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(omniTool, 1);
        pickaxe.enchant(smelter, 1);
        require(EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, pickaxe) == 1
                && EnchantmentInit.getLevel(EnchantmentInit.SMELTER, pickaxe) == 1
                && EnchantmentInit.getLevel(EnchantmentInit.LEECH, pickaxe) == 0, "levels by key");
        ItemStack book = EnchantmentInit.book(EnchantmentInit.OMNI_TOOL, 1);
        require(!book.isEmpty() && EnchantmentInit.levelIncludingStored(book, EnchantmentInit.OMNI_TOOL) == 1
                && EnchantmentInit.levelOn(book, EnchantmentInit.OMNI_TOOL) == 0, "omni tool book stores the enchantment");

        // Recipe ids are locations up to 1.21.1 and keys on 26.1; both print the namespaced id.
        require(server.getRecipeManager().getRecipes().stream().anyMatch(recipe ->
                recipe.id().toString().contains("enchantworks:omni_tool_netherite")
                        && recipe.value() instanceof dev.xyat.enchantworks.enchantment.recipe.OmniToolRecipe), "omni tool recipe loaded");

        com.mojang.logging.LogUtils.getLogger().info("ENCHANTWORKS_RUNTIME_VALIDATION_PASS: enchantments, tags, item rules, levels and recipe");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
*///?}
