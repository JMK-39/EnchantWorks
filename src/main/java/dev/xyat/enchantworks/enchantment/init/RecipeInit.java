package dev.xyat.enchantworks.enchantment.init;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.enchantment.recipe.OmniToolRecipe;
import dev.xyat.kineticcore.api.registry.KineticRecipeSerializers;
import dev.xyat.kineticcore.api.registry.KineticRegistryHandle;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;

public class RecipeInit {
    public static final KineticRegistryHandle<OmniToolRecipe.Serializer> OMNI_TOOL_SERIALIZER =
            KineticRecipeSerializers.register(
                    KineticResourceIds.of(EnchantWorks.MODID, "omni_tool_crafting"),
                    OmniToolRecipe.Serializer::new
            );

    public static void register() {
    }
}
