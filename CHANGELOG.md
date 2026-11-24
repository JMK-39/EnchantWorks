2026年11月24日 — 26.11.24 — Configuration directory / 配置目录
- Anvil and enchantment configuration moves to `config/kineticcore/enchantworks/anvilenchantments.toml`. The legacy file migrates automatically without replacing an existing destination. Requires KineticCore 26.11.24.
- 铁砧与附魔配置归档至 `config/kineticcore/enchantworks/anvilenchantments.toml`，自动迁移旧文件且不覆盖已有新文件；需要 KineticCore 26.11.24。

2026年10月26日 — 26.10.26 — Clearer editors / 更清晰的编辑界面
- The rule editor is a form: target items show as icons with their names, the enchantment and multiplier rows follow, and the Choose buttons are as wide as their labels.
- 规则编辑页改为表单：目标物品以图标加名称显示，其后是附魔或倍率行，“选择”按钮缩短为文字所需宽度。

2026年10月24日 — 26.10.24 — Buttons at the top, standard slots / 按钮置顶与统一物品格
- Enchantment and smelter rules are shown as what they do: the target items as icons (tags cycle their items, # and @ marks), an arrow, then the enchantments or the output multiplier. The title is centred on the screen; right-click a rule to edit or remove it.
- 附魔与熔炼规则按实际作用展示：目标物品以图标显示（标签轮播其物品，带 # / @ 标记），箭头后是附魔或产出倍率；标题以屏幕为中心居中；右键规则可编辑或移除。

2026年10月20日 — Editor canvas / 编辑画布

- Rule lists, rule editors and the enchantment picker keep every action in the header row (Apply top-right); lists and pickers start right under the header and show more rows.
- 规则列表、规则编辑与附魔选择页的所有按钮移到顶栏（应用在右上角）；列表从顶栏下方开始，显示更多行。
- Visual rule and selection pages use a full canvas with one white outer frame, gray-white header dividers and four-pixel control margins. Selection rows keep their outlines.
- 可视化规则与选择页使用完整画布、单层白色外框、灰白标题分隔线及四点控件边距，保留选择行边框。

---

2026年10月16日 — Direct effect and block selection / 状态效果与方块直接多选

- Widen the Leech effect search field and support Delete for the selected enchantment/Smelter rule through the core input API. Text-field editing keeps priority.
- 汲取效果搜索框加宽；附魔与熔炼规则列表支持通过核心输入 API 按 Delete 删除当前规则，文本框编辑优先。
- Leech effect lists open all registered effects with icons, IDs, translated names and multi-selection. Search and action buttons sit at the top; existing unregistered effects remain individually selectable.
- Omni Tool forced-drop rules open the shared item-search multi-selector directly, including block tags and mods. Selection uses block IDs, block tags and block namespaces, and preserves unknown existing rules.
- 汲取效果名单直接显示全部状态效果的图标、ID 与翻译名称并支持多选，搜索和操作按钮置顶，已有未注册效果仍可单独反选。
- 万能工具强制掉落方块直接进入核心物品搜索多选，支持方块标签与模组规则；始终按方块 ID、方块标签及方块所属模组保存，保留未知已有规则。

---

2026年10月15日 — Search hints / 搜索提示

- The search fields of the enchantment and block pickers show a hint while empty.

- 附魔与方块选择页的搜索框在为空时显示提示文字。

---

2026年10月13日 — Editor titles and rows / 编辑页标题与行距

- Visual rule editor titles line up with their Cancel buttons.
- The enchantment and Smelter rule editors list targets, mode, enchantments and multiplier in evenly spaced rows below the title, without empty gaps.

- 可视化规则编辑页的标题与取消按钮对齐。
- 附魔与熔炼规则编辑页的目标、模式、附魔与倍率按统一行距排列在标题下方，不再留出空白。

---

2026年10月10日 — Visual rule editing / 可视化规则编辑

- Choose Omni Tool forced-drop blocks, anvil item filters, Smelter output multipliers, and enchantment allowlists, denylists and global exclusions through visual selectors. Item, tag and mod targets no longer require typed rule syntax.
- Enchantment selections support multiple choices, search and individual removal of disabled or unregistered entries. Apply changes to the parent draft, then save the configuration; cancelling preserves existing rules.
- Place the visual editors' Cancel buttons at the upper left for consistent return navigation.
- Requires KineticCore 26.10.10.1 or later.

- 万能工具强制掉落、铁砧物品过滤、熔炼产出倍率、附魔白名单、黑名单和全局禁用改为可视化选择物品、标签、模组及附魔，无需手填规则语法。
- 附魔列表支持多选、搜索，并可单独移除已禁用或未注册的条目；应用后返回配置页保存，取消时保留原规则。
- 可视化编辑页的取消返回按钮统一放在左上角，方便按相同位置返回上级。
- 需要 KineticCore 26.10.10.1 或更新版本。

---

2026年10月08日 — Bilingual guide / 双语使用指南

- Detailed configuration, enchantment and multiplayer instructions are now available in separate English and Chinese Wiki guides. The project overview focuses on features and requirements.

- 详细配置、附魔和多人游戏说明已整理为独立的中英文 Wiki 教程，项目首页聚焦功能介绍与运行环境。

---

2026年10月06日 07时18分 — 26.10.6

- Enchantment tooltips follow the server's settings: when you join a server, its Enchant Works settings (such as disabled enchantments) are loaded once for you, so tooltips no longer depend on your own local config file. Nothing is sent to other players when an admin edits the settings; they see the change when they next join.
- Requires KineticCore 26.10.6+.

- 附魔提示遵循服务器的设置：加入服务器时会为你加载一次附魔工坊的服务器设置（例如禁用的附魔），提示不再依赖你自己的本地配置文件。管理员修改设置时不会向其他玩家发送任何内容，他们会在下次加入时看到变更。
- 需要 KineticCore 26.10.6+。

---

2026年10月04日 14时42分 — 26.10.4

- Added NeoForge 1.21.1 and NeoForge 26.1.2 support next to Forge 1.20.1 (Java 25 on 26.1.2; KineticCore 26.10.3+). JARs are named enchantworks-<loader>-<minecraft>-<version>.jar.
- Since 1.21 the five enchantments are data (data/enchantworks/enchantment) with the same rarity, levels, costs, slots and items as on 1.20.1; their treasure, trading and enchanting-table availability are vanilla enchantment tags, and Smelter cannot be combined with Silk Touch.
- Anvil whitelist, blacklist and disabled-enchantment rules, the anvil level cap, cheap renaming, tax-free levels, infinity bows and buckets, Channeling II without a thunderstorm, the Omni Tool roll at the enchanting table and the Omni Tool recipe work on every version.
- Default Smelter output rules use the c:ores and c:raw_materials tags on NeoForge.
- Fixed: Forge 1.20.1 shows the real mod version instead of a placeholder.
- Fixed: tax-free levels now also apply at the enchanting table; previously enchanting always took the full levels.
- Fixed: items allowed by the enchantment whitelist can now be enchanted at the enchanting table (enchantability 10), as the whitelist rules intended.

- 新增 NeoForge 1.21.1 与 NeoForge 26.1.2 支持，与 Forge 1.20.1 并存（26.1.2 为 Java 25；需要 KineticCore 26.10.3+）。JAR 命名为 enchantworks-<加载器>-<MC版本>-<版本>.jar。
- 1.21 起五种附魔改为数据（data/enchantworks/enchantment），稀有度、等级、花费、槽位与可附魔物品与 1.20.1 一致；宝藏、交易与附魔台可用性使用原版附魔标签，熔炼不能与精准采集共存。
- 铁砧白名单、黑名单与禁用附魔规则、铁砧等级上限、廉价改名、免税等级、无限弓与无限桶、无雷暴的引雷 II、附魔台的万能工具判定以及万能工具配方在所有版本中均可使用。
- NeoForge 版本中熔炼默认产出规则改用 c:ores 与 c:raw_materials 标签。
- 修复：Forge 1.20.1 显示真实模组版本，不再显示占位符。
- 修复：免税等级现在也作用于附魔台，此前附魔台始终按完整等级扣除经验。
- 修复：附魔白名单允许的物品现在可以在附魔台附魔（附魔能力 10），符合白名单规则的本意。
