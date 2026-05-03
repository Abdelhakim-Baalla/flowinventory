# Changelog

All notable changes to **FlowInventory** are documented in this file.

Format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [1.1.0] — 2026-05-03

### Compared to CurseForge build **[1.0.0]**

The published **1.0.0** JAR was a **client-only** build: `fabric.mod.json` had `"environment": "client"`, bundled **Cloth Config** inside the JAR, and contained a smaller class set (no dedicated `HotbarSwapper`, `ItemDatabase`, `ItemHeuristics`, `ProfileManager`, `ActivityChangePacket`, Mod Menu API entrypoint, etc.). **1.1.0** is a full gameplay-oriented release intended for **singleplayer + dedicated servers**.

### Added

- **Dedicated server support** — `environment: "*"` so packet handlers register on the server; hotbar profile swaps and sorting run with server authority.
- **Activity → hotbar** — `ActivityChangePacket` + `HotbarSwapper`: presets per `ActivityType` (tools, blocks, food lanes, off-hand shield/totem where defined).
- **Item knowledge** — large `ItemDatabase` (combinatorial vanilla 1.20.1 coverage) + `ItemHeuristics` scoring (tools, blocks, food, redstone, emergencies, etc.).
- **Smart hand selection** — activity-ordered priorities (e.g. Exploration: compass/map before sword), max-score preset alignment, split placement vs hand score thresholds; building blocks prioritized over sugar cane / bamboo as generic “BLOCK”.
- **Activity cycling** — `ActivityCycle` / `ActivityCycleRules`: **G** next, **V** previous, eligibility rules so “empty” profiles are not offered; key-repeat cooldown to avoid log storms.
- **G/V ordering** — manual profile change runs **before** `ActivityDetector.tick()` so the locked activity matches the key press.
- **Sort modes** — config: `SMART`, `ALPHABETICAL`, `TIER`; optional `mergeStacks`, `lockHotbar`; **activity-aware bias** when sorting (client sends current activity with sort packet).
- **Mod Menu** — entrypoint + config screen (Cloth Config).
- **HUD** — activity overlay, lock timer, hints (see config).
- **Emergency / vitals presets** — explicit hand-priority lists for low health, hunger, fire, lava, drowning, falling, poison, wither, sleeping (mirrors preset intent).
- **MIT license file** in distributed JAR (`LICENSE_flowinventory` via Gradle).
- **Documentation** — this changelog, README, CurseForge description template, `NOTICE` for bundled libraries.

### Changed

- **Fabric Loader / Loom** — project tracks Fabric Loom 1.9.x, Java **21**, Minecraft **1.20.1**.
- **Cloth Config** — still **included** in the mod JAR (`include` in Gradle) but **no longer** the only documented install path; users only need Fabric API + this mod.
- **Logging / behaviour** — combat vs food heuristics tuned (eating dampens false combat); tie-breakers and stickiness adjusted in `ActivityDetector`.

### Fixed

- Wrong item in main hand after **G** when a weak “BLOCK” (e.g. sugar cane) sat in slot 0 while logs/tools scored higher elsewhere.
- Exploration profile snapping to **sword** when compass/map were absent from early slots without a deliberate priority order.
- Farming profile not pulling **seeds / crops** when no hoe was present.
- **SLEEPING** profile: `FOOD` was incorrectly treated as a deferred “snack” and could never be chosen for main hand when no bed was available.

### Notes for server admins

- Install **FlowInventory** on the **server** as well as the client so **R** (sort) and **G** (profile) packets apply inventories correctly.
- Clients without the mod still connect; they simply do not send packets.

---

## [1.0.0] — CurseForge baseline

- Client-only Fabric mod for Minecraft **1.20.1**, Java **21**.
- Core sort, basic activity detection, inventory mixin, `SortInventoryPacket`.
- Description and metadata matched repository `fabric.mod.json` (MIT, same author links).

---

[1.1.0]: https://github.com/Abdelhakim-Baalla/flowinventory/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Abdelhakim-Baalla/flowinventory/releases/tag/v1.0.0
