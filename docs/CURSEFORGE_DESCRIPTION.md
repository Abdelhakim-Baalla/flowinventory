# CurseForge page — copy/paste guide

Use the sections below in your project **Description**. CurseForge accepts rich text; you can paste Markdown where supported or convert headings to BBCode (`[h2]Title[/h2]`).

---

## Short summary (one line)

FlowInventory: smart **inventory sorting** + **activity-based hotbar** for Fabric 1.20.1 (singleplayer & dedicated server).

---

## Full description

### Overview

**FlowInventory** helps you stay organized: press **R** to sort your inventory by smart categories, and use **G** / **V** to switch **activity profiles** (mining, combat, building, farming, exploration, etc.). The mod picks sensible **main-hand** items for each profile and works on **dedicated servers** when installed on both server and client.

### Requirements

- Minecraft **1.20.1**
- **Fabric Loader** (0.15+)
- **Fabric API**
- **Java 21**

**Important:** For multiplayer, put FlowInventory in the server `mods` folder **and** client `mods`.  
Optional: **Mod Menu** for in-game settings.

### Features

- **Smart sort** — Categories (tools, armor, food, blocks, redstone…), then name; optional **stack merge**; modes: Smart / Alphabetical / Tier.
- **Activity profiles** — Auto-detect with optional HUD, or manual **G** / **V** cycling with rules so empty profiles are skipped.
- **Hotbar layout** — Server applies presets per activity; main hand prefers the right tool (e.g. compass before sword for exploration).
- **Configurable** — JSON + Mod Menu (Cloth Config is **bundled** inside this JAR).

### Controls (default)

| Key | Action        |
|-----|---------------|
| R   | Sort inventory |
| G   | Next profile  |
| V   | Previous profile |
| B   | Toggle auto-detect |

All keys are rebindable.

### License

**MIT** — see the mod file list on GitHub for `LICENSE` and `NOTICE` (bundled libraries).

### Links

- **Issues / source:** GitHub (see mod metadata)
- **Changelog:** `CHANGELOG.md` on GitHub

---

## Release checklist (for you as author)

1. Bump `mod_version` in `gradle.properties`, update `CHANGELOG.md`.
2. Run `./gradlew build`; test `runClient` and optionally a **dedicated server** with one client.
3. Attach `LICENSE`, mention **MIT** and Cloth Config in **NOTICE** on the project page.
4. Tag Git release `v1.x.x` and upload the **same** JAR to CurseForge + Modrinth if desired.
5. Set CurseForge **Java version** to 21; **Minecraft** 1.20.1; dependency on **Fabric API**.
