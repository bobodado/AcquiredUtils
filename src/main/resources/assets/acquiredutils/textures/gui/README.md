# Placeholder Texture Guide

The mod uses procedural rendering and works without any textures.

## Optional Textures (for future enhancement)

Create in `src/main/resources/assets/acquiredutils/textures/gui/`:

- `panel_background.png` — 32x32 tileable dark pattern
- `sidebar_background.png` — 32x32 tileable, slightly lighter
- `frame_border.png` — 24x24 9-slice for window frame
- `icons/gear.png` — 16x16 settings icon (white on transparent)
- `icons/keyboard.png` — 16x16 keyboard icon (white on transparent)

If these textures exist, they will be used. Otherwise, procedural fallbacks are drawn.
