# AcquiredUtils

Fabric mod for Minecraft **1.21.11**. Implements the settings GUI described in
`AcquiredUtils_GUI_Master_Layout_Map.md`.

## What's here

```
build.gradle, settings.gradle, gradle.properties   — Loom project config
src/main/java/dev/bobodado/acquiredutils/
  AcquiredUtils.java                                — common entrypoint
  config/AcquiredUtilsConfig.java                    — settings model + JSON load/save
src/client/java/dev/bobodado/acquiredutils/client/
  AcquiredUtilsClient.java                           — client entrypoint, keybinding
  gui/AcquiredUtilsConfigScreen.java                 — the settings screen (root)
  gui/widget/DropdownWidget.java                     — custom select widget
  gui/widget/ExampleSliderWidget.java                — 0.1–5.0 slider
src/main/resources/
  fabric.mod.json, assets/acquiredutils/lang/en_us.json
  assets/acquiredutils/textures/gui/*.png            — PLACEHOLDER textures (flat colors/simple shapes)
  assets/acquiredutils/icon.png                      — PLACEHOLDER mod icon
```

Open the game with the `'` (apostrophe) key by default to bring up the
settings screen (rebindable in vanilla Controls once loaded).

## ⚠️ I could not build or run this in the sandbox that generated it

This code was written in an environment whose network access is limited to a
small allow-list (GitHub, npm, PyPI, crates.io, apt) that does **not**
include `maven.fabricmc.net` or Mojang's asset/library servers. That means:

- I could not run `./gradlew build` to confirm this actually compiles.
- I could not verify exact dependency version strings (`fabric_version` in
  `gradle.properties` is a literal placeholder — see the comment there).
- I could not verify method/class signatures against the real 1.21.11
  Mojang-mappings jar. Every place I'm not fully confident about is marked
  `VERIFY:` in a comment.

**Before you trust this**, run it locally and fix whatever Loom/javac flags.
Given 1.21.11 is a brand-new toolchain generation (Yarn/Intermediary just
retired in favor of shipping unobfuscated Mojang-mapped jars — see
[fabricmc.net's 1.21.11 announcement](https://fabricmc.net/2025/12/05/12111.html)),
there's a real chance some class/package names have shifted since my
training data. Treat this as a strong first draft, not a guaranteed-working
build.

## Build & verify checklist

1. **`gradle.properties`** — confirm `fabric_version` against
   [modrinth.com/mod/fabric-api](https://modrinth.com/mod/fabric-api) (pick
   the build tagged for 1.21.11), and double check `loader_version` /
   `loom_version` against [fabricmc.net/develop](https://fabricmc.net/develop/).
2. **`build.gradle`** — confirm `mappings loom.officialMojangMappings()` is
   still the correct call for a Yarn-free 1.21.11 project under Loom 1.14.
3. **`AcquiredUtilsConfigScreen.java`** — confirm `Checkbox.builder(...)`,
   `Button.builder(...)`, and the `GuiGraphics` method names
   (`fill`, `drawString`, `renderOutline`) match the real API.
4. **`ExampleSliderWidget.java`** / **`DropdownWidget.java`** — confirm
   `AbstractSliderButton` and `AbstractWidget` constructor/abstract-method
   signatures (`renderWidget` vs. older `renderButton` naming).
5. **`AcquiredUtilsClient.java`** — confirm `KeyMapping`, `InputConstants`,
   and `KeyBindingHelper` package paths/signatures.

## Known gaps / open items (carried over from the layout map, §10)

- Slider's real default value isn't specified anywhere — currently `2.5f`
  (midpoint), stored in `AcquiredUtilsConfig.exampleSliderValue`.
- Unchecked-checkbox art is a guess (not shown in the reference image) —
  `textures/gui/checkbox_unchecked.png`.
- Config backend: currently a hand-rolled Gson JSON file at
  `.minecraft/config/acquiredutils.json`
  (`AcquiredUtilsConfig.java`). Swap for Cloth Config if you'd rather have
  its screen-builder instead of the hand-built `AcquiredUtilsConfigScreen`.
- Post-save UX: `SAVE CHANGES` currently just persists and logs — screen
  stays open. Change the button handler in
  `AcquiredUtilsConfigScreen.buildFooter()` if you want it to close or show
  a confirmation toast instead.
- **Tabs are vanilla `Button`s, not the custom bordered/iconed `TabWidget`**
  described in the layout map (§4.3) — this was simplified so the screen has
  *something* clickable; swap in real icon textures + custom rendering when
  ready (gear/keyboard icons already exist as placeholders in
  `textures/gui/icon_gear.png` / `icon_keyboard.png`, just not wired up to
  the tab buttons yet).
- **Title gradient** is a flat two-color approximation (orange word, white
  rest), not a true per-character gradient — see the comment in
  `drawOverlayText()` for the two real options (custom font atlas glyph
  coloring, or a baked gradient texture strip).
- **Custom font atlas**: not included. Screen currently renders with the
  vanilla font. Per your earlier decision, drop the real atlas at
  `assets/acquiredutils/font/acquiredutils_gui.{json,png}` and wire it in
  when ready — nothing currently references that path yet.
- All textures under `assets/acquiredutils/textures/gui/` are simplified
  placeholders (flat fills / basic shapes) generated to match the asset list
  in the layout map §8 — replace with real 9-sliced, properly shaded art
  before release.

## Reference

Full widget-by-widget spec: `AcquiredUtils_GUI_Master_Layout_Map.md` (shared
separately). Section numbers in code comments (`§3.2`, `§4.6`, etc.) refer
back to that document.
