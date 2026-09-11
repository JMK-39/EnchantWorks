package dev.xyat.enchantworks.enchantment.init;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.enchantment.recipe.OmniToolRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeInit {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, EnchantWorks.MODID);
    public static final RegistryObject<RecipeSerializer<OmniToolRecipe>> OMNI_TOOL_SERIALIZER = SERIALIZERS.register("omni_tool_crafting", OmniToolRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
