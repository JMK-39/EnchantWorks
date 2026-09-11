# Enchant Works

[English](#english) | [简体中文](#简体中文)

## English

### Overview

An enchantment and anvil mechanics mod that expands equipment progression with configurable anvil rules, enchantment acceptance rules, vanilla-enchantment extensions, and several gameplay enchantments.

The project is designed around in-game administration. Where a feature changes shared gameplay data or server rules, the server remains authoritative; client-only presentation features stay local to the client. Configuration screens use KineticCore's UI and configuration infrastructure.

### Key Features

- Configurable anvil cost limits, repair-cost behavior, XP smoothing and low-cost renaming.
- Whitelist, blacklist and global-disable rules for item/enchantment combinations.
- Extended behavior for selected vanilla enchantments and compatible equipment.
- Gameplay enchantments including Smelter, Leech, Sixth Sense, Enlightenment and Omni Tool.
- Server-authoritative rules exposed through KineticCore configuration.

### Requirements and Compatibility

| Type | Dependency |
|---|---|
| Required | Minecraft 1.20.1 |
| Required | Minecraft Forge 47+ |
| Required | KineticCore 26.9.8+ |
| Optional | None |

### Access and Configuration

- Open the KineticCore configuration center with its configured F6 entry and select **Enchant Works**.
- Server-owned settings are saved by the server and synchronized where the feature requires client awareness.
- Client-only presentation settings remain local.
- Individual feature areas document their own data/configuration paths below.
- Search, list selection, item/entity inspection, tooltips and return/navigation controls reuse KineticCore UI components where available.

## Detailed Feature Reference

### Enchantments & Anvils

#### Overview

**Enchant Works** is an anvil, enchantment-rule, and equipment-mechanics mod for Minecraft Forge. It requires KineticCore and uses KineticCore's configuration system for server-authoritative rules and client-local display settings.

Its active feature set consists of anvil rules, enchantment rules, vanilla-enchantment extensions, and five Enchant Works enchantments.

#### Anvil System

##### Anvil Limits

- The vanilla “Too Expensive” level cap can be disabled for matching items.
- Matching outputs have their prior-work repair cost reset to zero.
- The item filter supports whitelist and blacklist modes.
- Targets accept an item ID, `#item_tag`, or `@modid`.
- The default filter contains `enigmaticaddons:totem_of_malice`.

##### XP Smoothing

- Anvil and enchanting-table level costs can be converted to a fixed XP-point cost instead of removing levels directly from the player's current high-level XP curve.
- The default calculation base level is 30.
- If an enchanting-table requirement is higher than the configured base, the displayed requirement participates in the fixed-cost calculation.

##### Cheap Renaming

- A pure anvil rename with only the first input slot occupied costs one level.
- The behavior is server-configurable.

#### Enchantment Rules
##### Whitelist

Whitelist entries force specific item/enchantment combinations to be accepted. Targets support item IDs, `#tags`, and `@modids`; the enchantment portion accepts multiple enchantment IDs.

```text
#minecraft:axes; minecraft:sharpness, minecraft:smite
minecraft:stick; minecraft:knockback, minecraft:sharpness
```

Explicitly allowed items can participate in enchanting even when vanilla would normally reject that combination.

##### Blacklist

- Explicitly denied enchantments are filtered out of enchanting-table results.
- Anvil outputs containing a denied item/enchantment combination are invalidated.
- `*`, `ALL`, or an empty enchantment part matches every enchantment.

##### Global Disable List

- Enchantments can be globally disabled by ID.
- Disabled enchantments are rejected by normal anvil and enchanting-table application paths.
- Disabled enchantments are hidden from normal discoverable/tradeable paths.
- Registry-level disabling of Enchant Works's own enchantments is determined during startup.

#### Vanilla Enchantments
##### Infinity Bow

- A bow with vanilla Infinity can be drawn and fired even when the player has no arrows in inventory.

##### Infinity Bucket

- Buckets can participate in enchanting and can receive vanilla Infinity.
- Bucket enchantability uses an enchantment value of 10.
- An Infinity bucket preserves its original container state after successful bucket use.
- Forge `FluidBucketWrapper` `fill` and both `drain` operations also preserve the Infinity bucket container.

##### Enhanced Channeling

- Channeling II can satisfy the lightning weather condition even when the world is not currently thundering.
- Channeling I keeps vanilla weather behavior.
- When enabled, a Channeling II enchanted book is exposed in the Creative Ingredients tab.

#### Mod Enchantments
The enchantments currently registered by Enchant Works are **Smelter, Leech, Sixth Sense, Enlightenment, and Omni Tool**.

##### Smelter

- Tool enchantment, default maximum level I.
- Incompatible with Silk Touch.
- Smelts eligible block drops through normal smelting recipes.
- Can smelt eligible mob drops.
- Sneaking bypasses Smelter processing.
- Can process items stored in `BlockEntityTag.Items` containers, up to four nested levels.
- Awards experience from the matched smelting recipe with a configurable XP multiplier.
- Fortune interaction can be enabled or suppressed while Smelter is active.
- Output multipliers support item IDs, `#tags`, and `@modids`; the first matching rule wins.
- Default output rules are `2x #forge:ores` and `2x #forge:raw_materials`.

##### Leech

- Weapon enchantment, maximum level III.
- Works on eligible direct attacks, not indirect damage.
- Default trigger chance: 50%.
- Healing is calculated from dealt damage × configured lifesteal ratio × enchantment level; the default ratio is 10% per level.
- A second roll can transfer one eligible beneficial effect from the target to the attacker; default chance: 25%.
- Effect filtering supports whitelist or blacklist mode.

##### Sixth Sense

- Head-armor treasure enchantment, default maximum level I.
- While crouching, scans nearby living entities and highlights them with vanilla model-fitting outline rendering.
- Default range: 32 blocks.
- Default scan interval: 10 ticks.
- Separate outline colors exist for players, friendly mobs, neutral mobs, aquatic mobs, monsters, and custom entities.
- Default custom entities are the Warden, Wither, and Ender Dragon.
- Range, interval, entity list, and all category colors are client-configurable.

##### Enlightenment

- Armor enchantment, maximum level II.
- Increases XP generated when the player kills living entities or breaks blocks that award experience.
- Default additional multiplier: 12.5% per enchantment level.
- Bonus XP is calculated from the source XP amount × level × configured multiplier.
- The source event's XP amount is increased before vanilla creates the experience orbs, so the enchantment does not modify or tag existing XP orbs.

##### Omni Tool

- Tool treasure enchantment, default maximum level I, and not villager-tradeable.
- Applies to digger tools and shears; swords are explicitly excluded.
- Uses the correct-tool behavior of a netherite pickaxe, axe, shovel, hoe, and shears.
- Does not universally bypass loot rules; the target must be harvestable by one of those tool categories unless it is explicitly listed as a forced-drop block.
- Mining speed uses both a configurable current-speed multiplier and block-hardness compensation; default multiplier: 3.0.
- Safe shears-style self-drops are provided for leaves, cobwebs, vines, glow lichen, grass, tall grass, ferns, dead bushes, seagrass, and tall seagrass when the original loot result is empty.
- Forced-drop rules support block IDs, `#block_tags`, and `@modids`.
- Forced self-drops suppress block XP, including dedicated handling for spawners.
- The third enchanting-table slot can roll Omni Tool I when its displayed level is at least 30; default chance: 10%.
- The displayed third-slot result and the actual applied result use the same deterministic roll.
- Books receive an Omni Tool I enchanted-book result when the roll succeeds.
- A built-in unordered crafting recipe creates an Omni Tool I enchanted book from a netherite pickaxe, netherite axe, netherite shovel, shears, and a book.

#### Configuration

Primary configuration file:

```text
config/kineticcore/anvilenchantments.toml
```

Enchant Works registers two KineticCore configuration pages:

- `enchantworks:server` — server-authoritative anvil, XP, enchantment-rule, and balance settings.
- `enchantworks:sixth_sense` — client-local Sixth Sense scanning and outline settings.

The server page covers anvil filtering, XP smoothing, cheap renaming, Enhanced Channeling, enchantment allow/deny/disable rules, registration toggles for all five active mod enchantments, Smelter settings, Leech settings, Omni Tool settings, and the Enlightenment XP multiplier.

The client page covers Sixth Sense range, scan interval, custom entity IDs, and all six outline-color categories.

Normal values and rules refresh their caches when saved. Enchantment registration state is established during startup and therefore requires a full game/server restart to change.

### Feature Reference
#### Config Details
| Item | Description |
|---|---|
| **Anvil & Enchantments (Server)** | Server-owned anvil, enchantment registration, filtering, and balance settings. This page is unavailable while connected to a remote server without secure synchronization. |
| **Remove Anvil Limits** | Remove the Too Expensive cap and prior-work penalty for matching items. |
| **Anvil List Is a Whitelist** | Enabled: only listed items are unlocked. Disabled: listed items keep vanilla limits and all others are unlocked. |
| **Anvil Item Filter** | Items used by the anvil whitelist or blacklist mode. |
| **Enable XP Smoothing** | Consume a fixed XP-point equivalent instead of raw levels, avoiding inflated costs at very high levels. |
| **XP Calculation Base Level** | At 30, a three-level cost always uses the XP required to go from level 30 to 27. |
| **Cheap Renaming** | Pure anvil renaming always costs one level. |
| **Enhanced Channeling** | Allow Channeling lightning in any weather; level II has the enhanced behavior. |
| **Enchantment Whitelist** | Force matching item/enchantment combinations to pass compatibility checks. |
| **Enchantment Blacklist** | Strictly reject matching item/enchantment combinations and clear the anvil output. |
| **Globally Disabled Enchantments** | Enchantment IDs listed here cannot be applied by an anvil or enchanting table. Disabling this mod's own registrations requires restart. |
| **registry toggle** | Controls whether this enchantment is registered. A full restart is required. |
| **Smelt Container Contents** | Let Smelter process items inside containers such as chests and shulker boxes. |
| **Allow Fortune with Smelter** | Allow Fortune to affect drops processed by Smelter. |
| **Cook Mob Drops** | Automatically smelt applicable mob drops unless the attacker is sneaking. |
| **Smelter XP Multiplier** | Multiplier for experience awarded by Smelter recipes; 1.0 is the recipe's normal value. |
| **Smelter Output Rules** | First matching rule determines the output multiplier. |
| **Leech Effects List Is a Blacklist** | Enabled: listed effects cannot be stolen. Disabled: only listed effects can be stolen. |
| **Leech Effect List** | Status-effect IDs filtered by the selected list mode. |
| **Leech Trigger Chance** | Chance per eligible direct attack to trigger Leech, from 0.0 to 1.0. |
| **Lifesteal Ratio** | Health restored as a fraction of damage dealt, multiplied by enchantment level. |
| **Effect-Steal Chance** | Chance to move one eligible beneficial effect from the target to the attacker. |
| **Omni-Tool Base Speed Multiplier** | Base speed boost relative to the current tool; block hardness may add further compensation. |
| **Omni-Tool Enchanting Chance** | Chance in the third enchanting-table slot when its displayed level is at least 30, from 0.0 to 1.0. |
| **Omni-Tool Forced-Drop Blocks** | Exceptional blocks that forcibly drop themselves when mined with Omni-Tool. |
| **Enlightenment XP Multiplier** | Additional experience multiplier applied per Enlightenment level. |
| **Sixth Sense Display (Client)** | Local Sixth Sense scanning and outline preferences. They can be edited while connected to a remote server. |
| **Detection Radius** | Non-negative radius in blocks scanned while crouching with Sixth Sense. Very large values can be expensive. |
| **Scan Interval (Ticks)** | Positive interval between nearby-entity scans; 20 ticks is approximately one second. |
| **Custom-Color Entities** | Exact entity IDs that use the custom highlight color. |
| **color** | RGB outline color used for this entity category while Sixth Sense highlighting is active. |

#### Other Features
| Item | Description |
|---|---|
| **Smelter** | When breaking blocks (including chests) or killing mobs, automatically smelts drops. Raw ores drop double. Does not work while sneaking. |
| **Leech** | Chance to restore health or steal target's status effects on attack. |
| **Sixth Sense** | Detects and highlights nearby mobs in different colors while sneaking. |
| **Enlightenment** | Increases the amount of experience gained. |
| **Magic Protection** | Reduces magic damage taken. |
| **Omni Tool** | Omni Tool: Grants the enchanted tool the mining capabilities of Pickaxe, Axe, Shovel, Hoe, Shears. When the enchantment table's third slot reaches level 30, appears with configured probability, default 10%. Base speed is compensated by current tool speed multiplier, with additional acceleration based on block hardness. Does not bypass blocks' own drop rules; only special blocks in the config list are forced to drop. |
| **channeling2** | Enhanced Channeling II: Can summon lightning in any weather. |
| **Infinity (Enhanced)** | Shoots arrows without requiring them in inventory |
| **Infinity Bucket** | Provides infinite water source / void liquid absorption |

#### Editable Options
- Anvil Limits
- XP Smoothing
- Enchantment Rules
- Smelter
- Leech
- Other Balance Values

#### Config Defaults
| Key | Default |
|---|---|
| `anvil.removeLimit` | `true` |
| `anvil.whitelistMode` | `false` |
| `enchantment.enableBetterChanneling` | `true` |
| `enchantment.enableEnlightenment` | `true` |
| `enchantment.enableLeech` | `true` |
| `enchantment.enableOmniTool` | `true` |
| `enchantment.enableSixthSense` | `true` |
| `enchantment.enableSmelter` | `true` |
| `tax_free.cheapRenaming` | `true` |
| `tax_free.enable` | `true` |
| `tax_free.levelBase` | `30` |
| `values.enlightenmentExpMult` | `0.125` |
| `values.leechEffectBlacklistMode` | `true` |
| `values.leechLifestealRatio` | `0.1` |
| `values.leechStealChance` | `0.25` |
| `values.leechTriggerChance` | `0.5` |
| `values.omniToolBaseSpeedMultiplier` | `3.0` |
| `values.omniToolEnchantChance` | `0.10` |
| `values.sixthSenseRange` | `32.0` |
| `values.sixthSenseTickRate` | `10` |
| `values.smelterCooksMobs` | `true` |
| `values.smelterExpMultiplier` | `1.0` |
| `values.smelterFortune` | `true` |
| `values.smelterSmeltsChests` | `true` |

#### Data Paths
Primary configuration/data paths:

- `config/kineticcore/anvilenchantments.toml`

### Building from Source

- Minecraft: `1.20.1`
- Java: `17`
- ForgeGradle: `6.0.24`
- Gradle: the project is pinned to the `8.1.1` Wrapper; do not import it with Gradle 9 directly.
- Local development JARs are controlled by `local_libs_dir` and can be overridden in `gradle.properties` or with a project property.
- Typical build command: `gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS.
- Development and release artifacts use `enchantworks` as the current project identifier.

## 简体中文

### 模组定位

围绕附魔与铁砧机制扩展装备成长的模组，提供可配置铁砧规则、附魔接受规则、原版附魔增强以及多种实际玩法附魔。

本项目以游戏内管理为核心。涉及共享玩法数据、世界规则或服务器规则的功能由服务端权威处理；仅影响显示的客户端功能保持本地生效。配置界面统一使用 KineticCore 提供的 GUI 与配置基础设施。

### 主要功能

- 支持铁砧成本上限、维修惩罚、经验消耗平滑与低成本重命名。
- 支持物品/附魔组合白名单、黑名单与全局禁用规则。
- 扩展部分原版附魔及兼容装备的行为。
- 包含熔炼、汲取、第六感、启蒙、万能工具等玩法附魔。
- 规则由服务端权威处理，并通过 KineticCore 配置系统管理。

### 运行环境与兼容

| 类型 | 依赖 |
|---|---|
| 必需 | Minecraft 1.20.1 |
| 必需 | Minecraft Forge 47+ |
| 必需 | KineticCore 26.9.8+ |
| 可选 | 无 |

### 打开方式与配置

- 使用 KineticCore 配置中心对应的 F6 入口，选择 **Enchant Works**。
- 服务端规则由服务端保存，并在需要时同步给客户端。
- 纯显示类客户端设置只在本地生效。
- 各功能自己的配置/数据路径在下方详细功能说明中列出。
- 搜索、列表选择、物品/实体信息读取、悬浮提示、返回与导航等操作尽可能复用 KineticCore GUI 组件。

## 完整功能参考

### 附魔与铁砧

#### 模组定位

**Enchant Works** 是面向 Minecraft Forge 的铁砧、附魔与装备机制扩展模组。模组依赖 KineticCore，并通过 KineticCore 的统一配置系统提供服务端规则与客户端显示设置。

当前功能由四个部分组成：铁砧规则、附魔规则、原版附魔扩展，以及 Enchant Works 自带附魔。

#### 铁砧系统

##### 铁砧限制控制

- 可解除原版铁砧的“过于昂贵”等级上限。
- 对适用物品将铁砧产物的累积维修惩罚归零，避免 Prior Work Penalty 持续抬高后续成本。
- 铁砧限制可使用白名单或黑名单模式。
- 物品规则支持三种目标：
  - `minecraft:iron_sword`：单个物品 ID。
  - `#minecraft:swords`：物品标签。
  - `@create`：整个模组命名空间。
- 默认铁砧过滤名单包含 `enigmaticaddons:totem_of_malice`。

##### 经验平滑

- 铁砧与附魔台的等级成本可换算为固定的经验点数成本，而不是直接按玩家当前高等级区间扣除等级。
- 默认经验计算基准等级为 30。
- 当附魔台本身要求的等级高于基准等级时，会使用实际要求等级参与固定成本计算。
- 该机制避免高等级玩家因原版经验曲线导致同样的等级消耗对应更大的实际 XP 损失。

##### 廉价重命名

- 只有第一槽有物品、第二槽为空且操作只产生重命名结果时，铁砧成本固定为 1 级。
- 可通过服务端配置关闭。

#### 附魔规则系统

##### 附魔白名单

白名单用于强制允许指定“物品 + 附魔”组合。目标物品支持物品 ID、`#标签`、`@模组ID`；附魔部分支持多个附魔 ID。

示例：

```text
#minecraft:axes; minecraft:sharpness, minecraft:smite
minecraft:stick; minecraft:knockback, minecraft:sharpness
```

白名单命中的组合可以绕过原版物品与附魔兼容性判定。原本不可附魔但被白名单明确允许的物品，也会获得可附魔判定与附魔值，使其能够进入附魔台逻辑。

##### 附魔黑名单

黑名单用于严格禁止指定“物品 + 附魔”组合。

- 命中的附魔不会出现在附魔台可选结果中。
- 铁砧产物中出现明确禁止的组合时，产物槽会被判定为无效。
- 目标语法与白名单一致。
- 附魔部分使用 `*`、`ALL` 或留空时表示匹配全部附魔。

##### 全局禁用附魔

- 可通过附魔 ID 全局禁用附魔。
- 被禁用的附魔不能通过正常附魔台结果或铁砧组合继续添加。
- 被禁用的附魔不会作为可发现附魔或交易附魔参与正常获取逻辑。
- Enchant Works 自带附魔可在注册阶段直接不注册；这种注册级设置需要完整重启游戏或服务器后生效。

#### 原版附魔扩展

##### 无限弓

- 带有原版“无限”的弓在背包中没有箭时仍可正常拉弓。
- 射击时会生成用于本次射击的箭判定，不要求玩家实际持有普通箭。

##### 无限桶

- 桶可以参与附魔，并可获得原版“无限”附魔。
- 桶的附魔值按 10 处理。
- 单个桶物品可进入附魔判定。
- 带“无限”的桶在成功使用后保留原桶状态，不因正常取液或放液操作被消耗或替换。
- 对 Forge `FluidBucketWrapper` 的 `fill` 与两种 `drain` 流体能力操作也会保留无限桶容器状态。
- 适用于需要无限液体来源或持续抽取液体的自动化场景。

##### 强化引雷

- 原版引雷 II 可以在非雷暴天气命中实体时触发闪电条件。
- 引雷 I 仍保留原版天气要求。
- 启用该功能时，创造模式原材料页会提供引雷 II 附魔书。

#### 自带附魔

当前实际注册的 Enchant Works 附魔为：**熔炼、汲取、第六感、启蒙、万能工具**。

##### 熔炼（Smelter）

- 工具附魔，默认最高等级 I。
- 与精准采集互斥。
- 玩家使用带熔炼的主手工具破坏方块时，可将生成的掉落物直接按熔炉配方转换为熔炼结果。
- 击杀生物时，可对适用的生物掉落物执行同样的熔炼处理。
- 玩家潜行时不会触发熔炼处理，可用于主动取得原始掉落。
- 支持处理带 `BlockEntityTag.Items` 的容器物品内容，最多递归处理 4 层嵌套容器。
- 熔炼后的经验按照原熔炼配方经验计算，并支持独立经验倍率。
- 可选择是否允许时运参与熔炼掉落计算；关闭后，持有熔炼工具进行对应挖掘时会将时运等级视为 0。
- 支持按物品 ID、`#标签`、`@模组ID` 设置产出倍率规则。
- 产出规则采用首条匹配结果，格式为 `[数字]x 目标`；未写倍率时按 2x 处理。
- 默认倍率规则：
  - `2x #forge:ores`
  - `2x #forge:raw_materials`

##### 汲取（Leech）

- 武器附魔，最高等级 III。
- 只处理直接攻击，不处理间接伤害。
- 每次符合条件的攻击按配置概率触发，默认触发概率为 50%。
- 触发后按“本次伤害 × 吸血比例 × 附魔等级”恢复攻击者生命值，默认每级吸血比例为 10%。
- 触发汲取后还会独立进行一次状态效果偷取判定，默认概率为 25%。
- 只会选择目标身上的正面状态效果。
- 成功偷取时，目标失去该效果，攻击者获得同一效果实例。
- 状态效果名单支持白名单 / 黑名单模式；默认名单包含 `minecraft:slow_falling`，默认使用黑名单模式。

##### 第六感（Sixth Sense）

- 头部护甲附魔，默认最高等级 I，属于宝藏附魔。
- 玩家佩戴带第六感的装备并潜行时，在客户端扫描附近存活生物。
- 默认扫描半径为 32 格。
- 默认每 10 Tick 更新一次扫描缓存。
- 使用原版实体发光轮廓进行透视高亮，描边贴合实体模型。
- 高亮颜色按实体类别区分：玩家、友好生物、中立生物、水生生物、怪物、自定义实体。
- 默认颜色：
  - 玩家：白色 `#FFFFFF`
  - 友好生物：绿色 `#00FF00`
  - 中立生物：黄色 `#FFFF00`
  - 水生生物：青色 `#00FFFF`
  - 怪物：红色 `#FF0000`
  - 自定义实体：紫色 `#FF00FF`
- 默认自定义实体名单包含：`minecraft:warden`、`minecraft:wither`、`minecraft:ender_dragon`。
- 扫描距离、扫描间隔、自定义实体名单和所有轮廓颜色均可在客户端配置页调整。

##### 启蒙（Enlightenment）

- 护甲附魔，最高等级 II。
- 玩家击杀生物或挖掘会产生经验的方块时，提高该次行为生成的经验总量。
- 默认每级额外经验倍率为 12.5%。
- 额外经验按照“原始经验 × 附魔等级 × 配置倍率”计算。
- 加成会在原版生成经验球之前直接提高经验掉落量，不再修改、标记或依赖已经生成的经验球。

##### 万能工具（Omni Tool）

- 工具附魔，默认最高等级 I，属于宝藏附魔，不能通过村民交易获得。
- 可用于原版挖掘工具与剪刀类工具；剑不会被视为万能工具适用物品。
- 带万能工具的物品会同时参考下界合金镐、下界合金斧、下界合金铲、下界合金锄和剪刀的“正确工具”判定。
- 只在这些原版工具本身能正确采集目标方块时提供正确工具判定，不会无条件绕过所有方块掉落规则。
- 实际挖掘速度会使用当前速度倍率与方块硬度补偿共同计算；默认基础速度倍率为 3.0。
- 对树叶、蜘蛛网、藤蔓、发光地衣、草、高草、蕨、大型蕨、枯萎灌木、海草和高海草提供安全的剪刀式自身掉落处理。
- 可配置“强制掉落方块”名单；只有名单中的特殊方块在原本没有掉落时才会强制掉落自身。
- 强制掉落名单支持方块 ID、`#方块标签`、`@模组ID`。
- 使用万能工具触发强制自身掉落时，该次方块经验会被抑制；刷怪笼也使用单独的经验抑制逻辑。
- 附魔台第三档显示等级至少为 30 时，可按配置概率出现万能工具 I，默认概率 10%。
- 第三档命中万能工具时，显示结果与实际附魔结果使用同一确定性随机判定。
- 书或附魔书命中时会得到带“万能工具 I”的附魔书。
- 内置无序合成配方可使用以下物品制作“万能工具 I”附魔书：
  - 下界合金镐
  - 下界合金斧
  - 下界合金铲
  - 剪刀
  - 书

#### 配置系统

主要配置文件：

```text
config/kineticcore/anvilenchantments.toml
```

Enchant Works 向 KineticCore 注册两个配置页面：

- `enchantworks:server`：服务端权威的铁砧、经验、附魔规则和附魔数值设置。
- `enchantworks:sixth_sense`：第六感客户端扫描与轮廓显示设置。

服务端页面包含：

- 铁砧限制开关、名单模式与物品名单。
- 经验平滑开关与基准等级。
- 廉价重命名。
- 强化引雷。
- 附魔白名单、黑名单、全局禁用名单。
- 熔炼、汲取、第六感、万能工具、启蒙的注册开关。
- 熔炼容器、时运、生物掉落、经验倍率、产出倍率规则。
- 汲取效果名单、名单模式、触发概率、吸血比例、效果偷取概率。
- 万能工具速度倍率、附魔台概率、强制掉落名单。
- 启蒙经验倍率。

第六感客户端页面包含：

- 扫描半径。
- 扫描间隔。
- 自定义实体名单。
- 玩家、友好、中立、水生、怪物、自定义实体六类轮廓颜色。

普通规则与数值保存后会刷新配置缓存。附魔是否注册属于注册阶段状态，需要完整重启游戏或服务器后确定。

### 完整功能参考

#### 配置项详细说明

| 项目 | 说明 |
|---|---|
| **铁砧与附魔（服务端）** | 服务端权威的铁砧、附魔注册、过滤和数值平衡设置。没有安全同步协议时，连接远程服务器将无法编辑本页。 |
| **移除铁砧限制** | 对匹配物品移除“过于昂贵”上限和累积惩罚。 |
| **铁砧名单使用白名单模式** | 开启：只解除名单内物品的限制；关闭：名单内保留原版限制，其余物品解除限制。 |
| **铁砧物品过滤名单** | 供铁砧白名单或黑名单模式使用的物品规则。 |
| **启用经验平滑** | 按固定经验点数等价值扣除，避免高等级玩家按等级扣除时损失膨胀。 |
| **经验计算基准等级** | 设为 30 时，扣除 3 级始终按从 30 级降至 27 级所需的经验点数计算。 |
| **廉价重命名** | 仅在铁砧中重命名物品时固定消耗 1 级。 |
| **强化引雷** | 允许引雷在任意天气召唤闪电，等级 II 使用增强效果。 |
| **附魔白名单** | 强制允许匹配的物品与附魔组合，跳过原版兼容性检查。 |
| **附魔黑名单** | 严格拒绝匹配的物品与附魔组合，并清空铁砧产物。 |
| **全局禁用附魔** | 名单内附魔不能通过铁砧或附魔台添加；禁用本模组自身的注册项需要重启。 |
| **registry toggle** | 控制是否注册该附魔；修改后必须完整重启。 |
| **熔炼容器内容物** | 允许熔炼处理箱子、潜影盒等容器中的物品。 |
| **熔炼允许时运** | 允许时运影响由熔炼处理的掉落物。 |
| **烤熟生物掉落物** | 攻击者未潜行时，自动熔炼适用的生物掉落物。 |
| **熔炼经验倍率** | 熔炼配方产生经验的倍率；1.0 为配方原始经验。 |
| **熔炼产出规则** | 首条匹配规则决定产出倍率。 |
| **汲取效果名单使用黑名单模式** | 开启：名单内效果不可偷取；关闭：只能偷取名单内效果。 |
| **汲取效果名单** | 由当前名单模式过滤的状态效果 ID。 |
| **汲取触发概率** | 每次符合条件的直接攻击触发汲取的概率，范围 0.0 至 1.0。 |
| **吸血比例** | 按造成伤害的比例恢复生命，并乘以附魔等级。 |
| **效果偷取概率** | 将目标一个可用增益效果转移给攻击者的概率。 |
| **万能工具基础速度倍率** | 以当前工具速度为基础的加速倍率；方块硬度还可能提供额外补偿。 |
| **万能工具附魔台概率** | 附魔台第三档显示等级至少为 30 时的出现概率，范围 0.0 至 1.0。 |
| **万能工具强制掉落方块** | 使用万能工具挖掘时强制掉落自身的例外方块。 |
| **启蒙经验倍率** | 每级启蒙附魔额外应用的经验倍率。 |
| **第六感显示（客户端）** | 本地第六感扫描和轮廓显示偏好；连接远程服务器时仍可编辑。 |
| **探测半径** | 佩戴第六感并潜行时扫描实体的非负半径；数值过大可能造成较高性能开销。 |
| **扫描间隔（Tick）** | 重新扫描附近实体的正整数间隔；20 Tick 约为 1 秒。 |
| **自定义颜色实体** | 使用自定义高亮颜色的精确实体 ID。 |
| **color** | 第六感高亮生效时，此实体类别使用的 RGB 轮廓颜色。 |

#### 其他功能说明

| 项目 | 说明 |
|---|---|
| **熔炼** | 破坏方块(包括箱子)或击杀生物时，自动冶炼掉落物，原矿掉落翻倍。潜行时不生效。 |
| **汲取** | 攻击时有概率恢复生命值或偷取目标的增益效果。 |
| **第六感** | 潜行时探测并以不同颜色高亮周围的生物。 |
| **启蒙** | 增加获得的经验值数量。 |
| **魔法保护** | 减少受到的魔法伤害。 |
| **万能工具** | 万能工具:让附魔工具同时具备镐、斧、铲、锄、剪刀的可挖掘能力；附魔台第三档达到30级时按配置概率出现，默认10%。基础速度按当前工具速度倍率补偿，并会按方块硬度额外加速。不会绕过方块自己的掉落规则，只有配置名单里的特殊方块才会被强制掉落。 |
| **channeling2** | 增强引雷 II: 可在任何天气下引发闪电。 |
| **无限 (增强)** | 无需箭矢即可射击 |
| **无限桶** | 提供无限水源 / 虚空吸取液体 |

#### 可编辑字段、模式与分类索引

- 铁砧限制
- 经验平滑
- 附魔规则
- 熔炼
- 汲取
- 其他平衡数值

#### 配置键与默认值

| 配置键 | 默认值 |
|---|---|
| `anvil.removeLimit` | `true` |
| `anvil.whitelistMode` | `false` |
| `enchantment.enableBetterChanneling` | `true` |
| `enchantment.enableEnlightenment` | `true` |
| `enchantment.enableLeech` | `true` |
| `enchantment.enableOmniTool` | `true` |
| `enchantment.enableSixthSense` | `true` |
| `enchantment.enableSmelter` | `true` |
| `tax_free.cheapRenaming` | `true` |
| `tax_free.enable` | `true` |
| `tax_free.levelBase` | `30` |
| `values.enlightenmentExpMult` | `0.125` |
| `values.leechEffectBlacklistMode` | `true` |
| `values.leechLifestealRatio` | `0.1` |
| `values.leechStealChance` | `0.25` |
| `values.leechTriggerChance` | `0.5` |
| `values.omniToolBaseSpeedMultiplier` | `3.0` |
| `values.omniToolEnchantChance` | `0.10` |
| `values.sixthSenseRange` | `32.0` |
| `values.sixthSenseTickRate` | `10` |
| `values.smelterCooksMobs` | `true` |
| `values.smelterExpMultiplier` | `1.0` |
| `values.smelterFortune` | `true` |
| `values.smelterSmeltsChests` | `true` |

#### 配置与数据路径

主要配置/数据路径：

- `config/kineticcore/anvilenchantments.toml`

### 从源码构建

- Minecraft：`1.20.1`
- Java：`17`
- ForgeGradle：`6.0.24`
- Gradle：项目固定使用 `8.1.1` Wrapper，请不要使用 Gradle 9 直接导入。
- 默认本地依赖目录由 `local_libs_dir` 控制，可在 `gradle.properties` 或命令行参数中覆盖。
- 常用构建命令：`gradlew.bat build`（Windows）或 `./gradlew build`（Linux/macOS）。
- 生成的开发/发布文件以 `enchantworks` 作为当前工程标识。
