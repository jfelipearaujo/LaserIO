package com.direwolf20.laserio.datagen.customrecipes;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CardClearRecipe implements CraftingRecipe {
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;
    private final boolean isSimple;

    public CardClearRecipe(String pGroup, CraftingBookCategory pCategory, ItemStack pResult, NonNullList<Ingredient> pIngredients) {
        this.group = pGroup;
        this.category = pCategory;
        this.result = pResult;
        this.ingredients = pIngredients;
        this.isSimple = pIngredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        StackedContents stackedcontents = new StackedContents();
        java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
        int i = 0;

        for (int j = 0; j < craftingInput.size(); ++j) {
            ItemStack itemstack = craftingInput.getItem(j);
            if (!itemstack.isEmpty()) {
                ++i;
                if (isSimple)
                    stackedcontents.accountStack(itemstack, 1);
                else inputs.add(itemstack);
            }
        }

        return i == this.ingredients.size() && (isSimple ? stackedcontents.canCraft(this, null) : net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(inputs, this.ingredients) != null);
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        ItemStack itemStack = craftingInput.getItem(0);
        if (itemStack.getItem() instanceof BaseCard) {
            CardItemHandler cardItemHandler = BaseCard.getInventory(itemStack);
            if (!cardItemHandler.getStackInSlot(0).isEmpty()) {
                return cardItemHandler.getStackInSlot(0);
            } else if (!cardItemHandler.getStackInSlot(1).isEmpty()) {
                return cardItemHandler.getStackInSlot(1);
            }
        }
        return this.result.copy();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= this.ingredients.size();
    }

    /**
     * Since this crafting type is always clearing a single card, we make a lot of assumptions like its a single card in the crafting window
     */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(1, ItemStack.EMPTY);

        ItemStack itemStack = craftingInput.getItem(0);
        if (itemStack.getItem() instanceof BaseCard) {
            CardItemHandler cardItemHandler = BaseCard.getInventory(itemStack);
            if (!cardItemHandler.getStackInSlot(0).isEmpty()) {
                ItemStack returnStack = itemStack.copy();
                BaseCard.getInventory(returnStack).setStackInSlot(0, ItemStack.EMPTY);
                nonnulllist.set(0, returnStack);
                return nonnulllist;
            } else if (!cardItemHandler.getStackInSlot(1).isEmpty()) {
                ItemStack returnStack = itemStack.copy();
                BaseCard.getInventory(returnStack).setStackInSlot(1, ItemStack.EMPTY);
                nonnulllist.set(0, returnStack);
                return nonnulllist;
            }
        } else {
            if (itemStack.hasCraftingRemainingItem()) {
                nonnulllist.set(0, itemStack.getCraftingRemainingItem());
            }
        }
        return nonnulllist;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.CARD_CLEAR_RECIPE_SERIALIZER.get();
    }


    public static class Serializer implements RecipeSerializer<CardClearRecipe> {
        private static final net.minecraft.resources.ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "cardclear");
        private static final MapCodec<CardClearRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_311734_ -> p_311734_.group(
                                Codec.STRING.fieldOf("group").forGetter(p_301127_ -> p_301127_.group),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_301133_ -> p_301133_.category),
                                ItemStack.CODEC.fieldOf("result").forGetter(p_301142_ -> p_301142_.result),
                                Ingredient.CODEC_NONEMPTY
                                        .listOf()
                                        .fieldOf("ingredients")
                                        .flatXmap(
                                                p_301021_ -> {
                                                    Ingredient[] aingredient = p_301021_
                                                            .toArray(Ingredient[]::new); //Forge skip the empty check and immediatly create the array.
                                                    if (aingredient.length == 0) {
                                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                                    } else {
                                                        return aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()
                                                                ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()))
                                                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                                    }
                                                },
                                                DataResult::success
                                        )
                                        .forGetter(p_300975_ -> p_300975_.ingredients)
                        )
                        .apply(p_311734_, CardClearRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, CardClearRecipe> STREAM_CODEC = StreamCodec.of(
                CardClearRecipe.Serializer::toNetwork, CardClearRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<CardClearRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CardClearRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static CardClearRecipe fromNetwork(RegistryFriendlyByteBuf pBuffer) {
            String s = pBuffer.readUtf();
            CraftingBookCategory craftingbookcategory = pBuffer.readEnum(CraftingBookCategory.class);
            int i = pBuffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

            nonnulllist.replaceAll(p_319733_ -> Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer));

            ItemStack itemstack = ItemStack.OPTIONAL_STREAM_CODEC.decode(pBuffer);
            return new CardClearRecipe(s, craftingbookcategory, itemstack, nonnulllist);
        }

        public static void toNetwork(RegistryFriendlyByteBuf pBuffer, CardClearRecipe pRecipe) {
            pBuffer.writeUtf(pRecipe.group);
            pBuffer.writeEnum(pRecipe.category);
            pBuffer.writeVarInt(pRecipe.ingredients.size());

            for (Ingredient ingredient : pRecipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, ingredient);
            }

            ItemStack.OPTIONAL_STREAM_CODEC.encode(pBuffer, pRecipe.result);
        }
    }
}
