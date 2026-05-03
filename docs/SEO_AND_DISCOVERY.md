# SEO & discovery (CurseForge, Modrinth, GitHub)

Minecraft hosts do not use `robots.txt` inside your mod JAR. Discovery comes from **metadata, tags, and consistent naming**. Use this when filling project pages.

---

## Stable naming (use everywhere)

| Field | Suggested value |
|--------|------------------|
| Project slug | `flowinventory` (lowercase, no spaces) |
| Display name | `FlowInventory` |
| Minecraft | `1.20.1` |
| Loader | `Fabric` |

Link **GitHub**, **CurseForge**, and **Modrinth** to each other in every description (“Also on …”).

---

## High-value keywords (natural language)

Work these into the **first paragraph** of your description (human-readable, not keyword stuffing):

- Fabric mod, Minecraft 1.20, inventory sort, hotbar, activity profiles, server compatible, Fabric API, quality of life, QoL, singleplayer, multiplayer, Mod Menu, smart sort

---

## CurseForge: categories & tags

Pick all that truly apply, for example:

- **Categories:** Map & Information, Fabric, Utility (adjust to what CurseForge offers for 1.20.1 Fabric).
- Mention **server + client** install in the first lines (filters and search favor clear compatibility text).

---

## Modrinth: categories & keywords

Examples:

- **Categories:** Utility, Management  
- **Additional keywords** (if available): `inventory`, `sorting`, `hotbar`, `fabric`, `server`

Set **client/server** side correctly: this mod is **both** (world interaction + packets).

---

## GitHub repository (indirect SEO)

1. **About** box: short one-liner + link to CurseForge / Modrinth.
2. **Topics** (repository topics): `minecraft`, `fabric`, `fabric-mod`, `inventory`, `sorting`, `java`, `minecraft-mod`
3. **README.md** first heading includes **Fabric** and **1.20.1** (already aligned).
4. **CHANGELOG.md** and **LICENSE** visible at repo root (trust + crawl).

---

## `fabric.mod.json` (launchers & Aggregators)

Already strong:

- `description` — concise feature list (Mods screen / launchers).
- `contact.homepage`, `sources`, `issues` — keep GitHub URLs correct when you move hosts.

Optional later: add a `custom` block only if a tool you use reads it (not required for basic discovery).

---

## Duplicate content

It is fine to reuse the same changelog on GitHub, CurseForge, and Modrinth. Identical **version numbers** matter more than unique prose per site.
