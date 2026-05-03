# Security

## Reporting a vulnerability

Please **do not** open a public GitHub issue for undisclosed security problems.

- Prefer **private** reporting if GitHub **Security Advisories** are enabled for the repository: use **Security → Report a vulnerability**.
- Otherwise, contact the maintainer via the options listed on the [GitHub profile / repository](https://github.com/Abdelhakim-Baalla/flowinventory).

## Scope

This project is a **Minecraft Fabric mod** (client + server packets). Typical concerns:

- Malicious servers or modified clients are outside this mod’s control; players should only join trusted servers and use official builds from **CurseForge**, **Modrinth**, or **GitHub Releases**.

## Secrets

Never commit API keys, host upload tokens, or session data. See `.gitignore` and `docs/LOCAL/` for local-only notes.
