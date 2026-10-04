package dev.xyat.enchantworks.anvil.mixin;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.anvil.util.TaxFreeLevelsLogic;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnvilEnchantmentCoreMixin {

    @Mixin(value = AnvilMenu.class, priority = 2000)
    public abstract static class AnvilMenuFix extends ItemCombinerMenu {
        @Shadow @Final private DataSlot cost;

        public AnvilMenuFix(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
            super(type, containerId, playerInventory, access);
        }

        @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40), require = 0)
        private int enchantworks_enchant$removeLevelCap(int old) {
            ItemStack stack = this.inputSlots.getItem(0);
            return AnvilEnchantmentConfig.shouldRemoveAnvilLimit(stack) ? Integer.MAX_VALUE : old;
        }

        @Inject(method = "createResult", at = @At("RETURN"), require = 0)
        private void enchantworks_enchant$removePriorWorkPenalty(CallbackInfo ci) {
            ItemStack outputStack = this.resultSlots.getItem(0);
            if (!outputStack.isEmpty() && AnvilEnchantmentConfig.shouldRemoveAnvilLimit(outputStack)) {
                outputStack.setRepairCost(0);
            }
        }

        @Inject(method = "createResult", at = @At("RETURN"), require = 0)
        private void enchantworks_enchant$validateAnvilResult(CallbackInfo ci) {
            ItemStack outputStack = this.resultSlots.getItem(0);
            if (outputStack.isEmpty()) return;

            //? if >=1.21 {
            /*var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(outputStack).keySet();
            if (enchantments.isEmpty()) return;

            for (var enchantment : enchantments) {
            *///?} else {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(outputStack);
            if (enchantments.isEmpty()) return;

            for (Enchantment enchantment : enchantments.keySet()) {
            //?}
                if (AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment)) {
                    this.enchantworks_enchant$invalidateResult();
                    return;
                }
                if (AnvilEnchantmentConfig.isExplicitlyAllowed(outputStack, enchantment)) continue;
                if (AnvilEnchantmentConfig.isExplicitlyDenied(outputStack, enchantment)) {
                    this.enchantworks_enchant$invalidateResult();
                    return;
                }
            }
        }

        @Unique
        private void enchantworks_enchant$invalidateResult() {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            this.broadcastChanges();
        }

        @Inject(method = "createResult", at = @At("RETURN"), require = 0)
        private void enchantworks_enchant$makeRenamingCheap(CallbackInfo ci) {
            if (!AnvilEnchantmentConfig.enableCheapRenaming) return;
            ItemStack input1 = this.inputSlots.getItem(0);
            ItemStack input2 = this.inputSlots.getItem(1);
            ItemStack result = this.resultSlots.getItem(0);
            if (!input1.isEmpty() && input2.isEmpty() && !result.isEmpty() && this.cost.get() > 0) {
                this.cost.set(1);
            }
        }

        @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"), require = 0)
        private void enchantworks_enchant$flattenAnvilCost(Player instance, int levels) {
            if (!AnvilEnchantmentConfig.enableTaxFreeLevels) {
                instance.giveExperienceLevels(levels);
                return;
            }
            TaxFreeLevelsLogic.applyFlattenedXpCost(instance, -levels);
        }
    }

    @Mixin(value = Player.class, priority = 2000)
    public abstract static class PlayerFix {
        @ModifyArg(method = "onEnchantmentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"), index = 0, require = 0)
        private int enchantworks_enchant$flattenEnchantmentCost(int levels) {
            if (!AnvilEnchantmentConfig.enableTaxFreeLevels) return levels;
            TaxFreeLevelsLogic.applyFlattenedXpCost((Player) (Object) this, -levels);
            return 0;
        }
    }

    @Mixin(value = EnchantmentMenu.class, priority = 2000)
    public abstract static class EnchantmentMenuFix {
        @Shadow @Final public int[] costs;

        @Inject(method = "clickMenuButton", at = @At("HEAD"), require = 0)
        private void enchantworks_enchant$setLevelRequirement(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
            if (this.costs != null && id >= 0 && id < this.costs.length) {
                TaxFreeLevelsLogic.setLevelRequirement(this.costs[id]);
            }
        }

        @Inject(method = "getEnchantmentList", at = @At("RETURN"), cancellable = true, require = 0)
        private void enchantworks_enchant$filterDisabledEnchantmentResults(
                //? if >=1.21
                /*net.minecraft.core.RegistryAccess registries,*/
                ItemStack stack,
                int slot,
                int power,
                CallbackInfoReturnable<List<EnchantmentInstance>> cir
        ) {
            List<EnchantmentInstance> original = cir.getReturnValue();
            if (original == null || original.isEmpty()) return;

            List<EnchantmentInstance> filtered = new ArrayList<>(original);
            filtered.removeIf(instance -> instance == null
                    || AnvilEnchantmentConfig.isEnchantmentDisabled(instance.enchantment)
                    || AnvilEnchantmentConfig.isExplicitlyDenied(stack, instance.enchantment));
            if (filtered.size() != original.size()) {
                cir.setReturnValue(filtered);
            }
        }
    }

    @Mixin(value = Enchantment.class, priority = 2000)
    public abstract static class EnchantmentFix {
        // Since 1.21 the anvil and the enchanting table both ask isSupportedItem (through NeoForge's
        // supportsEnchantment and isPrimaryItemFor), and trading and discovery are enchantment tags.
        //? if >=1.21 {
        /*@Inject(method = "isSupportedItem", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$interceptSupportedItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            Enchantment self = (Enchantment) (Object) this;
            if (AnvilEnchantmentConfig.isEnchantmentDisabled(self)) {
                cir.setReturnValue(false);
                return;
            }
            if (AnvilEnchantmentConfig.isExplicitlyDenied(stack, self)) {
                cir.setReturnValue(false);
                return;
            }
            if (AnvilEnchantmentConfig.isExplicitlyAllowed(stack, self)) {
                cir.setReturnValue(true);
            }
        }
        *///?} else {
        @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$interceptCanEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            Enchantment self = (Enchantment) (Object) this;
            if (AnvilEnchantmentConfig.isEnchantmentDisabled(self)) {
                cir.setReturnValue(false);
                return;
            }
            if (AnvilEnchantmentConfig.isExplicitlyDenied(stack, self)) {
                cir.setReturnValue(false);
                return;
            }
            if (AnvilEnchantmentConfig.isExplicitlyAllowed(stack, self)) {
                cir.setReturnValue(true);
            }
        }

        @Inject(method = "canApplyAtEnchantingTable", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
        private void enchantworks_enchant$interceptCanApplyAtEnchantingTable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            Enchantment self = (Enchantment) (Object) this;
            if (AnvilEnchantmentConfig.isEnchantmentDisabled(self)) {
                cir.setReturnValue(false);
                return;
            }
            if (AnvilEnchantmentConfig.isExplicitlyDenied(stack, self)) {
                cir.setReturnValue(false);
                return;
            }
            if (AnvilEnchantmentConfig.isExplicitlyAllowed(stack, self)) {
                cir.setReturnValue(true);
            }
        }

        @Inject(method = "isDiscoverable", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$hideDisabledDiscoverable(CallbackInfoReturnable<Boolean> cir) {
            if (AnvilEnchantmentConfig.isEnchantmentDisabled((Enchantment) (Object) this)) {
                cir.setReturnValue(false);
            }
        }

        @Inject(method = "isTradeable", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$hideDisabledTradeable(CallbackInfoReturnable<Boolean> cir) {
            if (AnvilEnchantmentConfig.isEnchantmentDisabled((Enchantment) (Object) this)) {
                cir.setReturnValue(false);
            }
        }
        //?}
    }

    @Mixin(value = ItemStack.class, priority = 2000)
    public abstract static class ItemStackFix {
        @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$allowWhitelistedItemEnchanting(CallbackInfoReturnable<Boolean> cir) {
            ItemStack stack = (ItemStack) (Object) this;
            if (!stack.isEnchanted() && AnvilEnchantmentConfig.hasExplicitAllowRuleForStack(stack)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Mixin(value = Item.class, priority = 2000)
    public abstract static class ItemFix {
        @Inject(method = "getEnchantmentValue", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$allowWhitelistedItemEnchantingValue(CallbackInfoReturnable<Integer> cir) {
            Item item = (Item) (Object) this;
            ItemStack stack = new ItemStack(item);
            if (AnvilEnchantmentConfig.hasExplicitAllowRuleForStack(stack)) {
                cir.setReturnValue(10);
            }
        }
    }

    @Mixin(value = EnchantmentHelper.class, priority = 2000)
    public static class EnchantmentHelperFix {
        //? if >=1.21 {
        /*@Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"), require = 0)
        private static void enchantworks_enchant$addCustomEnchantments(int power, ItemStack stack,
                java.util.stream.Stream<net.minecraft.core.Holder<Enchantment>> pool, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
            if (stack.isEmpty()) return;
            List<EnchantmentInstance> list = cir.getReturnValue();

            list.removeIf(instance -> AnvilEnchantmentConfig.isEnchantmentDisabled(instance.enchantment) || AnvilEnchantmentConfig.isExplicitlyDenied(stack, instance.enchantment));

            // Whitelisted enchantments are offered too; the table never offers curses or treasure.
            EnchantmentInit.allHolders().forEach(enchantment -> {
                if (AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment)) return;
                if (enchantment.is(net.minecraft.tags.EnchantmentTags.CURSE) || enchantment.is(net.minecraft.tags.EnchantmentTags.TREASURE)) return;
                if (AnvilEnchantmentConfig.isExplicitlyAllowed(stack, enchantment)) {
                    enchantworks_enchant$addSafeEntry(list, power, enchantment);
                }
            });
        }

        @Unique
        private static void enchantworks_enchant$addSafeEntry(List<EnchantmentInstance> list, int power, net.minecraft.core.Holder<Enchantment> holder) {
            for (EnchantmentInstance instance : list) if (instance.enchantment.equals(holder)) return;
            Enchantment enchantment = holder.value();
            for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
                if (enchantment.getMinCost(level) <= power && power <= enchantment.getMaxCost(level)) {
                    list.add(new EnchantmentInstance(holder, level));
                    break;
                }
            }
        }
        *///?} else {
        @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"), require = 0)
        private static void enchantworks_enchant$addCustomEnchantments(int power, ItemStack stack, boolean allowTreasure, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
            if (stack.isEmpty()) return;
            List<EnchantmentInstance> list = cir.getReturnValue();

            list.removeIf(instance -> AnvilEnchantmentConfig.isEnchantmentDisabled(instance.enchantment) || AnvilEnchantmentConfig.isExplicitlyDenied(stack, instance.enchantment));

            for (Enchantment enchantment : KineticRegistries.enchantments().values()) {
                if (AnvilEnchantmentConfig.isEnchantmentDisabled(enchantment)) continue;
                if (enchantment.isCurse() || (enchantment.isTreasureOnly() && !allowTreasure)) continue;
                if (AnvilEnchantmentConfig.isExplicitlyAllowed(stack, enchantment)) {
                    enchantworks_enchant$addSafeEntry(list, power, enchantment);
                }
            }
        }

        @Unique
        private static void enchantworks_enchant$addSafeEntry(List<EnchantmentInstance> list, int power, Enchantment enchantment) {
            for (EnchantmentInstance instance : list) if (instance.enchantment == enchantment) return;
            for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
                if (enchantment.getMinCost(level) <= power && power <= enchantment.getMaxCost(level)) {
                    list.add(new EnchantmentInstance(enchantment, level));
                    break;
                }
            }
        }
        //?}
    }
}

