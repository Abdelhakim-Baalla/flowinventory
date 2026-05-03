# NeoForge / multi-loader strategy

Your **Fabric 1.20.1** tree is the source of truth today. A NeoForge port is a **large** effort (not a find/replace): different mod loading, networking, events, config UI, and (often) **Mixin → Forge/NeoForge hooks**.

## Important version note

**NeoForge** is strongest on **newer** Minecraft lines (e.g. **1.20.4+**, **1.21.x**). For **1.20.1** specifically, the ecosystem is still mostly **Fabric** and **Forge**. Before committing, pick a **single NeoForge + Minecraft version** (e.g. *1.21.1 + NeoForge 21.1.x*) from the [NeoForge site](https://neoforged.net/) and build *for that*, rather than assuming 1.20.1 parity.

## Recommended ways to keep Fabric “unchanged” + add NeoForge

### Option A — Git branches (simplest mentally)

| Branch | Role |
|--------|------|
| `fabric/1.20.1` or `main` | Current Fabric code; tag releases `v1.x-fabric` |
| `neoforge/1.21.x` (example) | Port / new Gradle; tag `v1.x-neoforge` |

- **Pros:** Fabric files stay exactly as they are on `main`; no risk of breaking Fabric while experimenting.  
- **Cons:** Two codebases to maintain unless you introduce shared code (copy-paste or submodule).

### Option B — Architectury (one repo, two loader subprojects)

- Gradle structure: **`common/`** (shared logic: sort rules, heuristics, item DB) + **`fabric/`** + **`neoforge/`** (thin loader-specific: registration, packets, keybinds, client init).
- **Pros:** One place for game logic; industry standard for multi-loader mods.  
- **Cons:** Up-front refactor to strip Fabric-only imports from `common`; still non-trivial work.

### Option C — Second repository

- **`flowinventory`** (Fabric) vs **`flowinventory-neoforge`**.  
- **Pros:** Zero risk of mixing loaders.  
- **Cons:** Duplication unless you extract a shared library (Maven module or git submodule).

## What must be rewritten per loader (high level)

| Area | Fabric (current) | NeoForge |
|------|------------------|----------|
| Entry | `ModInitializer`, `ClientModInitializer` | `Mod` bus + `@SubscribeEvent` |
| Network | Fabric Networking API | `RegisterPayloadHandlersEvent`, payloads |
| Keybinds | Fabric Key Binding API | `RegisterKeyMappingsEvent` |
| Client tick | Fabric `ClientTickEvents` | NeoForge client tick event |
| Mixins | `fabric.mod.json` mixins | Often fewer; use events / hooks |
| Config GUI | Cloth (Fabric) | NeoForge config / other GUI lib |

**Shared or almost shared:** sorting algorithms, `ItemDatabase` / `ItemHeuristics` **if** they only use vanilla `net.minecraft.*` and no Fabric types.

## Practical sequence

1. **Freeze Fabric:** tag current `main` (or merge branch) as `v1.1.0-fabric` (or your released version).
2. **Choose NeoForge MC version** (e.g. 1.21.x).
3. **Either** create branch `neoforge` and a **new** Gradle (NeoForge MDK / template) **or** migrate whole repo to Architectury in a **dedicated branch** so `main` Fabric stays recoverable.
4. Port in order: **server sort + packets** → **hotbar swap** → **client keys + HUD** → **activity detector** (heaviest).

## This repository

- Fabric sources remain under `src/main/...` with Loom until you explicitly refactor.  
- Do **not** delete Fabric to “convert”; use a **branch** or **Architectury split** so CurseForge Fabric releases stay buildable from `main` (or `fabric` branch).

For questions (timeline, Architectury vs dual branch), decide **target Minecraft + NeoForge version** first; everything else follows.
