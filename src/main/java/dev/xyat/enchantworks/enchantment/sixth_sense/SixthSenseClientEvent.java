package dev.xyat.enchantworks.enchantment.sixth_sense;

import dev.xyat.enchantworks.EnchantWorks;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = EnchantWorks.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SixthSenseClientEvent {
    private static final Set<Integer> CACHED_ENTITY_IDS = new HashSet<>();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !AnvilEnchantmentConfig.enableSixthSense) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || !player.isCrouching() || EnchantmentInit.getEntityLevel(EnchantmentInit.SIXTH_SENSE, player) <= 0) {
            if (!CACHED_ENTITY_IDS.isEmpty()) CACHED_ENTITY_IDS.clear();
            return;
        }

        tickCounter++;
        if (tickCounter >= AnvilEnchantmentConfig.sixthSenseTickRate) {
            tickCounter = 0;
            CACHED_ENTITY_IDS.clear();
            AABB box = player.getBoundingBox().inflate(AnvilEnchantmentConfig.sixthSenseRange);
            for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                CACHED_ENTITY_IDS.add(entity.getId());
            }
        }
    }

    public static boolean isCached(LivingEntity entity) {
        return !CACHED_ENTITY_IDS.isEmpty() && CACHED_ENTITY_IDS.contains(entity.getId());
    }

    public static int getColorForEntity(LivingEntity entity) {
        // 1. 优先检查自定义名单 (亮紫色)
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id != null && AnvilEnchantmentConfig.isCustomSixthSenseMob(id)) {
            return AnvilEnchantmentConfig.sixthSenseColorCustom;
        }

        // 2. 玩家检查
        if (entity instanceof Player) return AnvilEnchantmentConfig.sixthSenseColorPlayer;

        // 3. 中立生物检查
        if (entity instanceof NeutralMob) return AnvilEnchantmentConfig.sixthSenseColorNeutral;

        // 4. 类别检查
        MobCategory category = entity.getType().getCategory();
        if (category == MobCategory.MONSTER) return AnvilEnchantmentConfig.sixthSenseColorMonster;
        if (category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE || category == MobCategory.AXOLOTLS) {
            return AnvilEnchantmentConfig.sixthSenseColorWater;
        }

        // 5. 友好生物兜底
        return AnvilEnchantmentConfig.sixthSenseColorFriendly;
    }
}
