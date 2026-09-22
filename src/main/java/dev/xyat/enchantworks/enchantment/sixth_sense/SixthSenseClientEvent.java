package dev.xyat.enchantworks.enchantment.sixth_sense;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

public final class SixthSenseClientEvent {
    private static final Set<Integer> CACHED_ENTITY_IDS = new HashSet<>();
    private static int tickCounter = 0;
    private static boolean initialized;

    private SixthSenseClientEvent() {
    }

    public static synchronized void register() {
        if (initialized) return;
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, SixthSenseClientEvent::onClientTick);
        initialized = true;
    }

    private static void onClientTick() {
        if (!AnvilEnchantmentConfig.enableSixthSense) return;

        LocalPlayer player = KineticClientRuntime.localPlayer();
        ClientLevel level = KineticClientRuntime.currentLevel();

        if (player == null || level == null || !player.isCrouching() || EnchantmentInit.getEntityLevel(EnchantmentInit.SIXTH_SENSE, player) <= 0) {
            if (!CACHED_ENTITY_IDS.isEmpty()) CACHED_ENTITY_IDS.clear();
            return;
        }

        tickCounter++;
        if (tickCounter >= AnvilEnchantmentConfig.sixthSenseTickRate) {
            tickCounter = 0;
            CACHED_ENTITY_IDS.clear();
            AABB box = player.getBoundingBox().inflate(AnvilEnchantmentConfig.sixthSenseRange);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                CACHED_ENTITY_IDS.add(entity.getId());
            }
        }
    }

    public static boolean isCached(LivingEntity entity) {
        return !CACHED_ENTITY_IDS.isEmpty() && CACHED_ENTITY_IDS.contains(entity.getId());
    }

    public static int getColorForEntity(LivingEntity entity) {
        ResourceLocation id = KineticRegistries.entityTypes().id(entity.getType());
        if (id != null && AnvilEnchantmentConfig.isCustomSixthSenseMob(id)) {
            return AnvilEnchantmentConfig.sixthSenseColorCustom;
        }

        if (entity instanceof Player) return AnvilEnchantmentConfig.sixthSenseColorPlayer;
        if (entity instanceof NeutralMob) return AnvilEnchantmentConfig.sixthSenseColorNeutral;

        MobCategory category = entity.getType().getCategory();
        if (category == MobCategory.MONSTER) return AnvilEnchantmentConfig.sixthSenseColorMonster;
        if (category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE || category == MobCategory.AXOLOTLS) {
            return AnvilEnchantmentConfig.sixthSenseColorWater;
        }

        return AnvilEnchantmentConfig.sixthSenseColorFriendly;
    }
}
