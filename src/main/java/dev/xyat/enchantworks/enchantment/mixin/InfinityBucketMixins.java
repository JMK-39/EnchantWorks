package dev.xyat.enchantworks.enchantment.mixin;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class InfinityBucketMixins {

    @Mixin(ItemStack.class)
    public abstract static class ItemStackFix {
        @Shadow public abstract int getCount();
        @Shadow public abstract Item getItem();

        @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
        private void enchantworks_enchant$limitBucketEnchanting(CallbackInfoReturnable<Boolean> cir) {
            if (this.getItem() instanceof BucketItem) {
                cir.setReturnValue(this.getCount() <= 1);
            }
        }
    }

    @Mixin(BucketItem.class)
    public static class BucketItemFix {
        @Inject(method = "getEmptySuccessItem", at = @At("HEAD"), cancellable = true)
        private static void enchantworks_enchant$infiniteEmptyResult(ItemStack pBucketStack, Player pPlayer, CallbackInfoReturnable<ItemStack> cir) {
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pBucketStack) > 0) {
                cir.setReturnValue(pBucketStack.copy());
            }
        }

        @Inject(method = "use", at = @At("RETURN"), cancellable = true)
        private void enchantworks_enchant$keepInfiniteBucket(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            ItemStack original = pPlayer.getItemInHand(pHand);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, original) > 0) {
                InteractionResultHolder<ItemStack> result = cir.getReturnValue();
                if (result.getResult().consumesAction()) {
                    cir.setReturnValue(new InteractionResultHolder<>(result.getResult(), original.copy()));
                }
            }
        }
    }

    @Mixin(ItemUtils.class)
    public static class ItemUtilsFix {
        @Inject(method = "createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
        private static void enchantworks_enchant$lockInfinityBucketState4(ItemStack pEmptyStack, Player pPlayer, ItemStack pFilledStack, boolean preventDup, CallbackInfoReturnable<ItemStack> cir) {
            if (pEmptyStack.getItem() instanceof BucketItem && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pEmptyStack) > 0) {
                cir.setReturnValue(pEmptyStack.copy());
            }
        }

        @Inject(method = "createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
        private static void enchantworks_enchant$lockInfinityBucketState3(ItemStack pEmptyStack, Player pPlayer, ItemStack pFilledStack, CallbackInfoReturnable<ItemStack> cir) {
            if (pEmptyStack.getItem() instanceof BucketItem && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pEmptyStack) > 0) {
                cir.setReturnValue(pEmptyStack.copy());
            }
        }
    }

    @Mixin(Enchantment.class)
    public static class EnchantmentFix {
        @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
        private void enchantworks_enchant$allowInfinityAnvil(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if ((Object) this == Enchantments.INFINITY_ARROWS && stack.getItem() instanceof BucketItem) {
                cir.setReturnValue(!AnvilEnchantmentConfig.isEnchantmentDisabled(Enchantments.INFINITY_ARROWS));
            }
        }

        @Inject(method = "canApplyAtEnchantingTable", at = @At("HEAD"), cancellable = true, remap = false)
        private void enchantworks_enchant$allowInfinityTable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if ((Object) this == Enchantments.INFINITY_ARROWS && stack.getItem() instanceof BucketItem) {
                cir.setReturnValue(!AnvilEnchantmentConfig.isEnchantmentDisabled(Enchantments.INFINITY_ARROWS));
            }
        }
    }

    @Mixin(Item.class)
    public static class ItemFix {
        @Inject(method = "getEnchantmentValue", at = @At("HEAD"), cancellable = true)
        private void enchantworks_enchant$bucketEnchantmentValue(CallbackInfoReturnable<Integer> cir) {
            if ((Object) this instanceof BucketItem) {
                cir.setReturnValue(10);
            }
        }
    }

    @Mixin(value = FluidBucketWrapper.class, remap = false)
    public static class FluidBucketWrapperFix {
        @Shadow protected ItemStack container;
        @Unique private ItemStack enchantworks_enchant$savedContainer;

        @Inject(method = "drain(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("HEAD"))
        private void saveDrain1(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
            this.enchantworks_enchant$savedContainer = this.container.copy();
        }

        @Inject(method = "drain(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("RETURN"))
        private void restoreDrain1(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
            if (action.execute() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, this.enchantworks_enchant$savedContainer) > 0) {
                this.container = this.enchantworks_enchant$savedContainer.copy();
            }
        }

        @Inject(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("HEAD"))
        private void saveDrain2(int maxDrain, IFluidHandler.FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
            this.enchantworks_enchant$savedContainer = this.container.copy();
        }

        @Inject(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("RETURN"))
        private void restoreDrain2(int maxDrain, IFluidHandler.FluidAction action, CallbackInfoReturnable<FluidStack> cir) {
            if (action.execute() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, this.enchantworks_enchant$savedContainer) > 0) {
                this.container = this.enchantworks_enchant$savedContainer.copy();
            }
        }

        @Inject(method = "fill", at = @At("HEAD"))
        private void saveFill(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<Integer> cir) {
            this.enchantworks_enchant$savedContainer = this.container.copy();
        }

        @Inject(method = "fill", at = @At("RETURN"))
        private void restoreFill(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<Integer> cir) {
            if (action.execute() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, this.enchantworks_enchant$savedContainer) > 0) {
                this.container = this.enchantworks_enchant$savedContainer.copy();
            }
        }
    }
}
