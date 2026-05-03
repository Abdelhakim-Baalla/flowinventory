# FlowInventory

Smart inventory sorting and **activity-aware hotbar** layout for **Minecraft 1.20.1** (Fabric).

FlowInventory estimates what you are doing (mining, combat, building, farming, exploration, …), lets you **cycle profiles** with keybinds, applies **server-side** hotbar rules, and sorts storage by category (with optional activity-based ordering).

---

## Requirements

| Component | Version |
|-----------|---------|
| Minecraft | **1.20.1** |
| Fabric Loader | **≥ 0.15.0** |
| [Fabric API](https://modrinth.com/mod/fabric-api) | Any build for 1.20.1 |
| Java | **21** |

**Dedicated server:** install this mod on the **server** and on **clients** that use sorting / profile switching. Vanilla clients can still join; they simply will not use the features.

---

## Features

- **Sort (R)** — Sorts player inventory (optionally excluding hotbar via config). Modes: **SMART** (categories + name), **ALPHABETICAL**, **TIER**. Optional stack merge; when using SMART from the client, sort can bias order toward the **current activity**.
- **Activity detection** — Heuristic scoring from held items, nearby mobs, context (tunable in config).
- **Profiles (G / V)** — Cycle **forward / backward** through a curated list of activities (not every enum value). Manual switch applies a short **lock** so auto-detect does not instantly overwrite your choice.
- **Hotbar presets** — Per-activity layout + **main-hand selection** (e.g. exploration prefers compass/map before sword; farming prefers hoe/seeds/crops; emergencies prefer milk, potions, water bucket, etc.).
- **HUD** — Optional overlay for current activity and lock countdown.
- **Config** — JSON file + **Mod Menu** screen (Cloth Config). Cloth Config classes are **bundled** in the FlowInventory JAR; you still need Fabric API separately.

---

## Controls

Rebind in **Options → Controls → FlowInventory**.

| Default key | Action |
|-------------|--------|
| **R** | Sort inventory (in-world or inside inventory screen, via mixin) |
| **G** | Next activity profile |
| **V** | Previous activity profile |
| **B** | Toggle **auto-detect activity** on/off (persists in config) |

---

## Configuration

Path: `.minecraft/config/flowinventory/config.json`

Notable options:

- `autoDetectActivity`, `autoApplyProfile` — auto behaviour.
- `sortMode` — `SMART` | `ALPHABETICAL` | `TIER`
- `mergeStacks`, `lockHotbar` — sort behaviour.
- `combatDetectionRange`, `activitySwitchDelay`, thresholds — detection tuning.

See `FlowConfig.java` for the full field list and defaults.

---

## Building

```bash
./gradlew build
```

Output: `build/libs/flowinventory-<version>.jar`

---

## Distribution & license

- **License:** [MIT](LICENSE) — see `LICENSE` in the repo; a copy is renamed and embedded in the built JAR.
- **Third-party:** [NOTICE](NOTICE) — bundled Cloth Config; Fabric API is a separate download.

---

## Scope and limitations (honest checklist)

The mod is designed for **survival-style play** on **Fabric 1.20.1**. It does **not** try to cover every possible world state exhaustively. In particular:

- **Other players / mobs** — Detection uses **your** client or **your** server player; it does not “know” what everyone on the server is doing.
- **Creative / Spectator** — Less relevant; sorting still runs if invoked, but profile automation may be pointless.
- **Modded items** — Heuristics use paths, tags, and `Item` classes where possible; odd mods may classify as generic categories or `MISC`.
- **Containers** — Sort applies to **player inventory**, not chests or mods that replace inventory handling entirely.
- **Conflicts** — Other mods that force hotbar slot, cancel packets, or replace screen hooks may interfere.

If something misclassifies, adjust config thresholds or file an issue with **activity**, **held items**, and **steps**.

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

---

## Author & links

- **Author:** Abdelhakim Baalla  
- **Repository / issues:** see `fabric.mod.json` `contact` section.
