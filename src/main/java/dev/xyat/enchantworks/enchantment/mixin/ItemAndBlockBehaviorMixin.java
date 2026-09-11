package dev.xyat.enchantworks.enchantment.mixin;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.smelter.SmelterEventHandler;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemAndBlockBehaviorMixin {

    @Mixin(value = BowItem.class, priority = 2000)
    public static class BowItemFix {
        @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"), require = 0)
        private ItemStack enchantworks_enchant$allowDrawWithoutArrow(Player player, ItemStack shootable) {
            ItemStack projectile = player.getProjectile(shootable);
            if (projectile.isEmpty() && shootable.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0) return new ItemStack(Items.ARROW);
            return projectile;
        }

        @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"), require = 0)
        private ItemStack enchantworks_enchant$fireWithoutArrow(Player player, ItemStack shootable) {
            ItemStack projectile = player.getProjectile(shootable);
            if (projectile.isEmpty() && shootable.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0) return new ItemStack(Items.ARROW);
            return projectile;
        }
    }

    @Mixin(value = ThrownTrident.class, priority = 2000)
    public abstract static class ThrownTridentFix extends AbstractArrow {
        protected ThrownTridentFix(EntityType<? extends AbstractArrow> type, Level level) { super(type, level); }
        @Shadow protected abstract @NotNull ItemStack getPickupItem();

        @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isThundering()Z"), require = 0)
        private boolean enchantworks_enchant$betterChanneling(Level level) {
            if (level.isClientSide() || !AnvilEnchantmentConfig.isBetterChannelingEnabled()) return level.isThundering();
            ItemStack stack = this.getPickupItem();
            if (stack.isEmpty()) return level.isThundering();
            return stack.getEnchantmentLevel(Enchantments.CHANNELING) >= 2 || level.isThundering();
        }
    }

    @Mixin(value = ItemStack.class, priority = 2000)
    public abstract static class ItemStackFix {
        /*
         * 关键修复：万能工具只模拟“镐/斧/铲/锄/剪刀”的正确工具判定。
         * 如果这些原版工具都不能让该方块掉落，万能工具也不能强行掉落。
         * 这样黑曜石、矿石、树叶等会按对应工具规则掉落，但刷怪笼这类本来无掉落的方块不会被挖下来。
         */
        @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$omniToolIsCorrectTool(BlockState state, CallbackInfoReturnable<Boolean> cir) {
            ItemStack stack = (ItemStack) (Object) this;
            if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL) || EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, stack) <= 0) return;

            boolean correct = new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(state)
                    || new ItemStack(Items.NETHERITE_AXE).isCorrectToolForDrops(state)
                    || new ItemStack(Items.NETHERITE_SHOVEL).isCorrectToolForDrops(state)
                    || new ItemStack(Items.NETHERITE_HOE).isCorrectToolForDrops(state)
                    || new ItemStack(Items.SHEARS).isCorrectToolForDrops(state);

            if (correct) {
                cir.setReturnValue(true);
            }
        }

        /*
         * 不在这里修改破坏速度，避免 ItemStack#getDestroySpeed 与 BreakSpeed 事件重复叠乘。
         * Jade 的“可挖掘”显示主要依赖 isCorrectToolForDrops，上面的正确工具判定已经处理。
         * 实际速度统一交给 OmniToolEvent#onBreakSpeed，根据配置倍率和方块硬度计算。
         */
    }


    @Mixin(value = BlockBehaviour.class, priority = 2000)
    public static class BlockFix {
        /*
         * 安全版掉落修复：
         * 1. 绝不在 BlockBehaviour#getDrops 里再次调用 state.getDrops(params)，避免递归卡死。
         * 2. 非配置名单方块继续遵守原版/数据包战利品表。
         * 3. 配置名单里的方块才强制掉落自身，例如 minecraft:spawner。
         * 4. 剪刀类脆弱方块只做安全白名单处理，不会影响刷怪笼这类特殊无掉落方块。
         */
        @Inject(method = "getDrops", at = @At("RETURN"), cancellable = true, require = 0)
        private void enchantworks_enchant$omniToolDropsSafely(BlockState state, LootParams.Builder params, CallbackInfoReturnable<List<ItemStack>> cir) {
            if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return;

            ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
            if (tool == null || tool.isEmpty() || EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, tool) <= 0) return;

            List<ItemStack> originalDrops = cir.getReturnValue();
            if (originalDrops != null && !originalDrops.isEmpty()) return;

            if (AnvilEnchantmentConfig.shouldOmniToolForceDrop(state) && state.getBlock().asItem() != Items.AIR) {
                List<ItemStack> forcedDrops = new ArrayList<>();
                forcedDrops.add(new ItemStack(state.getBlock()));
                cir.setReturnValue(forcedDrops);
                return;
            }

            if (enchantworks_enchant$isSafeShearsSelfDrop(state) && state.getBlock().asItem() != Items.AIR) {
                List<ItemStack> shearsDrops = new ArrayList<>();
                shearsDrops.add(new ItemStack(state.getBlock()));
                cir.setReturnValue(shearsDrops);
            }
        }

        @Unique
        private static boolean enchantworks_enchant$isSafeShearsSelfDrop(BlockState state) {
            return state.is(BlockTags.LEAVES)
                    || state.is(Blocks.COBWEB)
                    || state.is(Blocks.VINE)
                    || state.is(Blocks.GLOW_LICHEN)
                    || state.is(Blocks.GRASS)
                    || state.is(Blocks.TALL_GRASS)
                    || state.is(Blocks.FERN)
                    || state.is(Blocks.LARGE_FERN)
                    || state.is(Blocks.DEAD_BUSH)
                    || state.is(Blocks.SEAGRASS)
                    || state.is(Blocks.TALL_SEAGRASS);
        }
    }

    @Mixin(value = EnchantmentHelper.class, priority = 2000)
    public static class EnchantmentHelperFix {
        @Inject(method = "getItemEnchantmentLevel", at = @At("HEAD"), cancellable = true, require = 0)
        private static void enchantworks_enchant$disableFortuneForSmelter(
                Enchantment enchantment,
                ItemStack stack,
                CallbackInfoReturnable<Integer> cir
        ) {
            if (enchantment == Enchantments.BLOCK_FORTUNE && SmelterEventHandler.shouldSuppressFortune(stack)) {
                cir.setReturnValue(0);
            }
        }
    }

    @Mixin(value = EnchantmentMenu.class, priority = 2000)
    public static class EnchantmentMenuFix {
        @Shadow @Final private Container enchantSlots;
        @Shadow @Final private DataSlot enchantmentSeed;
        @Shadow @Final public int[] costs;

        @Unique
        private boolean enchantworks_enchant$thirdSlotShouldApplyOmni;

        @Unique
        private boolean enchantworks_enchant$thirdSlotInputWasBook;

        /**
         * 只改附魔台第三档的结果列表。
         * 命中后直接把第三档结果替换成“万能工具 I”，避免显示普通附魔但实际又要额外隐藏追加。
         */
        @Inject(method = "getEnchantmentList", at = @At("RETURN"), cancellable = true, require = 0)
        private void enchantworks_enchant$rollOmniToolOnThirdSlot(ItemStack stack, int enchantmentSlot, int level, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
            if (enchantmentSlot != 2) return;
            if (enchantworks_enchant$shouldSkipOmniRoll(stack, level)) return;

            List<EnchantmentInstance> original = cir.getReturnValue();
            if (enchantworks_enchant$containsOmni(original)) return;

            float chance = enchantworks_enchant$getOmniToolEnchantChance();
            float roll = enchantworks_enchant$deterministicOmniRoll(stack, enchantmentSlot, level, this.enchantmentSeed.get());
            if (roll >= chance) return;

            Enchantment omniTool = EnchantmentInit.get(EnchantmentInit.OMNI_TOOL);
            if (omniTool == null) return;
            List<EnchantmentInstance> result = new ArrayList<>();
            result.add(new EnchantmentInstance(omniTool, 1));
            cir.setReturnValue(result);
        }

        /**
         * 玩家实际点击第三档时，用和预览完全一致的确定性随机数记录本次是否应该写入万能工具。
         */
        @Inject(method = "clickMenuButton", at = @At("HEAD"), require = 0)
        private void enchantworks_enchant$prepareOmniToolClickFlag(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
            this.enchantworks_enchant$thirdSlotShouldApplyOmni = false;
            this.enchantworks_enchant$thirdSlotInputWasBook = false;

            if (id != 2) return;

            ItemStack input = this.enchantSlots.getItem(0);
            int displayedLevel = enchantworks_enchant$getDisplayedLevel(id);
            if (enchantworks_enchant$shouldSkipOmniRoll(input, displayedLevel)) return;

            float chance = enchantworks_enchant$getOmniToolEnchantChance();
            float roll = enchantworks_enchant$deterministicOmniRoll(input, id, displayedLevel, this.enchantmentSeed.get());
            this.enchantworks_enchant$thirdSlotShouldApplyOmni = roll < chance;
            this.enchantworks_enchant$thirdSlotInputWasBook = input.is(Items.BOOK) || input.is(Items.ENCHANTED_BOOK);
        }

        /**
         * 实际点击附魔后，在返回阶段直接修正输入槽内的最终物品。
         * 不使用 Redirect，避免不同映射/不同 Forge 小版本下 setItem 调用签名不一致导致注入签名报错。
         */
        @Inject(method = "clickMenuButton", at = @At("RETURN"), require = 0)
        private void enchantworks_enchant$applyOmniToolAfterEnchant(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
            try {
                if (id == 2 && Boolean.TRUE.equals(cir.getReturnValue()) && this.enchantworks_enchant$thirdSlotShouldApplyOmni) {
                    ItemStack result = this.enchantSlots.getItem(0);
                    if (!result.isEmpty()) {
                        Enchantment omniTool = EnchantmentInit.get(EnchantmentInit.OMNI_TOOL);
                        if (omniTool == null) return;
                        if (this.enchantworks_enchant$thirdSlotInputWasBook || result.is(Items.BOOK) || result.is(Items.ENCHANTED_BOOK)) {
                            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                            EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(omniTool, 1));
                            this.enchantSlots.setItem(0, book);
                        } else {
                            Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
                            enchantments.put(omniTool, 1);
                            EnchantmentHelper.setEnchantments(enchantments, result);
                            this.enchantSlots.setItem(0, result);
                        }
                        this.enchantSlots.setChanged();
                        ((EnchantmentMenu) (Object) this).broadcastChanges();
                    }
                }
            } finally {
                this.enchantworks_enchant$thirdSlotShouldApplyOmni = false;
                this.enchantworks_enchant$thirdSlotInputWasBook = false;
            }
        }

        @Unique
        private int enchantworks_enchant$getDisplayedLevel(int slot) {
            return this.costs != null && slot >= 0 && slot < this.costs.length ? this.costs[slot] : 0;
        }

        @Unique
        private static boolean enchantworks_enchant$shouldSkipOmniRoll(ItemStack stack, int displayedLevel) {
            if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return true;
            if (stack == null || stack.isEmpty()) return true;
            if (displayedLevel < 30) return true;
            Enchantment omniTool = EnchantmentInit.get(EnchantmentInit.OMNI_TOOL);
            return omniTool == null || (!enchantworks_enchant$isBookForEnchanting(stack) && !omniTool.canApplyAtEnchantingTable(stack));
        }

        @Unique
        private static boolean enchantworks_enchant$isBookForEnchanting(ItemStack stack) {
            return stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
        }

        @Unique
        private static float enchantworks_enchant$getOmniToolEnchantChance() {
            double value = AnvilEnchantmentConfig.omniToolEnchantChance;
            if (Double.isNaN(value) || Double.isInfinite(value)) return 0.10F;
            if (value < 0.0D) return 0.0F;
            if (value > 1.0D) return 1.0F;
            return (float) value;
        }

        @Unique
        private static float enchantworks_enchant$deterministicOmniRoll(ItemStack stack, int slot, int displayedLevel, int enchantmentSeed) {
            String itemId = enchantworks_enchant$itemId(stack);
            long seed = 0x6A09E667F3BCC909L;
            seed ^= (long) enchantmentSeed * 0x9E3779B97F4A7C15L;
            seed ^= (long) slot * 0xBF58476D1CE4E5B9L;
            seed ^= (long) displayedLevel * 0x94D049BB133111EBL;
            seed ^= (long) itemId.hashCode() * 0x632BE59BD9B4E019L;
            seed = enchantworks_enchant$mix64(seed);
            return RandomSource.create(seed).nextFloat();
        }

        @Unique
        private static long enchantworks_enchant$mix64(long z) {
            z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
            z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
            return z ^ (z >>> 31);
        }

        @Unique
        private static boolean enchantworks_enchant$containsOmni(List<EnchantmentInstance> list) {
            if (list == null || list.isEmpty()) return false;
            for (EnchantmentInstance instance : list) {
                if (instance != null && EnchantmentInit.isSame(instance.enchantment, EnchantmentInit.OMNI_TOOL)) return true;
            }
            return false;
        }

        @Unique
        private static String enchantworks_enchant$itemId(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return "empty";
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return id == null ? String.valueOf(stack.getItem()) : id.toString();
        }
    }

    @Mixin(value = SpawnerBlock.class, priority = 2000)
    public static class SpawnerBlockFix {
        /*
         * 修复：万能工具通过“强制掉落名单”挖下刷怪笼时，不应该再掉刷怪笼经验。
         * 原因：刷怪笼经验在 SpawnerBlock#spawnAfterBreak 内部生成，不是普通战利品表掉落，
         * 也不一定会被 ServerPlayerGameMode#destroyBlock 里的 popExperience Redirect 拦住。
         */
        @Inject(method = "spawnAfterBreak", at = @At("HEAD"), cancellable = true, require = 0)
        private void enchantworks_enchant$suppressForcedOmniToolSpawnerXp(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience, CallbackInfo ci) {
            if (dropExperience
                    && EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)
                    && state.is(Blocks.SPAWNER)
                    && AnvilEnchantmentConfig.shouldOmniToolForceDrop(state)
                    && EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, stack) > 0) {
                ci.cancel();
            }
        }
    }

    @Mixin(value = ServerPlayerGameMode.class, priority = 2000)
    public static class ServerPlayerGameModeFix {
        @Shadow @Final protected ServerPlayer player;

        /*
         * 只记录当前 ServerPlayerGameMode 这一次 destroyBlock 是否需要压制经验。
         * 注意：字段放在内部 mixin 自己身上，不引用外层 ItemAndBlockBehaviorMixin，避免 IllegalClassLoadError。
         */
        @Unique
        private boolean enchantworks_enchant$suppressForcedOmniToolBlockXp;

        @Inject(at = @At("HEAD"), method = "destroyBlock", require = 0)
        private void enchantworks_enchant$destroyBlockStart(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
            SmelterEventHandler.pushPlayer(this.player);

            this.enchantworks_enchant$suppressForcedOmniToolBlockXp = false;
            if (!EnchantmentInit.isEnabled(EnchantmentInit.OMNI_TOOL)) return;

            BlockState state = this.player.level().getBlockState(pos);
            ItemStack tool = this.player.getMainHandItem();
            this.enchantworks_enchant$suppressForcedOmniToolBlockXp = !tool.isEmpty()
                    && EnchantmentInit.getLevel(EnchantmentInit.OMNI_TOOL, tool) > 0
                    && AnvilEnchantmentConfig.shouldOmniToolForceDrop(state);
        }

        /*
         * Forge/原版破坏流程里，方块经验最终可能从 ServerPlayerGameMode#destroyBlock 调用 Block#popExperience。
         * 对“万能工具强制掉落名单方块”这一类掉落，直接吞掉这次经验；普通破坏仍原样放行。
         */
        @Redirect(method = "destroyBlock",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;I)V"),
                require = 0)
        private void enchantworks_enchant$suppressForcedOmniToolBlockXp(Block block, ServerLevel level, BlockPos pos, int amount) {
            if (this.enchantworks_enchant$suppressForcedOmniToolBlockXp) {
                return;
            }
            block.popExperience(level, pos, amount);
        }

        @Inject(at = @At("RETURN"), method = "destroyBlock", require = 0)
        private void enchantworks_enchant$destroyBlockEnd(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
            SmelterEventHandler.popPlayer(this.player);
            this.enchantworks_enchant$suppressForcedOmniToolBlockXp = false;
        }
    }

}

