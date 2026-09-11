package dev.xyat.enchantworks.anvil.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dev.xyat.enchantworks.EnchantWorks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AnvilEnchantmentConfig {
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("kineticcore");
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("anvilenchantments.toml");
    private static CommentedFileConfig configData;

    // --- 铁砧设置 ---
    public static boolean removeAnvilLimit = true;
    public static boolean anvilLimitWhitelistMode = false;
    public static List<String> anvilLimitList = new ArrayList<>(List.of("enigmaticaddons:totem_of_malice"));

    // --- 经验平滑 ---
    public static boolean enableTaxFreeLevels = true;
    public static int taxFreeLevelBase = 30;
    public static boolean enableCheapRenaming = true;

    // --- 附魔设置 ---
    public static boolean enableBetterChanneling = true;
    public static List<String> whitelist = new ArrayList<>();
    public static List<String> blacklist = new ArrayList<>();
    public static List<String> disabledEnchantments = new ArrayList<>();

    // --- 独立附魔开关 ---
    public static boolean enableSmelter = true, enableLeech = true,
            enableSixthSense = true, enableOmniTool = true, enableEnlightenment = true;

    // --- 详细数值设定 ---
    public static double smelterExpMultiplier = 1.0;
    public static boolean smelterFortune = true, smelterCooksMobs = true, smelterSmeltsChests = true;

    // 熔炼产出倍增配置
    public static List<String> smelterMultiplierList = new ArrayList<>(List.of("2x #forge:ores", "2x #forge:raw_materials"));
    public static double leechTriggerChance = 0.5, leechLifestealRatio = 0.1, leechStealChance = 0.25;
    public static boolean leechEffectBlacklistMode = true;
    public static List<String> leechEffectList = new ArrayList<>(List.of("minecraft:slow_falling"));
    public static final Set<ResourceLocation> LEECH_EFFECT_CACHE = new HashSet<>();
    public static int sixthSenseTickRate = 10;
    public static double sixthSenseRange = 32.0;
    public static double enlightenmentExpMult = 0.125, omniToolBaseSpeedMultiplier = 3.0, omniToolEnchantChance = 0.10;
    public static List<String> omniToolForceDropBlocks = new ArrayList<>();

    // --- 第六感高亮颜色 ---
    public static int sixthSenseColorPlayer = 0xFFFFFF;
    public static int sixthSenseColorFriendly = 0x00FF00;
    public static int sixthSenseColorNeutral = 0xFFFF00;
    public static int sixthSenseColorWater = 0x00FFFF;
    public static int sixthSenseColorMonster = 0xFF0000;
    public static int sixthSenseColorCustom = 0xFF00FF;

    // --- 第六感自定义名单 ---
    public static List<String> sixthSenseCustomMobs = new ArrayList<>(List.of("minecraft:warden", "minecraft:wither", "minecraft:ender_dragon"));
    private static final Set<ResourceLocation> SIXTH_SENSE_CUSTOM_CACHE = new HashSet<>();

    private static final Set<String> ANVIL_ID_CACHE = new HashSet<>();
    private static final List<TargetMatcher> ANVIL_PATTERN_CACHE = new ArrayList<>();
    private static final List<ParsedRule> WHITELIST_CACHE = new ArrayList<>();
    private static final List<ParsedRule> BLACKLIST_CACHE = new ArrayList<>();
    private static final Set<String> DISABLED_ENCHANTMENTS_CACHE = new HashSet<>();
    private static final Set<String> MOD_ENCHANTMENT_PATHS = new LinkedHashSet<>(Arrays.asList("smelter", "leech", "sixth_sense", "enlightenment", "omni_tool"));
    private static final List<SmelterRule> SMELTER_MULTIPLIER_CACHE = new ArrayList<>();
    private static final List<BlockTargetMatcher> OMNI_TOOL_FORCE_DROP_BLOCK_CACHE = new ArrayList<>();

    public static void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            Config old = Config.inMemory();
            if (Files.exists(CONFIG_PATH)) {
                try (FileConfig oldFile = FileConfig.of(CONFIG_PATH)) { oldFile.load(); old.putAll(oldFile); }
            }
            configData = CommentedFileConfig.builder(CONFIG_PATH).sync().preserveInsertionOrder().writingMode(WritingMode.REPLACE).build();
            setup(old);
            configData.save();
            readValues();
        } catch (Exception e) {
            discardBrokenConfig();
            EnchantWorks.LOGGER.error("Failed to load Anvil & Enchantment config", e);
        }
    }

    private static void discardBrokenConfig() {
        if (configData == null) return;
        try {
            configData.close();
        } catch (Throwable ignored) {
        }
        configData = null;
    }

    private static void setup(Config old) {
        configData.setComment("anvil", """
                 铁砧逻辑全面解封：移除等级上限(40+)、移除累积惩罚(PWP)与双模名单过滤。
                 Anvil Unleashed: Removes Level 40 cap, Prior Work Penalty, and supports Dual-Mode filtering.""");
        define(old, "anvil.removeLimit", true, """
                 是否移除铁砧限制。开启后将解锁‘过于昂贵’限制，并彻底删除累积惩罚(维修次数不再增加成本)。
                 Whether to remove anvil limits. Unlocks 'Too Expensive' cap and deletes Prior Work Penalty.""");
        define(old, "anvil.whitelistMode", false, """
                 名单模式设定。true: 白名单(仅名单内的物品移除限制)；false: 黑名单(名单内的物品保留原版限制，其余解除)。
                 List Mode. true: Whitelist (only listed items unlocked); false: Blacklist (listed items kept vanilla, others unlocked).""");
        define(old, "anvil.itemList", Arrays.asList("enigmaticaddons:totem_of_malice"), """
                 【铁砧过滤名单 - 小白语法教程】
                 请在英文双引号 " " 内填写你需要过滤的物品，如果有多个物品，请用英文逗号 , 隔开。
                 ！警告：不要使用中文的标点符号，必须全是英文半角符号！
                 支持以下三种填写格式：
                 1. 指定单个物品 (直接写物品ID)："minecraft:iron_sword" (仅代表原版铁剑)
                 2. 指定一类物品 (在前面加 # 号，这叫标签)："#minecraft:swords" (代表游戏里所有的剑)
                 3. 指定整个模组 (在前面加 @ 号)："@create" (代表“机械动力”这个模组里的所有物品)
                 示例大全：
                 ["minecraft:stick", "#minecraft:logs", "@farmersdelight"]
                 (以上代码表示：指定的物品包含了“木棍”、“所有的原木”、以及“农夫乐事模组里的所有物品”)
                 =========================================================
                 [Anvil Filter List - Beginner's Guide]
                 Please write the items you want to filter inside double quotes " ". If there are multiple items, separate them with a comma , .
                 ! WARNING: Only use English (half-width) punctuation !
                 Supported formats:
                 1. Single Item (Direct ID): "minecraft:iron_sword" (Vanilla iron sword only)
                 2. Item Group (Prefix with #, called a Tag): "#minecraft:swords" (All swords in the game)
                 3. Entire Mod (Prefix with @): "@create" (All items from the 'Create' mod)
                 Examples:
                 ["minecraft:stick", "#minecraft:logs", "@farmersdelight"]
                 (Meaning: includes "stick", "all logs", and "all items from Farmer's Delight")""");

        configData.setComment("tax_free", """
                 免税附魔机制：将‘等级扣除’重构为‘XP点数扣除’。防止高等级时因经验曲线导致巨大的额外损失。
                 Tax-Free Levels: Flattens costs by consuming XP points instead of levels to prevent high-level inflation.""");
        define(old, "tax_free.enable", true, """
                 是否启用经验平滑。启用后，铁砧和附魔台将根据你的当前等级扣除对应的固定XP值，而不是直接抹除等级。
                 Enable XP smoothing. Enchanting/Anvil will consume equivalent XP points instead of raw levels.""");
        define(old, "tax_free.levelBase", 30, """
                 经验计算基数。设为30则：无论你100级还是1000级，扣除3级时的XP开销永远等同于从30级降至27级的数值。
                 XP calculation base. If 30: the XP cost for 3 levels will always equal the points needed from level 30 to 27.""");
        define(old, "tax_free.cheapRenaming", true, """
                 廉价重命名。开启后，在铁砧中进行纯重命名操作固定只消耗 1 级经验。
                 Cheap renaming. Renaming an item in the anvil will always cost exactly 1 level.""");

        String tutorial = """
                 【核弹级附魔拦截 - 小白语法教程】
                 底层过滤，就算其他模组(比如JEI)显示能附魔，在这里被禁止后，铁砧出来的成品也会强制变成空。
                 语法格式："目标物品 ; 附魔ID"
                 (注意：中间必须有一个英文分号 ; 隔开)
                 [左边：目标物品] 的写法和上面的铁砧名单完全一样：
                 - "minecraft:stick" (单个物品)
                 - "#minecraft:pickaxes" (加#号表示标签)
                 - "@create" (加@号表示模组)
                 [右边：附魔ID] 的写法：
                 - "minecraft:mending" (指定单个附魔)
                 - "minecraft:sharpness, minecraft:smite" (指定多个附魔，用逗号隔开)
                 - "*" 或 "ALL" (表示不限制，匹配所有的附魔)
                 示例大全：
                 ["#minecraft:pickaxes ; minecraft:mending"] (所有的镐子 ; 经验修补)
                 ["minecraft:stick ; *"] (木棍 ; 允许/禁止打上任何附魔)
                 ["@farmersdelight ; minecraft:unbreaking, minecraft:mending"] (农夫乐事所有物品 ; 耐久和经验修补)
                 =========================================================
                 [Nuclear Interception - Beginner's Guide]
                 Hardcore filtering. Even if JEI shows it's enchantable, blocked combinations will force the anvil output to be empty.
                 Syntax: "Target Item ; Enchantment ID"
                 (Note: You MUST use an English semicolon ; to separate the item and the enchantment)
                 [Left side: Target Item] formats:
                 - "minecraft:stick" (Single item)
                 - "#minecraft:pickaxes" (Tag prefix #)
                 - "@create" (Mod prefix @)
                 [Right side: Enchantment ID] formats:
                 - "minecraft:mending" (Specific enchantment)
                 - "minecraft:sharpness, minecraft:smite" (Multiple enchantments separated by commas)
                 - "*" or "ALL" (Matches ANY enchantment)
                 Examples:
                 ["#minecraft:pickaxes ; minecraft:mending"] (All pickaxes ; Mending)
                 ["minecraft:stick ; *"] (Stick ; ANY enchantment)
                 ["@farmersdelight ; minecraft:unbreaking, minecraft:mending"] (Farmer's Delight items ; Unbreaking & Mending)""";
        configData.setComment("enchantment", tutorial);

        define(old, "enchantment.enableBetterChanneling", true, """
                 强化引雷。开启后，引雷附魔可以在任何天气(包括晴天)下召唤闪电，等级2时效果增强。
                 Better Channeling. Allows lightning strikes in any weather. Level 2 enhances the effect.""");
        define(old, "enchantment.whitelist", Arrays.asList("#minecraft:axes; minecraft:sharpness, minecraft:smite, minecraft:bane_of_arthropods, minecraft:mob_looting, minecraft:knockback, minecraft:fire_aspect", "minecraft:stick; minecraft:knockback, minecraft:sharpness"), """
                 附魔白名单：强制允许这些 [物品 ; 附魔] 组合，无视原版的兼容性检查。
                 Enchantment Whitelist: Forcibly allow these [Item ; Enchant] combinations, bypassing vanilla checks.""");
        define(old, "enchantment.blacklist", Arrays.asList("@create ; minecraft:mending"), """
                 附魔黑名单：严厉禁止这些组合。匹配时铁砧将无法产出成品。
                 Enchantment Blacklist: Strictly forbid these combinations. Anvil output will be cleared.""");
        define(old, "enchantment.disabledEnchantments", new ArrayList<>(), """
                 全局禁用附魔名单：直接填写附魔ID。填入这里的附魔将被彻底禁用，无论什么物品都无法在铁砧或附魔台中打上它。
                 Global Disabled Enchantments: Direct Enchantment IDs. Listed enchantments are completely disabled globally.
                 示例/Examples: ["minecraft:mending", "minecraft:unbreaking"]""");
        define(old, "enchantment.enableSmelter", true, """
                 是否启用熔炼附魔。关闭后将不会注册该附魔，游戏内等同不存在（需重启）。
                 Enable Smelter enchantment. If disabled, it will not be registered on next restart.""");
        define(old, "enchantment.enableLeech", true, """
                 是否启用汲取附魔。关闭后将不会注册该附魔，游戏内等同不存在（需重启）。
                 Enable Leech enchantment. If disabled, it will not be registered on next restart.""");
        define(old, "enchantment.enableSixthSense", true, """
                 是否启用第六感附魔。关闭后将不会注册该附魔，游戏内等同不存在（需重启）。
                 Enable Sixth Sense enchantment. If disabled, it will not be registered on next restart.""");
        define(old, "enchantment.enableOmniTool", true, """
                 是否启用万能工具附魔。关闭后将不会注册该附魔，游戏内等同不存在（需重启）。
                 Enable Omni-Tool enchantment. If disabled, it will not be registered on next restart.""");
        define(old, "enchantment.enableEnlightenment", true, """
                 是否启用启蒙附魔。关闭后将不会注册该附魔，游戏内等同不存在（需重启）。
                 Enable Enlightenment enchantment. If disabled, it will not be registered on next restart.""");

        configData.setComment("values", """
                 附魔效果的具体数值平衡设定。修改后通常无需重启，但建议重新打开容器界面。
                 Balancing values for custom enchantments. Changes usually apply without restart.""");
        define(old, "values.smelterSmeltsChests", true, """
                 熔炼增强：是否允许熔炼附魔直接处理容器（如潜影盒、箱子）内的矿石。
                 Smelter Plus: Whether Smelter can process items inside containers like Shulker Boxes.""");
        define(old, "values.smelterFortune", true, """
                 熔炼附魔是否受时运影响。
                 Does Smelter work with Fortune.""");
        define(old, "values.smelterCooksMobs", true, """
                 熔炼增强：是否直接烤熟生物掉落物。
                 Smelter Plus: Whether Smelter directly cooks mob drops.""");
        define(old, "values.smelterExpMultiplier", 1.0, """
                 熔炼经验倍率：熔炼产出时额外获得的经验系数 (1.0 = 原版)。
                 Smelter XP multiplier (1.0 = Vanilla default).""");

        define(old, "values.smelterMultiplierList", Arrays.asList("2x #forge:ores", "2x #forge:raw_materials"), """
                 【熔炼倍增名单 - 小白语法教程】
                 自定义特定物品被“熔炼”附魔处理时，掉落物翻多少倍。
                 语法格式："[倍率]x [目标物品]"
                 (数字和x连在一起写，空一格写目标物品的ID/标签/模组)
                 * 注意：如果不写前面的倍率，系统默认算作翻一倍 (即 2x)。
                 [目标物品] 的写法：
                 - "minecraft:raw_iron" (单个物品：粗铁)
                 - "#forge:ores" (加#号表示标签：所有矿石)
                 - "@create" (加@号表示模组：机械动力所有物品)
                 示例大全：
                 "2x #forge:ores" (所有矿石产出变为2倍)
                 "3x minecraft:raw_iron" (粗铁产出变为3倍)
                 "4x @farmersdelight" (农夫乐事所有物品变为4倍)
                 "@create" (省略了倍率，默认把机械动力所有物品变为2倍)
                 =========================================================
                 [Smelter Multiplier List - Beginner's Guide]
                 Customize the output multiplier for specific items processed by the Smelter enchantment.
                 Syntax: "[Multiplier]x [Target Item]"
                 (Write the number and 'x', add a space, then the target item/tag/mod)
                 * Note: If you omit the multiplier prefix, it defaults to doubling (2x).
                 [Target Item] formats:
                 - "minecraft:raw_iron" (Single item: raw iron)
                 - "#forge:ores" (Tag prefix #: all blocks with the 'ores' tag)
                 - "@create" (Mod prefix @: all Create mod items)
                 Examples:
                 "2x #forge:ores" (Smelting any ore yields 2x output)
                 "3x minecraft:raw_iron" (Smelting raw iron yields 3x output)
                 "4x @farmersdelight" (Farmer's Delight items yield 4x output)
                 "@create" (Multiplier omitted, defaults to 2x output for Create mod items)""");

        define(old, "values.leechEffectList", Arrays.asList("minecraft:slow_falling"), """
                 汲取白/黑名单：配合'leechEffectBlacklistMode'使用，过滤可被攻击偷取的Buff ID。
                 Leech Effect List: Filter list for stealable status effects using IDs.""");
        define(old, "values.leechEffectBlacklistMode", true, """
                 汲取名单模式，true为黑名单。
                 Leech list mode, true for blacklist.""");
        define(old, "values.leechTriggerChance", 0.5, """
                 汲取概率：攻击时触发吸血或偷取Buff的几率 (0.0 = 0%, 1.0 = 100%)。
                 Leech trigger chance (0.0 to 1.0).""");
        define(old, "values.leechLifestealRatio", 0.1, """
                 吸血比例：基于造成的伤害值按比例恢复生命值。
                 Lifesteal ratio based on damage dealt.""");
        define(old, "values.leechStealChance", 0.25, """
                 偷取Buff的概率。
                 Chance to steal Buffs.""");
        define(old, "values.sixthSenseRange", 32.0, """
                 第六感探测半径：潜行状态下探测并高亮生物的格数范围。
                 Sixth Sense range: Radius in blocks to highlight entities while crouching.""");
        define(old, "values.sixthSenseTickRate", 10, """
                 第六感探测频率。
                 Sixth Sense tick detection rate.""");
        define(old, "values.omniToolBaseSpeedMultiplier", 3.0, """
                 万能工具基础速度倍率：基础速度补偿 = 当前工具原始挖掘速度 × 该倍率。默认 3.0 表示当前工具速度的 3 倍。
                 实际速度还会按方块硬度额外补偿，硬度越高越快，但不会让低硬度方块变慢。
                 Omni-Tool base speed multiplier: base speed boost = current tool mining speed × this multiplier. Default 3.0 means triple speed.
                 The final speed also scales up with block hardness, but never slows low-hardness blocks.""");
        define(old, "values.omniToolEnchantChance", 0.10, """
                 万能工具附魔台出现概率：仅第三档显示等级达到 30 时生效，工具和普通书都可以出现。0.10 = 10%，0.05 = 5%，1.0 = 100%。
                 Omni-Tool enchanting chance: only works for the 3rd enchanting-table slot when displayed level is at least 30. 0.10 = 10%, 0.05 = 5%, 1.0 = 100%.""");
        define(old, "values.omniToolForceDropBlocks", new ArrayList<>(), """
                 【万能工具强制掉落方块名单 - 小白语法教程】
                 这里是“额外例外名单”，只有填入名单的方块，才会在万能工具挖掘时强制掉落自身。
                 不在名单内的方块仍严格遵守原版/数据包战利品表规则：刷怪笼这类本来不会掉落的方块，不会被万能工具挖下来。
                 支持三种格式：
                 1. 指定单个方块："minecraft:spawner"
                 2. 指定一类方块标签："#minecraft:mineable/pickaxe"
                 3. 指定整个模组方块："@minecraft"
                 示例： ["minecraft:spawner", "#forge:storage_blocks"]
                 注意：名单方块被万能工具强制掉落时，会压制该次破坏产生的方块经验，避免刷怪笼同时掉方块又掉经验。
                 =========================================================
                 [Omni-Tool Force Drop Block List]
                 Extra exception list. Only blocks listed here will forcibly drop themselves when mined with Omni-Tool.
                 Blocks not listed still obey vanilla/datapack loot tables. Spawners and other no-drop blocks will not drop unless listed here.
                 Formats: block id, #block_tag, or @modid. Forced drops suppress block XP for that break.""");
        define(old, "values.enlightenmentExpMult", 0.125, """
                 启蒙附魔经验倍率。
                 Enlightenment XP multiplier.""");
        define(old, "values.sixthSenseColorPlayer", 0xFFFFFF, """
                 第六感颜色：玩家 (默认白色)。
                 Sixth Sense Color: Player (Default White).""");
        define(old, "values.sixthSenseColorFriendly", 0x00FF00, """
                 第六感颜色：友好生物 (默认绿色)。
                 Sixth Sense Color: Friendly (Default Green).""");
        define(old, "values.sixthSenseColorNeutral", 0xFFFF00, """
                 第六感颜色：中立生物 (默认黄色)。
                 Sixth Sense Color: Neutral (Default Yellow).""");
        define(old, "values.sixthSenseColorWater", 0x00FFFF, """
                 第六感颜色：水生生物 (默认淡蓝色)。
                 Sixth Sense Color: Water (Default Light Blue).""");
        define(old, "values.sixthSenseColorMonster", 0xFF0000, """
                 第六感颜色：怪物 (默认红色)。
                 Sixth Sense Color: Monster (Default Red).""");
        define(old, "values.sixthSenseColorCustom", 0xFF00FF, """
                 第六感颜色：自定义生物类 (默认亮紫色)。
                 Sixth Sense Color: Custom Mobs (Default Magenta).""");
        define(old, "values.sixthSenseCustomMobs", Arrays.asList("minecraft:warden", "minecraft:wither", "minecraft:ender_dragon"), """
                 自定义高亮生物名单 (直接填写生物的ID，如："minecraft:warden")。
                 List of mobs for custom color highlighting (using entity IDs).""");
    }

    public static synchronized void saveServerSettings() {
        if (configData == null) {
            throw new IllegalStateException("Enchantment server config is not loaded");
        }
        configData.set("anvil.removeLimit", removeAnvilLimit);
        configData.set("anvil.whitelistMode", anvilLimitWhitelistMode);
        configData.set("anvil.itemList", anvilLimitList);
        configData.set("tax_free.enable", enableTaxFreeLevels);
        configData.set("tax_free.levelBase", taxFreeLevelBase);
        configData.set("tax_free.cheapRenaming", enableCheapRenaming);
        configData.set("enchantment.enableBetterChanneling", enableBetterChanneling);
        configData.set("enchantment.whitelist", whitelist);
        configData.set("enchantment.blacklist", blacklist);
        configData.set("enchantment.disabledEnchantments", disabledEnchantments);
        configData.set("enchantment.enableSmelter", enableSmelter);
        configData.set("enchantment.enableLeech", enableLeech);
        configData.set("enchantment.enableSixthSense", enableSixthSense);
        configData.set("enchantment.enableOmniTool", enableOmniTool);
        configData.set("enchantment.enableEnlightenment", enableEnlightenment);
        configData.set("values.smelterSmeltsChests", smelterSmeltsChests);
        configData.set("values.smelterFortune", smelterFortune);
        configData.set("values.smelterCooksMobs", smelterCooksMobs);
        configData.set("values.smelterExpMultiplier", smelterExpMultiplier);
        configData.set("values.smelterMultiplierList", smelterMultiplierList);
        configData.set("values.leechEffectList", leechEffectList);
        configData.set("values.leechEffectBlacklistMode", leechEffectBlacklistMode);
        configData.set("values.leechTriggerChance", leechTriggerChance);
        configData.set("values.leechLifestealRatio", leechLifestealRatio);
        configData.set("values.leechStealChance", leechStealChance);
        configData.set("values.omniToolBaseSpeedMultiplier", omniToolBaseSpeedMultiplier);
        configData.set("values.omniToolEnchantChance", omniToolEnchantChance);
        configData.set("values.omniToolForceDropBlocks", omniToolForceDropBlocks);
        configData.set("values.enlightenmentExpMult", enlightenmentExpMult);
        configData.save();
        refreshCaches();
    }

    public static synchronized void saveClientSettings() {
        if (configData == null) return;
        configData.set("values.sixthSenseRange", sixthSenseRange);
        configData.set("values.sixthSenseTickRate", sixthSenseTickRate);
        configData.set("values.sixthSenseColorPlayer", sixthSenseColorPlayer);
        configData.set("values.sixthSenseColorFriendly", sixthSenseColorFriendly);
        configData.set("values.sixthSenseColorNeutral", sixthSenseColorNeutral);
        configData.set("values.sixthSenseColorWater", sixthSenseColorWater);
        configData.set("values.sixthSenseColorMonster", sixthSenseColorMonster);
        configData.set("values.sixthSenseColorCustom", sixthSenseColorCustom);
        configData.set("values.sixthSenseCustomMobs", sixthSenseCustomMobs);
        configData.save();
        refreshCaches();
    }

    private static void readValues() {
        removeAnvilLimit = configData.getOrElse("anvil.removeLimit", true);
        anvilLimitWhitelistMode = configData.getOrElse("anvil.whitelistMode", false);
        anvilLimitList = configData.getOrElse("anvil.itemList", new ArrayList<>());
        enableTaxFreeLevels = configData.getOrElse("tax_free.enable", true);
        taxFreeLevelBase = configData.getOrElse("tax_free.levelBase", 30);
        enableCheapRenaming = configData.getOrElse("tax_free.cheapRenaming", true);
        enableBetterChanneling = configData.getOrElse("enchantment.enableBetterChanneling", true);
        whitelist = configData.getOrElse("enchantment.whitelist", new ArrayList<>());
        blacklist = configData.getOrElse("enchantment.blacklist", new ArrayList<>());
        disabledEnchantments = configData.getOrElse("enchantment.disabledEnchantments", new ArrayList<>());
        enableSmelter = configData.getOrElse("enchantment.enableSmelter", true);
        enableLeech = configData.getOrElse("enchantment.enableLeech", true);
        enableSixthSense = configData.getOrElse("enchantment.enableSixthSense", true);
        enableOmniTool = configData.getOrElse("enchantment.enableOmniTool", true);
        enableEnlightenment = configData.getOrElse("enchantment.enableEnlightenment", true);
        smelterSmeltsChests = configData.getOrElse("values.smelterSmeltsChests", true);
        smelterFortune = configData.getOrElse("values.smelterFortune", true);
        smelterCooksMobs = configData.getOrElse("values.smelterCooksMobs", true);
        smelterExpMultiplier = configData.getOrElse("values.smelterExpMultiplier", 1.0);
        smelterMultiplierList = configData.getOrElse("values.smelterMultiplierList", new ArrayList<>(List.of("2x #forge:ores", "2x #forge:raw_materials")));
        leechEffectList = configData.getOrElse("values.leechEffectList", new ArrayList<>());
        leechEffectBlacklistMode = configData.getOrElse("values.leechEffectBlacklistMode", true);
        leechTriggerChance = clampChance(configData.getOrElse("values.leechTriggerChance", 0.5), 0.5);
        leechLifestealRatio = configData.getOrElse("values.leechLifestealRatio", 0.1);
        leechStealChance = clampChance(configData.getOrElse("values.leechStealChance", 0.25), 0.25);
        sixthSenseRange = configData.getOrElse("values.sixthSenseRange", 32.0);
        sixthSenseTickRate = configData.getOrElse("values.sixthSenseTickRate", 10);
        omniToolBaseSpeedMultiplier = configData.getOrElse("values.omniToolBaseSpeedMultiplier", 3.0);
        omniToolEnchantChance = clampChance(configData.getOrElse("values.omniToolEnchantChance", 0.10), 0.10);
        omniToolForceDropBlocks = configData.getOrElse("values.omniToolForceDropBlocks", new ArrayList<>());
        enlightenmentExpMult = configData.getOrElse("values.enlightenmentExpMult", 0.125);
        sixthSenseColorPlayer = configData.getOrElse("values.sixthSenseColorPlayer", 0xFFFFFF);
        sixthSenseColorFriendly = configData.getOrElse("values.sixthSenseColorFriendly", 0x00FF00);
        sixthSenseColorNeutral = configData.getOrElse("values.sixthSenseColorNeutral", 0xFFFF00);
        sixthSenseColorWater = configData.getOrElse("values.sixthSenseColorWater", 0x00FFFF);
        sixthSenseColorMonster = configData.getOrElse("values.sixthSenseColorMonster", 0xFF0000);
        sixthSenseColorCustom = configData.getOrElse("values.sixthSenseColorCustom", 0xFF00FF);
        sixthSenseCustomMobs = configData.getOrElse("values.sixthSenseCustomMobs", new ArrayList<>(List.of("minecraft:warden", "minecraft:wither", "minecraft:ender_dragon")));

        refreshCaches();
    }

    private static void refreshCaches() {
        ANVIL_ID_CACHE.clear();
        ANVIL_PATTERN_CACHE.clear();
        for (String s : anvilLimitList) {
            String t = s.trim();
            if (t.startsWith("#")) ANVIL_PATTERN_CACHE.add(new TargetMatcher(TargetType.TAG, t.substring(1)));
            else if (t.startsWith("@")) ANVIL_PATTERN_CACHE.add(new TargetMatcher(TargetType.MOD, t.substring(1)));
            else ANVIL_ID_CACHE.add(t);
        }

        WHITELIST_CACHE.clear();
        for (String r : whitelist) WHITELIST_CACHE.add(new ParsedRule(r));

        BLACKLIST_CACHE.clear();
        for (String r : blacklist) BLACKLIST_CACHE.add(new ParsedRule(r));

        DISABLED_ENCHANTMENTS_CACHE.clear();
        for (String e : disabledEnchantments) {
            String id = normalizeAnyEnchantmentId(e);
            if (id != null) DISABLED_ENCHANTMENTS_CACHE.add(id);
        }

        LEECH_EFFECT_CACHE.clear();
        for (String id : leechEffectList) {
            ResourceLocation loc = ResourceLocation.tryParse(id.trim()); if (loc != null) LEECH_EFFECT_CACHE.add(loc);
        }

        SIXTH_SENSE_CUSTOM_CACHE.clear();
        for (String idStr : sixthSenseCustomMobs) {
            ResourceLocation loc = ResourceLocation.tryParse(idStr.trim()); if (loc != null) SIXTH_SENSE_CUSTOM_CACHE.add(loc);
        }

        SMELTER_MULTIPLIER_CACHE.clear();
        for (String entry : smelterMultiplierList) {
            if (entry != null && !entry.trim().isEmpty()) {
                SMELTER_MULTIPLIER_CACHE.add(new SmelterRule(entry));
            }
        }

        OMNI_TOOL_FORCE_DROP_BLOCK_CACHE.clear();
        for (String entry : omniToolForceDropBlocks) {
            if (entry == null) continue;
            String t = entry.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("#")) OMNI_TOOL_FORCE_DROP_BLOCK_CACHE.add(new BlockTargetMatcher(TargetType.TAG, t.substring(1)));
            else if (t.startsWith("@")) OMNI_TOOL_FORCE_DROP_BLOCK_CACHE.add(new BlockTargetMatcher(TargetType.MOD, t.substring(1)));
            else OMNI_TOOL_FORCE_DROP_BLOCK_CACHE.add(new BlockTargetMatcher(TargetType.ITEM, t));
        }
    }

    private static double clampChance(double value, double fallback) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return fallback;
        if (value < 0.0) return 0.0;
        return Math.min(value, 1.0);
    }

    private static void define(Config old, String path, Object def, String comment) {
        configData.set(path, old.getOrElse(path, def));
        if (comment != null) configData.setComment(path, "\n " + comment.trim());
    }

    public static boolean isEnchantmentDisabled(Enchantment e) {
        if (e == null) return false;
        ResourceLocation rl = ForgeRegistries.ENCHANTMENTS.getKey(e);
        if (rl == null) return false;
        String id = rl.toString();

        if (DISABLED_ENCHANTMENTS_CACHE.contains(id)) return true;
        if (id.equals(EnchantWorks.MODID + ":smelter")) return !enableSmelter;
        if (id.equals(EnchantWorks.MODID + ":leech")) return !enableLeech;
        if (id.equals(EnchantWorks.MODID + ":sixth_sense")) return !enableSixthSense;
        if (id.equals(EnchantWorks.MODID + ":omni_tool")) return !enableOmniTool;
        if (id.equals(EnchantWorks.MODID + ":enlightenment")) return !enableEnlightenment;
        return false;
    }

    public static Set<String> getRegistryDisabledModEnchantments() {
        Set<String> result = new HashSet<>();
        if (!Files.exists(CONFIG_PATH)) return result;

        try (FileConfig early = FileConfig.of(CONFIG_PATH)) {
            early.load();
            addDisabledToggle(result, early, "enchantment.enableSmelter", "smelter");
            addDisabledToggle(result, early, "enchantment.enableLeech", "leech");
            addDisabledToggle(result, early, "enchantment.enableSixthSense", "sixth_sense");
            addDisabledToggle(result, early, "enchantment.enableOmniTool", "omni_tool");
            addDisabledToggle(result, early, "enchantment.enableEnlightenment", "enlightenment");
            collectEarlyDisabledModEnchantments(result, early.getOrElse("enchantment.disabledEnchantments", new ArrayList<>()));
        } catch (Exception e) {
            EnchantWorks.LOGGER.error("Failed to read early enchantment registry config", e);
        }
        return result;
    }

    private static void addDisabledToggle(Set<String> result, FileConfig config, String configPath, String enchantmentPath) {
        if (!config.getOrElse(configPath, true)) {
            result.add(EnchantWorks.MODID + ":" + enchantmentPath);
        }
    }

    public static boolean hasExplicitAllowRuleForStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || WHITELIST_CACHE.isEmpty()) return false;
        ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemRL == null) return false;
        for (ParsedRule rule : WHITELIST_CACHE) {
            if (rule.matchesTargetOnly(stack, itemRL.toString(), itemRL.getNamespace())) return true;
        }
        return false;
    }


    private static void collectEarlyDisabledModEnchantments(Set<String> result, List<String> entries) {
        if (entries == null) return;
        for (String entry : entries) {
            String id = normalizeModEnchantmentId(entry);
            if (id != null) result.add(id);
        }
    }

    private static String normalizeAnyEnchantmentId(String rawId) {
        if (rawId == null) return null;
        String trimmed = rawId.trim();
        if (trimmed.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(trimmed);
        return id == null ? null : id.toString();
    }

    private static String normalizeModEnchantmentId(String rawId) {
        if (rawId == null) return null;
        String trimmed = rawId.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.indexOf(':') < 0) {
            trimmed = EnchantWorks.MODID + ":" + trimmed;
        }
        ResourceLocation id = ResourceLocation.tryParse(trimmed);
        if (id == null || !EnchantWorks.MODID.equals(id.getNamespace()) || !MOD_ENCHANTMENT_PATHS.contains(id.getPath())) {
            return null;
        }
        return id.toString();
    }

    public static int getSmelterMultiplier(ItemStack stack) {
        if (stack.isEmpty()) return 1;
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null) return 1;
        String id = rl.toString();
        String modId = rl.getNamespace();

        for (SmelterRule rule : SMELTER_MULTIPLIER_CACHE) {
            if (rule.matches(stack, id, modId)) {
                return rule.multiplier;
            }
        }
        return 1;
    }

    public static boolean shouldRemoveAnvilLimit(ItemStack stack) {
        if (!removeAnvilLimit) return false;
        if (stack.isEmpty()) return true;
        ResourceLocation idRL = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (idRL == null) return !anvilLimitWhitelistMode;
        String id = idRL.toString();
        boolean match = ANVIL_ID_CACHE.contains(id);

        if (!match && !ANVIL_PATTERN_CACHE.isEmpty()) {
            for (TargetMatcher matcher : ANVIL_PATTERN_CACHE) {
                if (matcher.type() == TargetType.MOD) {
                    if (idRL.getNamespace().equals(matcher.value())) { match = true; break; }
                } else if (matcher.type() == TargetType.TAG) {
                    if (matcher.getTagKey() != null && stack.is(matcher.getTagKey())) { match = true; break; }
                }
            }
        }
        return anvilLimitWhitelistMode == match;
    }

    public static boolean isExplicitlyAllowed(ItemStack stack, Enchantment e) {
        return checkParsedRules(stack, e, WHITELIST_CACHE);
    }

    public static boolean isExplicitlyDenied(ItemStack stack, Enchantment e) {
        return checkParsedRules(stack, e, BLACKLIST_CACHE);
    }

    public static boolean isCustomSixthSenseMob(ResourceLocation id) {
        return SIXTH_SENSE_CUSTOM_CACHE.contains(id);
    }

    public static boolean canLeechEffect(MobEffect effect) {
        if (effect == null || !effect.isBeneficial()) return false;
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (id == null) return false;
        boolean listed = LEECH_EFFECT_CACHE.contains(id);
        return leechEffectBlacklistMode ? !listed : listed;
    }

    private static boolean checkParsedRules(ItemStack stack, Enchantment enchantment, List<ParsedRule> cache) {
        if (stack.isEmpty() || cache.isEmpty() || enchantment == null) return false;
        ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(stack.getItem());
        ResourceLocation enchantRL = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (itemRL == null || enchantRL == null) return false;
        for (ParsedRule rule : cache) {
            if (rule.matches(stack, itemRL.toString(), itemRL.getNamespace(), enchantRL.toString())) return true;
        }
        return false;
    }

    private static class SmelterRule {
        int multiplier = 1;
        TargetMatcher matcher = new TargetMatcher(TargetType.ITEM, "minecraft:air");

        SmelterRule(String entry) {
            try {
                String trimmed = entry.trim();
                int mult = 2;
                String targetStr = trimmed;

                java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)x\\s*(.+)$").matcher(trimmed);
                if (m.matches()) {
                    mult = Integer.parseInt(m.group(1));
                    targetStr = m.group(2).trim();
                }

                TargetType tType = TargetType.ITEM;
                String val = targetStr;
                if (targetStr.startsWith("#")) {
                    tType = TargetType.TAG;
                    val = targetStr.substring(1);
                } else if (targetStr.startsWith("@")) {
                    tType = TargetType.MOD;
                    val = targetStr.substring(1);
                }

                this.multiplier = mult;
                this.matcher = new TargetMatcher(tType, val);
            } catch (Exception e) {
                EnchantWorks.LOGGER.error("EnchantWorks | Config Syntax Error! Failed to parse Smelter Rule: [{}]. 熔炼倍增规则语法错误，请检查！", entry, e);
            }
        }

        boolean matches(ItemStack stack, String itemId, String modId) {
            switch (matcher.type()) {
                case ITEM: return itemId.equals(matcher.value());
                case MOD: return modId.equals(matcher.value());
                case TAG:
                    if (matcher.getTagKey() != null) {
                        return stack.is(matcher.getTagKey());
                    }
                    break;
            }
            return false;
        }
    }

    private static class ParsedRule {
        final List<TargetMatcher> targetMatchers = new ArrayList<>();
        final Set<String> enchantmentIds = new HashSet<>();
        boolean matchAll = false;

        ParsedRule(String entry) {
            try {
                String[] parts = entry.split(";");
                for (String target : parts[0].split(",")) {
                    String ct = target.trim();
                    if (ct.startsWith("#")) targetMatchers.add(new TargetMatcher(TargetType.TAG, ct.substring(1)));
                    else if (ct.startsWith("@")) targetMatchers.add(new TargetMatcher(TargetType.MOD, ct.substring(1)));
                    else targetMatchers.add(new TargetMatcher(TargetType.ITEM, ct));
                }
                if (parts.length < 2) this.matchAll = true;
                else {
                    String encPart = parts[1].trim();
                    if (encPart.isEmpty() || encPart.equals("*") || encPart.equalsIgnoreCase("ALL")) this.matchAll = true;
                    else for (String enc : encPart.split(",")) enchantmentIds.add(enc.trim());
                }
            } catch (Exception e) {
                EnchantWorks.LOGGER.error("EnchantWorks | Config Syntax Error! Failed to parse Enchantment Rule: [{}]. 附魔拦截规则语法错误，请检查！", entry, e);
            }
        }

        boolean matches(ItemStack stack, String itemId, String modId, String enchantId) {
            if (!matchAll && !enchantmentIds.contains(enchantId)) return false;
            return matchesTargetOnly(stack, itemId, modId);
        }

        boolean matchesTargetOnly(ItemStack stack, String itemId, String modId) {
            for (TargetMatcher matcher : targetMatchers) {
                switch (matcher.type()) {
                    case ITEM: if (itemId.equals(matcher.value())) return true; break;
                    case MOD: if (modId.equals(matcher.value())) return true; break;
                    case TAG:
                        if (matcher.getTagKey() != null && stack.is(matcher.getTagKey())) return true;
                        break;
                }
            }
            return false;
        }
    }


    public static boolean shouldOmniToolForceDrop(BlockState state) {
        if (state == null || OMNI_TOOL_FORCE_DROP_BLOCK_CACHE.isEmpty()) return false;
        ResourceLocation idRL = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (idRL == null) return false;
        String id = idRL.toString();
        String modId = idRL.getNamespace();

        for (BlockTargetMatcher matcher : OMNI_TOOL_FORCE_DROP_BLOCK_CACHE) {
            switch (matcher.type()) {
                case ITEM:
                    if (id.equals(matcher.value())) return true;
                    break;
                case MOD:
                    if (modId.equals(matcher.value())) return true;
                    break;
                case TAG:
                    if (matcher.getTagKey() != null && state.is(matcher.getTagKey())) return true;
                    break;
            }
        }
        return false;
    }

    public static boolean isBetterChannelingEnabled() {
        return enableBetterChanneling;
    }

    private enum TargetType { ITEM, MOD, TAG }

    private static class TargetMatcher {
        private final TargetType type;
        private final String value;
        private final TagKey<net.minecraft.world.item.Item> cachedTagKey;

        TargetMatcher(TargetType type, String value) {
            this.type = type;
            this.value = value;
            if (type == TargetType.TAG) {
                ResourceLocation loc = ResourceLocation.tryParse(value);
                this.cachedTagKey = loc != null ? ItemTags.create(loc) : null;
            } else {
                this.cachedTagKey = null;
            }
        }

        public TargetType type() { return type; }
        public String value() { return value; }
        public TagKey<net.minecraft.world.item.Item> getTagKey() { return cachedTagKey; }
    }

    private static class BlockTargetMatcher {
        private final TargetType type;
        private final String value;
        private final TagKey<Block> cachedTagKey;

        BlockTargetMatcher(TargetType type, String value) {
            this.type = type;
            this.value = value;
            if (type == TargetType.TAG) {
                ResourceLocation loc = ResourceLocation.tryParse(value);
                this.cachedTagKey = loc != null ? BlockTags.create(loc) : null;
            } else {
                this.cachedTagKey = null;
            }
        }

        public TargetType type() { return type; }
        public String value() { return value; }
        public TagKey<Block> getTagKey() { return cachedTagKey; }
    }
}
