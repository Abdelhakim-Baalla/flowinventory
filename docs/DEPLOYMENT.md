# Deployment: Git branches → GitHub → CurseForge → Modrinth

Use this as your checklist for a clean **1.x** release.

---

## 1. Pre-flight (workspace health)

From the **repository root** (clone of [flowinventory on GitHub](https://github.com/Abdelhakim-Baalla/flowinventory)):

```bash
./gradlew clean build --no-daemon
```

On Windows, the same command works in **PowerShell** or **cmd** from that folder (`gradlew.bat` is used automatically).

- Expect **BUILD SUCCESSFUL**.
- Output JAR: `build/libs/flowinventory-<version>.jar` (see `mod_version` in `gradle.properties`).

**Machine-specific notes** (paths, API keys, upload tokens): keep them only in **ignored** files — see `.gitignore` (`docs/**/*.local.md`, `docs/LOCAL/`).

Match **game stack** to the JAR:

- Minecraft **1.20.1**
- **Fabric Loader** ≥ 0.15
- **Fabric API** (required)
- **Java 21**

Do **not** upload the `-sources.jar` to CurseForge/Modrinth as the primary file (optional secondary / Maven).

---

## 2. Git: feature branch → integration → release

You currently have branches such as `feature/auto-hotbar-swap`, `develop`, `main`, `feature/release-build`. A simple, professional flow:

### A. Finish the feature branch

```bash
git checkout your-feature-branch
git status
git add -A
git commit -m "Describe the final changes for release"
git push -u origin your-feature-branch
```

### B. Open a Pull Request

- **Target:** `develop` (if you use it as integration) **or** `main` if you merge straight to production.
- **Title:** e.g. `Release 1.1.0 — server hotbar, heuristics, docs`
- **Description:** paste relevant sections from `CHANGELOG.md`.
- Request review if you work with others.

### C. Merge order (recommended)

1. Merge feature PR → **`develop`**.
2. Quick sanity check on `develop` (build + quick in-game test).
3. Merge **`develop`** → **`main`** (or open PR `develop` → `main`).
4. For important long-lived branches (`feature/release-build`, etc.): either merge them into `develop` first or delete if obsolete after merge.

### D. Tag the release on `main`

After `main` has the version you want:

```bash
git checkout main
git pull origin main
git tag -a v1.1.0 -m "FlowInventory 1.1.0"
git push origin v1.1.0
```

Keep **tag** = **mod version** in `gradle.properties`.

---

## 3. GitHub Release (optional but recommended)

1. GitHub → **Releases** → **Draft a new release**.
2. Choose tag `v1.1.0`.
3. Title: `1.1.0`.
4. Body: copy from `CHANGELOG.md` for that version.
5. Attach **`flowinventory-1.1.0.jar`** (the remapped mod JAR from `build/libs/`).
6. Publish.

Modrinth can sync from GitHub Releases if you enable it later.

---

## 4. CurseForge

1. Log in → your project → **Files** → **Upload File**.
2. **Minecraft version:** 1.20.1  
3. **Mod loader:** Fabric  
4. **Java:** 21 (if the form asks)  
5. **Dependencies:** add **Fabric API** as *required*.
6. **Changelog:** paste the same block as in `CHANGELOG.md` / GitHub Release.
7. **Display name:** e.g. `FlowInventory 1.1.0`.
8. Upload the **main remapped JAR** from `build/libs/` (name matches `archives_base_name` + `mod_version`).
9. **Double-check** each file’s Minecraft version matches what you built (e.g. **1.20.1** for this branch); avoid listing the wrong game version on the host.

Project settings (do once):

- Description: use `docs/CURSEFORGE_DESCRIPTION.md` as a base.
- License: **MIT** (match repo).
- Source link: GitHub.
- **Tags/categories:** Fabric, Utility, Map & Information, Server (if applicable) — see `docs/SEO_AND_DISCOVERY.md`.

---

## 5. Modrinth

1. Log in → **Dashboard** → your project (or create project).
2. **Upload version** → select JAR.
3. **Game versions:** 1.20.1  
4. **Loaders:** Fabric  
5. **Dependencies:** Fabric API — **Required**.
6. **Version title / number:** align with `mod_version` (e.g. `1.1.0`).
7. **Changelog:** same as GitHub / CurseForge.
8. **Project license** (sidebar): set to **MIT** to match [`LICENSE`](https://github.com/Abdelhakim-Baalla/flowinventory/blob/main/LICENSE) in the repo, not ARR, unless you intentionally reserve all rights.

Optional:

- **Environment:** server + client (because `fabric.mod.json` has `environment: "*"`).
- **Sync:** connect GitHub Releases to auto-publish (Modrinth project settings).

---

## 6. After publish

- Bump **`mod_version`** in `gradle.properties` for the *next* patch (e.g. `1.1.1-SNAPSHOT` or keep `1.1.0` until you start new work — your choice).
- Add **`[Unreleased]`** section at top of `CHANGELOG.md` for the next cycle.

---

## Credential safety

Never commit CurseForge token or Modrinth token. Use:

- Host web UI uploads (manual), or
- CI secrets (`CURSEFORGE_TOKEN`, `MODRINTH_TOKEN`) if you add a publish workflow later.

See `.gitignore` for patterns already ignored for local secrets.
