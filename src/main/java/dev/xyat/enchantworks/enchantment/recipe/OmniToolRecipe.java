package dev.xyat.enchantworks.enchantment.recipe;

//? if >=1.21 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.enchantworks.enchantment.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/^* Shapeless recipe whose result is an Omni Tool book; it only matches while the enchantment is enabled. ^/
public class OmniToolRecipe implements CraftingRecipe {
    private final String group;
    private final NonNullList<Ingredient> ingredients;

    public OmniToolRecipe(String group, NonNullList<Ingredient> ingredients) {
        this.group = group;
        this.ingredients = ingredients;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public @NotNull String getGroup() {
        return this.group;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return false;
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
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
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) {
            return ItemStack.EMPTY;
        }
        return EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(EnchantmentInit.holder(registries, EnchantmentInit.OMNI_TOOL), 1));
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeInit.OMNI_TOOL_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<OmniToolRecipe> {
        private static final MapCodec<OmniToolRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                        list -> list.isEmpty()
                                ? DataResult.<NonNullList<Ingredient>>error(() -> "No ingredients for the Omni Tool recipe")
                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new))),
                        DataResult::success
                ).forGetter(recipe -> recipe.ingredients)
        ).apply(instance, OmniToolRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, OmniToolRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeUtf(recipe.group);
                    buf.writeVarInt(recipe.ingredients.size());
                    for (Ingredient ingredient : recipe.ingredients) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }
                },
                buf -> {
                    String group = buf.readUtf();
                    NonNullList<Ingredient> ingredients = NonNullList.withSize(buf.readVarInt(), Ingredient.EMPTY);
                    ingredients.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    return new OmniToolRecipe(group, ingredients);
                }
        );

        @Override
        public @NotNull MapCodec<OmniToolRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, OmniToolRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
*///?} else {
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

//?}
