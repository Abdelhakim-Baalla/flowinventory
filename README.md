# ⚡ FlowInventory

**Smart AI-style inventory management for Minecraft 1.20.1**

> Stop wasting time sorting. Start playing.

---

## ✨ What makes FlowInventory different?

Most inventory mods just sort items alphabetically or by type.

**FlowInventory learns how YOU play.**

It detects your current activity (⛏ Mining, ⚔ Combat, 🧱 Building, 🌾 Farming) and:
- **Auto-organizes** your entire inventory (hotbar + main) with smart category sorting
- **Detects activity changes** in real-time (~0.5s response time)
- **Merges scattered stacks** — no more 3 separate piles of cobblestone

---

## 🎮 Features

| Feature | Description |
|---|---|
| **Smart Sort** (R key) | Sorts full inventory (hotbar + main) by category, then by name |
| **In-Screen Sort** | Press R while inventory is open — sorts instantly without closing |
| **Stack Merging** | Automatically combines scattered partial stacks |
| **Activity Detection** | Detects Mining/Combat/Building/Farming from held item + nearby mobs |
| **Manual Cycle** (G key) | Manually switch between activity profiles |
| **HUD Overlay** | See your current detected activity in the top-right corner |
| **Configurable** | JSON config for detection range, HUD position, and more |

---

## ⌨️ Keybinds

| Key | Action |
|---|---|
| `R` | Sort inventory (hotbar + main inventory) — works in-game and inside inventory screen |
| `G` | Cycle to next activity profile |

All keybinds can be rebound in **Options → Controls → FlowInventory**

---

## 📦 Sort Categories

Items are sorted in this priority order:

| Priority | Category | Examples |
|---|---|---|
| 1 | Swords | Diamond Sword, Iron Sword |
| 2 | Pickaxes | Diamond Pickaxe, Stone Pickaxe |
| 3 | Axes | Iron Axe, Netherite Axe |
| 4 | Shovels | Diamond Shovel |
| 5 | Hoes | Iron Hoe |
| 6 | Armor | Helmet, Chestplate, Leggings, Boots |
| 7 | Shields | Shield |
| 8 | Food | Steak, Bread, Golden Apple |
| 9 | Blocks | Cobblestone, Planks, Dirt |
| 10 | Other | Everything else |

Within each category, items are sorted alphabetically by name.

---

## 🔧 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download FlowInventory and place in your `mods` folder
4. Launch Minecraft — done!

**Optional:** Install [Mod Menu](https://modrinth.com/mod/modmenu) to see FlowInventory in the mod list.

### Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.20.1 |
| Fabric Loader | ≥ 0.15.0 |
| Fabric API | Any |
| Java | ≥ 21 |

---

## ⚙️ Configuration

Config file is located at: `.minecraft/config/flowinventory/config.json`

```json
{
  "autoDetectActivity": true,
  "autoApplyProfile": true,
  "activitySwitchDelay": 30,
  "combatDetectionRange": 8,
  "sortMode": "SMART",
  "mergeStacks": true,
  "showHudOverlay": true,
  "hudPosition": "TOP_RIGHT",
  "showActivityMessages": true,
  "showKeyHints": true,
  "enablePatternLearning": true
}
```

---

## 🤝 Compatibility

- ✅ Just Enough Items (JEI)
- ✅ Roughly Enough Items (REI)
- ✅ Mod Menu
- ✅ All major modpacks
- ⚠️ May conflict with other mods that also modify inventory sorting

---

## 🗺️ Roadmap

- **v1.0** — Core features (sort, profiles, activity detection) ✅
- **v1.1** — Improved pattern learning, JEI deep integration
- **v1.2** — Cloud profile sync (Patreon feature)
- **v2.0** — Forge/NeoForge port, Minecraft 1.21.x support

---

## 💖 Support Development

If FlowInventory saves you time, consider supporting:
- ⭐ Star this repo on GitHub
- 👍 Leave a review on Modrinth/CurseForge

---

## 📝 License

MIT License — free to use, modify, and distribute with attribution.

---

*Made with ❤️ by Abdelhakim Baalla*
