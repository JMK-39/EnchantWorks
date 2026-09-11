package dev.xyat.enchantworks.anvil.config;

import dev.xyat.kineticcore.config.client.KTConfigApi;
import dev.xyat.kineticcore.config.client.KTConfigPage;
import dev.xyat.kineticcore.config.client.KTConfigScope;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class AnvilEnchantmentConfigGui {
    public static final String SERVER_PAGE_ID = "enchantworks:server";
    public static final String CLIENT_PAGE_ID = "enchantworks:sixth_sense";

    private static final List<String> DEFAULT_ANVIL_ITEMS = List.of("enigmaticaddons:totem_of_malice");
    private static final List<String> DEFAULT_WHITELIST = List.of(
            "#minecraft:axes; minecraft:sharpness, minecraft:smite, minecraft:bane_of_arthropods, minecraft:mob_looting, minecraft:knockback, minecraft:fire_aspect",
            "minecraft:stick; minecraft:knockback, minecraft:sharpness"
    );
    private static final List<String> DEFAULT_BLACKLIST = List.of("@create ; minecraft:mending");
    private static final List<String> DEFAULT_SMELTER_MULTIPLIERS = List.of("2x #forge:ores", "2x #forge:raw_materials");
    private static final List<String> DEFAULT_LEECH_EFFECTS = List.of("minecraft:slow_falling");
    private static final List<String> DEFAULT_CUSTOM_MOBS = List.of(
            "minecraft:warden", "minecraft:wither", "minecraft:ender_dragon"
    );

    private AnvilEnchantmentConfigGui() {
    }

    public static void load() {
        KTConfigApi.register(buildServerPage());
        KTConfigApi.register(buildClientPage());
    }

    private static KTConfigPage buildServerPage() {
        return KTConfigPage.builder(SERVER_PAGE_ID, Component.translatable("cfg.enchantworks.server.title"))
                .scope(KTConfigScope.SERVER_AUTHORITATIVE)
                .serverManaged()
                .applyTiming(KTConfigPage.ApplyTiming.MIXED)
                .applyNotice(Component.translatable("cfg.enchantworks.server.apply_notice"))
                .pageDescription(Component.translatable("cfg.enchantworks.server.description"))

                .section(Component.translatable("cfg.enchantworks.section.anvil"))
                .booleanValue("remove_anvil_limit", Component.translatable("cfg.enchantworks.anvil.remove_limit"),
                        () -> AnvilEnchantmentConfig.removeAnvilLimit,
                        value -> AnvilEnchantmentConfig.removeAnvilLimit = value, true,
                        Component.translatable("cfg.enchantworks.anvil.remove_limit.tooltip"))
                .booleanValue("anvil_whitelist_mode", Component.translatable("cfg.enchantworks.anvil.whitelist_mode"),
                        () -> AnvilEnchantmentConfig.anvilLimitWhitelistMode,
                        value -> AnvilEnchantmentConfig.anvilLimitWhitelistMode = value, false,
                        Component.translatable("cfg.enchantworks.anvil.whitelist_mode.tooltip"))
                .description(Component.translatable("cfg.enchantworks.anvil.item_list.syntax"))
                .itemRuleList("anvil_item_list", Component.translatable("cfg.enchantworks.anvil.item_list"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.anvilLimitList),
                        value -> AnvilEnchantmentConfig.anvilLimitList = new ArrayList<>(value), DEFAULT_ANVIL_ITEMS,
                        Component.translatable("cfg.enchantworks.anvil.item_list.tooltip"))

                .section(Component.translatable("cfg.enchantworks.section.tax_free"))
                .booleanValue("tax_free_enable", Component.translatable("cfg.enchantworks.tax_free.enable"),
                        () -> AnvilEnchantmentConfig.enableTaxFreeLevels,
                        value -> AnvilEnchantmentConfig.enableTaxFreeLevels = value, true,
                        Component.translatable("cfg.enchantworks.tax_free.enable.tooltip"))
                .intValue("tax_free_level_base", Component.translatable("cfg.enchantworks.tax_free.level_base"),
                        () -> AnvilEnchantmentConfig.taxFreeLevelBase,
                        value -> AnvilEnchantmentConfig.taxFreeLevelBase = value, 30,
                        Component.translatable("cfg.enchantworks.tax_free.level_base.tooltip"))
                .booleanValue("cheap_renaming", Component.translatable("cfg.enchantworks.tax_free.cheap_renaming"),
                        () -> AnvilEnchantmentConfig.enableCheapRenaming,
                        value -> AnvilEnchantmentConfig.enableCheapRenaming = value, true,
                        Component.translatable("cfg.enchantworks.tax_free.cheap_renaming.tooltip"))

                .section(Component.translatable("cfg.enchantworks.section.enchantment"))
                .booleanValue("better_channeling", Component.translatable("cfg.enchantworks.enchantment.better_channeling"),
                        () -> AnvilEnchantmentConfig.enableBetterChanneling,
                        value -> AnvilEnchantmentConfig.enableBetterChanneling = value, true,
                        Component.translatable("cfg.enchantworks.enchantment.better_channeling.tooltip"))
                .description(Component.translatable("cfg.enchantworks.enchantment.rules.syntax"))
                .stringList("enchantment_whitelist", Component.translatable("cfg.enchantworks.enchantment.whitelist"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.whitelist),
                        value -> AnvilEnchantmentConfig.whitelist = new ArrayList<>(value), DEFAULT_WHITELIST,
                        Component.translatable("cfg.enchantworks.enchantment.whitelist.tooltip"))
                .stringList("enchantment_blacklist", Component.translatable("cfg.enchantworks.enchantment.blacklist"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.blacklist),
                        value -> AnvilEnchantmentConfig.blacklist = new ArrayList<>(value), DEFAULT_BLACKLIST,
                        Component.translatable("cfg.enchantworks.enchantment.blacklist.tooltip"))
                .stringList("disabled_enchantments", Component.translatable("cfg.enchantworks.enchantment.disabled"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.disabledEnchantments),
                        value -> AnvilEnchantmentConfig.disabledEnchantments = new ArrayList<>(value), List.of(),
                        Component.translatable("cfg.enchantworks.enchantment.disabled.tooltip"))
                .booleanValue("enable_smelter", Component.translatable("cfg.enchantworks.enchantment.enable_smelter"),
                        () -> AnvilEnchantmentConfig.enableSmelter,
                        value -> AnvilEnchantmentConfig.enableSmelter = value, true,
                        Component.translatable("cfg.enchantworks.enchantment.registry_toggle.tooltip"))
                .booleanValue("enable_leech", Component.translatable("cfg.enchantworks.enchantment.enable_leech"),
                        () -> AnvilEnchantmentConfig.enableLeech,
                        value -> AnvilEnchantmentConfig.enableLeech = value, true,
                        Component.translatable("cfg.enchantworks.enchantment.registry_toggle.tooltip"))
                .booleanValue("enable_sixth_sense", Component.translatable("cfg.enchantworks.enchantment.enable_sixth_sense"),
                        () -> AnvilEnchantmentConfig.enableSixthSense,
                        value -> AnvilEnchantmentConfig.enableSixthSense = value, true,
                        Component.translatable("cfg.enchantworks.enchantment.registry_toggle.tooltip"))
                .booleanValue("enable_omni_tool", Component.translatable("cfg.enchantworks.enchantment.enable_omni_tool"),
                        () -> AnvilEnchantmentConfig.enableOmniTool,
                        value -> AnvilEnchantmentConfig.enableOmniTool = value, true,
                        Component.translatable("cfg.enchantworks.enchantment.registry_toggle.tooltip"))
                .booleanValue("enable_enlightenment", Component.translatable("cfg.enchantworks.enchantment.enable_enlightenment"),
                        () -> AnvilEnchantmentConfig.enableEnlightenment,
                        value -> AnvilEnchantmentConfig.enableEnlightenment = value, true,
                        Component.translatable("cfg.enchantworks.enchantment.registry_toggle.tooltip"))

                .section(Component.translatable("cfg.enchantworks.section.smelter"))
                .booleanValue("smelter_chests", Component.translatable("cfg.enchantworks.values.smelter_chests"),
                        () -> AnvilEnchantmentConfig.smelterSmeltsChests,
                        value -> AnvilEnchantmentConfig.smelterSmeltsChests = value, true,
                        Component.translatable("cfg.enchantworks.values.smelter_chests.tooltip"))
                .booleanValue("smelter_fortune", Component.translatable("cfg.enchantworks.values.smelter_fortune"),
                        () -> AnvilEnchantmentConfig.smelterFortune,
                        value -> AnvilEnchantmentConfig.smelterFortune = value, true,
                        Component.translatable("cfg.enchantworks.values.smelter_fortune.tooltip"))
                .booleanValue("smelter_mobs", Component.translatable("cfg.enchantworks.values.smelter_mobs"),
                        () -> AnvilEnchantmentConfig.smelterCooksMobs,
                        value -> AnvilEnchantmentConfig.smelterCooksMobs = value, true,
                        Component.translatable("cfg.enchantworks.values.smelter_mobs.tooltip"))
                .doubleValue("smelter_exp_multiplier", Component.translatable("cfg.enchantworks.values.smelter_exp"),
                        () -> AnvilEnchantmentConfig.smelterExpMultiplier,
                        value -> AnvilEnchantmentConfig.smelterExpMultiplier = value, 1.0,
                        Component.translatable("cfg.enchantworks.values.smelter_exp.tooltip"))
                .description(Component.translatable("cfg.enchantworks.values.smelter_multipliers.syntax"))
                .stringList("smelter_multipliers", Component.translatable("cfg.enchantworks.values.smelter_multipliers"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.smelterMultiplierList),
                        value -> AnvilEnchantmentConfig.smelterMultiplierList = new ArrayList<>(value), DEFAULT_SMELTER_MULTIPLIERS,
                        Component.translatable("cfg.enchantworks.values.smelter_multipliers.tooltip"))

                .section(Component.translatable("cfg.enchantworks.section.leech"))
                .booleanValue("leech_blacklist_mode", Component.translatable("cfg.enchantworks.values.leech_blacklist_mode"),
                        () -> AnvilEnchantmentConfig.leechEffectBlacklistMode,
                        value -> AnvilEnchantmentConfig.leechEffectBlacklistMode = value, true,
                        Component.translatable("cfg.enchantworks.values.leech_blacklist_mode.tooltip"))
                .stringList("leech_effects", Component.translatable("cfg.enchantworks.values.leech_effects"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.leechEffectList),
                        value -> AnvilEnchantmentConfig.leechEffectList = new ArrayList<>(value), DEFAULT_LEECH_EFFECTS,
                        Component.translatable("cfg.enchantworks.values.leech_effects.tooltip"))
                .doubleValue("leech_trigger_chance", Component.translatable("cfg.enchantworks.values.leech_trigger"),
                        () -> AnvilEnchantmentConfig.leechTriggerChance,
                        value -> AnvilEnchantmentConfig.leechTriggerChance = value, 0.5, 0.0, 1.0,
                        Component.translatable("cfg.enchantworks.values.leech_trigger.tooltip"))
                .doubleValue("leech_lifesteal_ratio", Component.translatable("cfg.enchantworks.values.leech_lifesteal"),
                        () -> AnvilEnchantmentConfig.leechLifestealRatio,
                        value -> AnvilEnchantmentConfig.leechLifestealRatio = value, 0.1,
                        Component.translatable("cfg.enchantworks.values.leech_lifesteal.tooltip"))
                .doubleValue("leech_steal_chance", Component.translatable("cfg.enchantworks.values.leech_steal"),
                        () -> AnvilEnchantmentConfig.leechStealChance,
                        value -> AnvilEnchantmentConfig.leechStealChance = value, 0.25, 0.0, 1.0,
                        Component.translatable("cfg.enchantworks.values.leech_steal.tooltip"))

                .section(Component.translatable("cfg.enchantworks.section.other_values"))
                .doubleValue("omni_tool_speed", Component.translatable("cfg.enchantworks.values.omni_speed"),
                        () -> AnvilEnchantmentConfig.omniToolBaseSpeedMultiplier,
                        value -> AnvilEnchantmentConfig.omniToolBaseSpeedMultiplier = value, 3.0,
                        Component.translatable("cfg.enchantworks.values.omni_speed.tooltip"))
                .doubleValue("omni_tool_chance", Component.translatable("cfg.enchantworks.values.omni_chance"),
                        () -> AnvilEnchantmentConfig.omniToolEnchantChance,
                        value -> AnvilEnchantmentConfig.omniToolEnchantChance = value, 0.10, 0.0, 1.0,
                        Component.translatable("cfg.enchantworks.values.omni_chance.tooltip"))
                .description(Component.translatable("cfg.enchantworks.values.force_drop.syntax"))
                .stringList("omni_force_drop", Component.translatable("cfg.enchantworks.values.force_drop"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.omniToolForceDropBlocks),
                        value -> AnvilEnchantmentConfig.omniToolForceDropBlocks = new ArrayList<>(value), List.of(),
                        Component.translatable("cfg.enchantworks.values.force_drop.tooltip"))
                .doubleValue("enlightenment_multiplier", Component.translatable("cfg.enchantworks.values.enlightenment"),
                        () -> AnvilEnchantmentConfig.enlightenmentExpMult,
                        value -> AnvilEnchantmentConfig.enlightenmentExpMult = value, 0.125,
                        Component.translatable("cfg.enchantworks.values.enlightenment.tooltip"))
                .build();
    }

    private static KTConfigPage buildClientPage() {
        return KTConfigPage.builder(CLIENT_PAGE_ID, Component.translatable("cfg.enchantworks.sixth_sense.title"))
                .scope(KTConfigScope.CLIENT_LOCAL)
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .pageDescription(Component.translatable("cfg.enchantworks.sixth_sense.description"))
                .doubleValue("sixth_sense_range", Component.translatable("cfg.enchantworks.sixth_sense.range"),
                        () -> AnvilEnchantmentConfig.sixthSenseRange,
                        value -> AnvilEnchantmentConfig.sixthSenseRange = value, 32.0, 0.0, Double.MAX_VALUE,
                        Component.translatable("cfg.enchantworks.sixth_sense.range.tooltip"))
                .intValue("sixth_sense_tick_rate", Component.translatable("cfg.enchantworks.sixth_sense.tick_rate"),
                        () -> AnvilEnchantmentConfig.sixthSenseTickRate,
                        value -> AnvilEnchantmentConfig.sixthSenseTickRate = value, 10, 1, Integer.MAX_VALUE,
                        Component.translatable("cfg.enchantworks.sixth_sense.tick_rate.tooltip"))
                .entityList("sixth_sense_custom_mobs", Component.translatable("cfg.enchantworks.sixth_sense.custom_mobs"),
                        () -> new ArrayList<>(AnvilEnchantmentConfig.sixthSenseCustomMobs),
                        value -> AnvilEnchantmentConfig.sixthSenseCustomMobs = new ArrayList<>(value), DEFAULT_CUSTOM_MOBS,
                        Component.translatable("cfg.enchantworks.sixth_sense.custom_mobs.tooltip"))
                .section(Component.translatable("cfg.enchantworks.sixth_sense.colors"))
                .color("sixth_sense_color_player", Component.translatable("cfg.enchantworks.sixth_sense.color_player"),
                        () -> AnvilEnchantmentConfig.sixthSenseColorPlayer,
                        value -> AnvilEnchantmentConfig.sixthSenseColorPlayer = value, 0xFFFFFF,
                        Component.translatable("cfg.enchantworks.sixth_sense.color.tooltip"))
                .color("sixth_sense_color_friendly", Component.translatable("cfg.enchantworks.sixth_sense.color_friendly"),
                        () -> AnvilEnchantmentConfig.sixthSenseColorFriendly,
                        value -> AnvilEnchantmentConfig.sixthSenseColorFriendly = value, 0x00FF00,
                        Component.translatable("cfg.enchantworks.sixth_sense.color.tooltip"))
                .color("sixth_sense_color_neutral", Component.translatable("cfg.enchantworks.sixth_sense.color_neutral"),
                        () -> AnvilEnchantmentConfig.sixthSenseColorNeutral,
                        value -> AnvilEnchantmentConfig.sixthSenseColorNeutral = value, 0xFFFF00,
                        Component.translatable("cfg.enchantworks.sixth_sense.color.tooltip"))
                .color("sixth_sense_color_water", Component.translatable("cfg.enchantworks.sixth_sense.color_water"),
                        () -> AnvilEnchantmentConfig.sixthSenseColorWater,
                        value -> AnvilEnchantmentConfig.sixthSenseColorWater = value, 0x00FFFF,
                        Component.translatable("cfg.enchantworks.sixth_sense.color.tooltip"))
                .color("sixth_sense_color_monster", Component.translatable("cfg.enchantworks.sixth_sense.color_monster"),
                        () -> AnvilEnchantmentConfig.sixthSenseColorMonster,
                        value -> AnvilEnchantmentConfig.sixthSenseColorMonster = value, 0xFF0000,
                        Component.translatable("cfg.enchantworks.sixth_sense.color.tooltip"))
                .color("sixth_sense_color_custom", Component.translatable("cfg.enchantworks.sixth_sense.color_custom"),
                        () -> AnvilEnchantmentConfig.sixthSenseColorCustom,
                        value -> AnvilEnchantmentConfig.sixthSenseColorCustom = value, 0xFF00FF,
                        Component.translatable("cfg.enchantworks.sixth_sense.color.tooltip"))
                .onSave(AnvilEnchantmentConfig::saveClientSettings)
                .build();
    }

    public static Screen createServerScreen(Screen parent) {
        return KTConfigApi.createScreen(parent, SERVER_PAGE_ID);
    }

    public static Screen createClientScreen(Screen parent) {
        return KTConfigApi.createScreen(parent, CLIENT_PAGE_ID);
    }
}
