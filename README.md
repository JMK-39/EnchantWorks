# Enchant Works

[English](#english) · [简体中文](#简体中文) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantworks)

## English

Enchant Works combines configurable anvil rules, enchantment filters, five custom enchantments and extensions to Infinity and Channeling.

### Adjust the Sixth Sense detection radius

Search the client settings for the detection radius. Change 32 to 48 and save. The running client now uses 48 blocks for the Sixth Sense scan radius.

![Searching the Sixth Sense settings, changing the radius from 32 blocks to 48 and saving it](https://raw.githubusercontent.com/wiki/JMK-39/EnchantWorks/media/tutorials-20261126/en.gif)

<details>
<summary>Full walkthrough with Mandarin narration</summary>

https://github.com/user-attachments/assets/03fb616a-304a-4ce3-badc-5157cf27ae7f

</details>

[Editor guide](https://github.com/JMK-39/EnchantWorks/wiki/Tutorial)

- Control anvil level limits, prior-work penalties, renaming costs and fixed-base XP costs.
- Allow or deny item/enchantment combinations and disable selected enchantments.
- Use Smelter, Leech, Sixth Sense, Enlightenment and Omni Tool for equipment progression.
- Configure gameplay through KineticCore's configuration center (F6 by default); Sixth Sense has separate client display settings.

| Minecraft | Loader | Java |
| --- | --- | --- |
| 1.20.1 | Forge 47.4.2+ | 17 |
| 1.21.1 | NeoForge 21.1+ | 21 |
| 26.1.2 | NeoForge 26.1.2+ | 25 |

Install on both client and server with matching KineticCore: **26.11.24+ on every supported version**. Enchantments are data-driven from 1.21 onward.

[Detailed tutorial](https://github.com/JMK-39/EnchantWorks/wiki/Tutorial) · [Changelog](CHANGELOG.md)

---

## 简体中文

附魔工坊整合可配置的铁砧规则、附魔组合过滤、五种自定义附魔，以及无限和引雷附魔的扩展。

### 调整「第六感」的探测半径

在客户端设置里搜索「探测半径」，把 32 改为 48 并保存。本次实录也核对了运行中的设置，第六感的扫描半径已变成 48 格。

![查找第六感探测半径、把 32 格改为 48 格并保存的实录](https://raw.githubusercontent.com/wiki/JMK-39/EnchantWorks/media/tutorials-20261126/zh.gif)

<details>
<summary>播放完整普通话讲解</summary>

https://github.com/user-attachments/assets/03fb616a-304a-4ce3-badc-5157cf27ae7f

</details>

[编辑器使用指南](https://github.com/JMK-39/EnchantWorks/wiki/%E4%BD%BF%E7%94%A8%E6%95%99%E7%A8%8B)

- 调整铁砧等级限制、累积维修惩罚、改名费用和固定基准经验计费。
- 允许或禁止指定物品与附魔组合，并禁用指定附魔。
- 使用熔炼、汲取、第六感、启蒙和万能工具丰富装备成长。
- 通过 KineticCore 配置中心（默认 F6）调整玩法，第六感另有独立的客户端显示设置。

| Minecraft | 加载器 | Java |
| --- | --- | --- |
| 1.20.1 | Forge 47.4.2+ | 17 |
| 1.21.1 | NeoForge 21.1+ | 21 |
| 26.1.2 | NeoForge 26.1.2+ | 25 |

客户端与服务端均需安装，并使用对应版本的 KineticCore：**所有支持版本均为 26.11.24+**。1.21 起附魔采用数据驱动。

[详细使用教程](https://github.com/JMK-39/EnchantWorks/wiki/使用教程) · [更新日志](CHANGELOG.md)

## Configuration files / 配置文件

Requires KineticCore 26.11.24 or newer. Configuration files are grouped under `config/kineticcore/enchantworks/`. Old files migrate automatically before loading; an existing new file wins and the conflicting old file is preserved.

需要 KineticCore 26.11.24 或更新版本。配置统一归档在 `config/kineticcore/enchantworks/`；首次读取前自动迁移旧文件，已有新文件优先，并保留发生冲突的旧文件。

| Old path below `config/kineticcore/` / 旧相对路径 | New filename / 新文件名 |
| --- | --- |
| `anvilenchantments.toml` | `anvilenchantments.toml` |
