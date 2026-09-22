package dev.xyat.enchantworks.enchantment.recipe;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.enchantworks.enchantment.init.RecipeInit;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OmniToolRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final String group;
    private final NonNullList<Ingredient> ingredients;

    public OmniToolRecipe(ResourceLocation id, String group, NonNullList<Ingredient> ingredients) {
        this.id = id;
        this.group = group;
        this.ingredients = ingredients;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return false;
        java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) inputs.add(stack);
        }
        if (inputs.size() != this.ingredients.size()) return false;

        for (Ingredient ingredient : this.ingredients) {
            boolean found = false;
            for (int i = 0; i < inputs.size(); i++) {
                if (ingredient.test(inputs.get(i))) {
                    inputs.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) {
            return ItemStack.EMPTY;
        }
        Enchantment omniTool = EnchantmentInit.get(EnchantmentInit.OMNI_TOOL);
        if (omniTool == null) {
            return ItemStack.EMPTY;
        }
        ItemStack book = new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(omniTool, 1));
        return book;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeInit.OMNI_TOOL_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public static class Serializer implements RecipeSerializer<OmniToolRecipe> {
        @Override
        public @NotNull OmniToolRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            NonNullList<Ingredient> ingredients = itemsFromJson(GsonHelper.getAsJsonArray(json, "ingredients"));
            return new OmniToolRecipe(id, group, ingredients);
        }

        private static NonNullList<Ingredient> itemsFromJson(com.google.gson.JsonArray jsonArray) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < jsonArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i));
                if (!ingredient.isEmpty()) {
                    ingredients.add(ingredient);
                }
            }
            return ingredients;
        }

        @Override
        public @Nullable OmniToolRecipe fromNetwork(@NotNull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            String group = buf.readUtf();
            int size = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buf));
            return new OmniToolRecipe(id, group, ingredients);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull OmniToolRecipe recipe) {
            buf.writeUtf(recipe.group);
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buf);
            }
        }
    }
}

