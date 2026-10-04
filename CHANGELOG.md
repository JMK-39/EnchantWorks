2026年10月04日 14时42分 — 26.10.4

- Added NeoForge 1.21.1 and NeoForge 26.1.2 support next to Forge 1.20.1 (Java 21; Java 25 on 26.1.2; KineticCore 26.10.3+). JARs are named enchantworks-<loader>-<minecraft>-<version>.jar.
- Since 1.21 the five enchantments are data (data/enchantworks/enchantment) with the same rarity, levels, costs, slots and items as on 1.20.1; their treasure, trading and enchanting-table availability are vanilla enchantment tags, and Smelter cannot be combined with Silk Touch.
- Anvil whitelist, blacklist and disabled-enchantment rules, the anvil level cap, cheap renaming, tax-free levels, infinity bows and buckets, Channeling II without a thunderstorm, the Omni Tool roll at the enchanting table and the Omni Tool recipe work on every version.
- Default Smelter output rules use the c:ores and c:raw_materials tags on NeoForge.
- Fixed: the 1.20.1 mods.toml now carries the real mod version instead of a literal placeholder.
- Checked on 1.21.1 and 26.1.2 servers (enchantments, tags, item rules, levels and recipe) and by joining a 26.1.2 world.

- 新增 NeoForge 1.21.1 与 NeoForge 26.1.2 支持，与 Forge 1.20.1 并存（Java 21；26.1.2 为 Java 25；需要 KineticCore 26.10.3+）。JAR 命名为 enchantworks-<加载器>-<MC版本>-<版本>.jar。
- 1.21 起五种附魔改为数据（data/enchantworks/enchantment），稀有度、等级、花费、槽位与可附魔物品与 1.20.1 一致；宝藏、交易与附魔台可用性使用原版附魔标签，熔炼不能与精准采集共存。
- 铁砧白名单、黑名单与禁用附魔规则、铁砧等级上限、廉价改名、免税等级、无限弓与无限桶、无雷暴的引雷 II、附魔台的万能工具判定以及万能工具配方在所有版本中均可使用。
- NeoForge 版本中熔炼默认产出规则改用 c:ores 与 c:raw_materials 标签。
- 修复：1.20.1 的 mods.toml 现在写入真实模组版本，不再是字面占位符。
- 已在 1.21.1 与 26.1.2 服务端中检查（附魔、标签、物品规则、等级与配方），并在 26.1.2 中进入世界确认。

---

2026年10月04日 — Language key validation / 语言键一致性检查

- Require identical authored English/Chinese keys and string values in source, version overrides and packaged resources; generated formatting keys are rejected during builds.

- 强制检查源码、版本覆盖与最终资源的中英文完整键名一致、值为字符串；构建禁止派生格式语言键。

---
