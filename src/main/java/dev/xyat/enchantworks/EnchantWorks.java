package dev.xyat.enchantworks;

import com.mojang.logging.LogUtils;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfigGui;
import dev.xyat.enchantworks.enchantment.init.EnchantmentInit;
import dev.xyat.enchantworks.enchantment.init.RecipeInit;
import dev.xyat.enchantworks.enchantment.init.EnchantmentTabRegistry;
import dev.xyat.enchantworks.enchantment.client.EnchantmentTooltipHandler;
import dev.xyat.enchantworks.enchantment.enlightenment.EnlightenmentEvent;
import dev.xyat.enchantworks.enchantment.leech.LeechEvent;
import dev.xyat.enchantworks.enchantment.omni_tool.OmniToolEvent;
import dev.xyat.enchantworks.enchantment.sixth_sense.SixthSenseClientEvent;
import dev.xyat.enchantworks.enchantment.smelter.SmelterEventHandler;
import dev.xyat.kineticcore.api.config.server.KTServerConfigApi;
import dev.xyat.kineticcore.api.config.server.KTServerConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;

import java.util.ArrayList;

@Mod(EnchantWorks.MODID)
public final class EnchantWorks {
    public static final String MODID = "enchantworks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnchantWorks() {
        AnvilEnchantmentConfig.load();
        KTServerConfigApi.register(KTServerConfigSpec.builder("enchantworks:server")
                .booleanValue("remove_anvil_limit", () -> AnvilEnchantmentConfig.removeAnvilLimit, value -> AnvilEnchantmentConfig.removeAnvilLimit = value)
                .booleanValue("anvil_whitelist_mode", () -> AnvilEnchantmentConfig.anvilLimitWhitelistMode, value -> AnvilEnchantmentConfig.anvilLimitWhitelistMode = value)
                .stringList("anvil_item_list", () -> new ArrayList<>(AnvilEnchantmentConfig.anvilLimitList), value -> AnvilEnchantmentConfig.anvilLimitList = new ArrayList<>(value))
                .booleanValue("tax_free_enable", () -> AnvilEnchantmentConfig.enableTaxFreeLevels, value -> AnvilEnchantmentConfig.enableTaxFreeLevels = value)
                .intValue("tax_free_level_base", () -> AnvilEnchantmentConfig.taxFreeLevelBase, value -> AnvilEnchantmentConfig.taxFreeLevelBase = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .booleanValue("cheap_renaming", () -> AnvilEnchantmentConfig.enableCheapRenaming, value -> AnvilEnchantmentConfig.enableCheapRenaming = value)
                .booleanValue("better_channeling", () -> AnvilEnchantmentConfig.enableBetterChanneling, value -> AnvilEnchantmentConfig.enableBetterChanneling = value)
                .stringList("enchantment_whitelist", () -> new ArrayList<>(AnvilEnchantmentConfig.whitelist), value -> AnvilEnchantmentConfig.whitelist = new ArrayList<>(value))
                .stringList("enchantment_blacklist", () -> new ArrayList<>(AnvilEnchantmentConfig.blacklist), value -> AnvilEnchantmentConfig.blacklist = new ArrayList<>(value))
                .stringList("disabled_enchantments", () -> new ArrayList<>(AnvilEnchantmentConfig.disabledEnchantments), value -> AnvilEnchantmentConfig.disabledEnchantments = new ArrayList<>(value))
                .booleanValue("enable_smelter", () -> AnvilEnchantmentConfig.enableSmelter, value -> AnvilEnchantmentConfig.enableSmelter = value)
                .booleanValue("enable_leech", () -> AnvilEnchantmentConfig.enableLeech, value -> AnvilEnchantmentConfig.enableLeech = value)
                .booleanValue("enable_sixth_sense", () -> AnvilEnchantmentConfig.enableSixthSense, value -> AnvilEnchantmentConfig.enableSixthSense = value)
                .booleanValue("enable_omni_tool", () -> AnvilEnchantmentConfig.enableOmniTool, value -> AnvilEnchantmentConfig.enableOmniTool = value)
                .booleanValue("enable_enlightenment", () -> AnvilEnchantmentConfig.enableEnlightenment, value -> AnvilEnchantmentConfig.enableEnlightenment = value)
                .booleanValue("smelter_chests", () -> AnvilEnchantmentConfig.smelterSmeltsChests, value -> AnvilEnchantmentConfig.smelterSmeltsChests = value)
                .booleanValue("smelter_fortune", () -> AnvilEnchantmentConfig.smelterFortune, value -> AnvilEnchantmentConfig.smelterFortune = value)
                .booleanValue("smelter_mobs", () -> AnvilEnchantmentConfig.smelterCooksMobs, value -> AnvilEnchantmentConfig.smelterCooksMobs = value)
                .doubleValue("smelter_exp_multiplier", () -> AnvilEnchantmentConfig.smelterExpMultiplier, value -> AnvilEnchantmentConfig.smelterExpMultiplier = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .stringList("smelter_multipliers", () -> new ArrayList<>(AnvilEnchantmentConfig.smelterMultiplierList), value -> AnvilEnchantmentConfig.smelterMultiplierList = new ArrayList<>(value))
                .booleanValue("leech_blacklist_mode", () -> AnvilEnchantmentConfig.leechEffectBlacklistMode, value -> AnvilEnchantmentConfig.leechEffectBlacklistMode = value)
                .stringList("leech_effects", () -> new ArrayList<>(AnvilEnchantmentConfig.leechEffectList), value -> AnvilEnchantmentConfig.leechEffectList = new ArrayList<>(value))
                .doubleValue("leech_trigger_chance", () -> AnvilEnchantmentConfig.leechTriggerChance, value -> AnvilEnchantmentConfig.leechTriggerChance = value, 0.0D, 1.0D)
                .doubleValue("leech_lifesteal_ratio", () -> AnvilEnchantmentConfig.leechLifestealRatio, value -> AnvilEnchantmentConfig.leechLifestealRatio = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .doubleValue("leech_steal_chance", () -> AnvilEnchantmentConfig.leechStealChance, value -> AnvilEnchantmentConfig.leechStealChance = value, 0.0D, 1.0D)
                .doubleValue("omni_tool_speed", () -> AnvilEnchantmentConfig.omniToolBaseSpeedMultiplier, value -> AnvilEnchantmentConfig.omniToolBaseSpeedMultiplier = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .doubleValue("omni_tool_chance", () -> AnvilEnchantmentConfig.omniToolEnchantChance, value -> AnvilEnchantmentConfig.omniToolEnchantChance = value, 0.0D, 1.0D)
                .stringList("omni_force_drop", () -> new ArrayList<>(AnvilEnchantmentConfig.omniToolForceDropBlocks), value -> AnvilEnchantmentConfig.omniToolForceDropBlocks = new ArrayList<>(value))
                .doubleValue("enlightenment_multiplier", () -> AnvilEnchantmentConfig.enlightenmentExpMult, value -> AnvilEnchantmentConfig.enlightenmentExpMult = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .onSave(AnvilEnchantmentConfig::saveServerSettings)
                .build());
        EnchantmentInit.register();
        RecipeInit.register();
        EnchantmentTabRegistry.register();
        EnlightenmentEvent.register();
        OmniToolEvent.register();
        LeechEvent.register();
        SmelterEventHandler.register();
        KineticPlatform.runOnClient(() -> () -> {
            AnvilEnchantmentConfigGui.load();
            SixthSenseClientEvent.register();
            EnchantmentTooltipHandler.register();
        });
    }
}
