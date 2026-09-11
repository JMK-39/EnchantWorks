package dev.xyat.enchantworks.enchantment.smelter;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Stack;

@Mod.EventBusSubscriber(modid = EnchantWorks.MODID)
public class SmelterEventHandler {
    private static final int MAX_CONTAINER_DEPTH = 4;
    private static final ThreadLocal<Stack<ServerPlayer>> MINING_PLAYER = ThreadLocal.withInitial(Stack::new);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrop(LivingDropsEvent event) {
        if (!EnchantmentInit.isEnabled(EnchantmentInit.SMELTER) || !AnvilEnchantmentConfig.smelterCooksMobs) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player) || player.isCrouching()) {
            return;
        }
        if (!hasSmelter(player.getMainHandItem())) {
            return;
        }

        double experience = 0.0D;
        for (ItemEntity drop : event.getDrops()) {
            SmeltResult result = process(player.level(), drop.getItem(), 0);
            if (result.changed()) {
                drop.setItem(result.stack());
                experience += result.experience();
            }
        }
        awardExperience(player.level(), event.getEntity().position(), experience);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !EnchantmentInit.isEnabled(EnchantmentInit.SMELTER)) {
            return;
        }
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        Stack<ServerPlayer> players = MINING_PLAYER.get();
        if (players.isEmpty()) {
            return;
        }

        ServerPlayer player = players.peek();
        if (player == null || player.isCrouching() || !hasSmelter(player.getMainHandItem())) {
            return;
        }

        SmeltResult result = process(event.getLevel(), itemEntity.getItem(), 0);
        if (!result.changed()) {
            return;
        }

        itemEntity.setItem(result.stack());
        awardExperience(event.getLevel(), itemEntity.position(), result.experience());
    }

    public static void pushPlayer(ServerPlayer player) {
        if (player != null) {
            MINING_PLAYER.get().push(player);
        }
    }

    public static void popPlayer(ServerPlayer player) {
        Stack<ServerPlayer> players = MINING_PLAYER.get();
        if (!players.isEmpty() && players.peek() == player) {
            players.pop();
        }
        if (players.isEmpty()) {
            MINING_PLAYER.remove();
        }
    }

    public static boolean shouldSuppressFortune(ItemStack stack) {
        if (AnvilEnchantmentConfig.smelterFortune || stack == null || stack.isEmpty()) {
            return false;
        }
        Stack<ServerPlayer> players = MINING_PLAYER.get();
        if (players.isEmpty()) {
            return false;
        }
        ServerPlayer player = players.peek();
        if (player == null || !ItemStack.isSameItemSameTags(stack, player.getMainHandItem())) {
            return false;
        }
        return hasSmelter(stack);
    }

    private static boolean hasSmelter(ItemStack stack) {
        return EnchantmentInit.getLevel(EnchantmentInit.SMELTER, stack) > 0;
    }

    private static SmeltResult process(Level level, ItemStack stack, int depth) {
        if (stack == null || stack.isEmpty()) {
            return SmeltResult.unchanged(stack);
        }

        SmeltResult direct = processSingle(level, stack);
        if (direct.changed()) {
            return direct;
        }

        if (!AnvilEnchantmentConfig.smelterSmeltsChests || depth >= MAX_CONTAINER_DEPTH) {
            return SmeltResult.unchanged(stack);
        }

        CompoundTag originalTag = stack.getTag();
        if (originalTag == null || !originalTag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            return SmeltResult.unchanged(stack);
        }

        CompoundTag originalBlockEntityTag = originalTag.getCompound("BlockEntityTag");
        if (!originalBlockEntityTag.contains("Items", Tag.TAG_LIST)) {
            return SmeltResult.unchanged(stack);
        }

        ItemStack resultStack = stack.copy();
        CompoundTag resultTag = resultStack.getTag();
        if (resultTag == null) {
            return SmeltResult.unchanged(stack);
        }

        CompoundTag resultBlockEntityTag = resultTag.getCompound("BlockEntityTag");
        ListTag items = resultBlockEntityTag.getList("Items", Tag.TAG_COMPOUND);
        boolean changed = false;
        double experience = 0.0D;

        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            byte slot = itemTag.getByte("Slot");
            ItemStack contained = ItemStack.of(itemTag);
            SmeltResult containedResult = process(level, contained, depth + 1);
            if (!containedResult.changed()) {
                continue;
            }

            CompoundTag saved = new CompoundTag();
            containedResult.stack().save(saved);
            saved.putByte("Slot", slot);
            items.set(i, saved);
            changed = true;
            experience += containedResult.experience();
        }

        return changed ? new SmeltResult(resultStack, experience, true) : SmeltResult.unchanged(stack);
    }

    private static SmeltResult processSingle(Level level, ItemStack stack) {
        SimpleContainer container = new SimpleContainer(stack);
        var recipeOptional = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level);
        if (recipeOptional.isEmpty()) {
            return SmeltResult.unchanged(stack);
        }

        var recipe = recipeOptional.get();
        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
        if (result.isEmpty()) {
            return SmeltResult.unchanged(stack);
        }

        int multiplier = Math.max(1, AnvilEnchantmentConfig.getSmelterMultiplier(stack));
        long outputCount = (long) stack.getCount() * result.getCount() * multiplier;
        result.setCount((int) Math.min(Integer.MAX_VALUE, outputCount));

        double experience = recipe.getExperience()
                * stack.getCount()
                * Math.max(0.0D, AnvilEnchantmentConfig.smelterExpMultiplier);
        return new SmeltResult(result, experience, true);
    }

    private static void awardExperience(Level level, Vec3 position, double experience) {
        if (!(level instanceof ServerLevel serverLevel) || experience <= 0.0D) {
            return;
        }

        int amount = Mth.floor(experience);
        double fraction = experience - amount;
        if (fraction > 0.0D && serverLevel.random.nextDouble() < fraction) {
            amount++;
        }
        if (amount > 0) {
            ExperienceOrb.award(serverLevel, position, amount);
        }
    }

    private record SmeltResult(ItemStack stack, double experience, boolean changed) {
        private static SmeltResult unchanged(ItemStack stack) {
            return new SmeltResult(stack == null ? ItemStack.EMPTY : stack, 0.0D, false);
        }
    }
}
