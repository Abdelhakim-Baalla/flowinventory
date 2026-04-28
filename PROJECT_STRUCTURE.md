# FlowInventory Mod — Project Structure

## Mod Identity
- Mod ID: flowinventory
- Mod Name: FlowInventory
- Description: AI-style smart inventory management for Minecraft
- Target: Fabric 1.20.1 (then 1.21.x)
- Java: 21

## Folder Structure
flowinventory/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/flowinventory/
│       │       ├── FlowInventoryMod.java          ← Main entry point
│       │       ├── core/
│       │       │   ├── ActivityDetector.java       ← Detects Mining/Combat/Building
│       │       │   ├── PatternEngine.java          ← AI pattern learning
│       │       │   └── InventoryManager.java       ← Core sort logic
│       │       ├── profiles/
│       │       │   ├── ActivityProfile.java        ← Mining/Combat/Build profiles
│       │       │   ├── ProfileManager.java         ← Save/load profiles
│       │       │   └── HotbarPreset.java           ← Hotbar configurations
│       │       ├── ui/
│       │       │   ├── FlowInventoryScreen.java    ← Main GUI
│       │       │   └── ProfileWidget.java          ← Profile switcher widget
│       │       ├── config/
│       │       │   └── FlowConfig.java             ← All settings
│       │       └── mixin/
│       │           ├── InventoryScreenMixin.java   ← Hook into inventory GUI
│       │           └── PlayerEntityMixin.java      ← Hook into player actions
│       └── resources/
│           ├── fabric.mod.json                     ← Mod metadata
│           ├── flowinventory.mixins.json           ← Mixin config
│           └── assets/flowinventory/
│               ├── lang/
│               │   ├── en_us.json                  ← English translations
│               │   └── ar_sa.json                  ← Arabic translations
│               └── textures/gui/                   ← Custom UI icons
├── build.gradle                                    ← Build config
├── gradle.properties                               ← Version numbers
├── settings.gradle                                 ← Project name
└── README.md                                       ← Documentation

## Feature Roadmap
### V1.0 (2 days — MVP)
- [x] Smart sort by item type
- [x] Activity detection (Mining/Combat/Building/Farming)
- [x] Auto hotbar swap when activity changes
- [x] Save/load 4 profiles
- [x] Keybind: Sort inventory (default: R)
- [x] Keybind: Cycle profiles (default: G)

### V1.1 (next sprint)
- [ ] Pattern learning (tracks usage frequency per activity)
- [ ] Cloud sync profiles (Patreon feature)
- [ ] Mod compatibility (JEI, REI)

### V2.0 (future)
- [ ] Forge port via Architectury
- [ ] 1.21.x support
- [ ] Bedrock-style add-on export
