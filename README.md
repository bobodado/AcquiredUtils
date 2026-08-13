<p align="center">
  <img src="src/main/resources/assets/acquiredutils/icon.png" alt="AcquiredUtils Logo" width="128" height="128">
</p>

<h1 align="center">AcquiredUtils</h1>

<p align="center">
  Client-side Minecraft Fabric utilities for custom SMP / MMORPG servers.
</p>

# AcquiredUtils

Client-side Minecraft Fabric utility mod for Minecraft 1.21.11.

## Current features

- Slot Lock with configurable keybind
- Item Pickup Notifier with rarity-colored notifications
- Rarity tiers: Common, Uncommon, Rare, Epic, Legendary, Mythic
- Rarity triangle highlight on inventory items
- Unlocked Recipe Highlight for `Recipes - ...` vault screens
- Custom purple configuration GUI
- Menu Scale slider
- Item Pickup HUD editor

## Build

Linux/Codespaces:

```bash
chmod +x gradlew
./gradlew clean build
```

Windows CMD:

```bat
gradlew.bat clean build
```

The project intentionally contains no AcquiredUtils-owned Mixins or access wideners. Container overlays use the public Minecraft 1.21.11 `AbstractContainerScreen` API plus Fabric screen events.
