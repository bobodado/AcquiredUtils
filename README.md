<p align="center">
  <img src="src/main/resources/assets/acquiredutils/icon.png" alt="AcquiredUtils" width="128" height="128">
</p>

<h1 align="center">AcquiredUtils</h1>

<p align="center">
  Client-side Fabric utilities for FakePixel SMP.
</p>

<p align="center">
  <b>Minecraft 1.21.11</b> · <b>Fabric</b> · <b>Client-side</b>
</p>

---

## Overview

AcquiredUtils is a client-side utility mod designed for the **FakePixel SMP** experience.

It provides lightweight quality-of-life features for inventory management, item collection, rarity recognition, recipe-vault interaction, HUD customization, and other everyday gameplay tasks.

AcquiredUtils does not add content to the server and does not require changes to the server itself.

## Features

### Inventory & Item Management

- Slot locking for protected inventory slots
- Advanced slot-lock controls for hotbar, inventory, armor, and offhand
- Favorite item marking
- Item comparison for supported equipment
- Inventory item search and highlighting
- Rarity indicators for custom items

### Item Pickup

- Item pickup notifications
- Rarity-aware notification colors
- Minimum rarity filtering
- Duplicate pickup stacking
- Item icons in notifications
- Configurable notification duration
- Custom HUD positioning

### Overlays

- Custom rarity circle indicators
- Recipe unlock highlighting inside supported recipe vaults
- Player inventory search highlighting
- Configurable visual overlays

### Interface

- Purple and gold themed configuration interface
- Searchable settings
- Menu scaling
- Scrollable settings sections
- HUD editor with presets
- Custom controls and keybind configuration

## Compatibility

AcquiredUtils is developed specifically for:

- **FakePixel SMP**
- **Minecraft 1.21.11**
- **Fabric Loader**
- **Fabric API**

The mod is intended to run on the client.

## Installation

1. Install Minecraft **1.21.11** with Fabric Loader.
2. Install the required Fabric dependencies.
3. Download the latest AcquiredUtils release.
4. Place the AcquiredUtils `.jar` file in your Minecraft `mods` directory.
5. Launch Minecraft using your Fabric installation.

## Configuration

Open the AcquiredUtils configuration screen through the configured keybind or the available client configuration entry.

Settings are organized into:

- **General**
- **Item Pickup**
- **Overlays**
- **Keybinds**

The configuration menu includes a built-in search field for quickly finding settings.

## Development

### Requirements

- Java 21
- Gradle Wrapper
- Minecraft 1.21.11
- Fabric Loom
- Fabric API

### Build

Linux / GitHub Codespaces:

```bash
chmod +x gradlew
./gradlew build
```

Windows:

```bat
gradlew.bat build
```

The built mod is generated in:

```text
build/libs/
```

## Project Structure

```text
src/
├── main/
│   ├── java/
│   └── resources/
└── client/
    └── java/
```

Client-side functionality is contained in the client source set.

## License

See the repository license file for licensing terms.
