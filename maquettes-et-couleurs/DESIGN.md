# Design System Document

## 1. Overview & Creative North Star: "The Ethereal Organizer"

This design system is not a mere utility; it is a "Digital Sanctuary." Moving away from the rigid, grid-locked nature of traditional productivity tools, this system embraces **Soft Minimalism**. Our Creative North Star is the **Ethereal Organizer**—a layout that feels like fine stationery floating in a well-lit, airy studio.

We break the "standard" template look by utilizing **intentional asymmetry** and **high-scale typographic contrast**. By prioritizing generous whitespace (using our `16` and `20` spacing tokens) and removing structural lines, we create an experience where the user’s schedule feels light and manageable, rather than a heavy list of burdens. The interface should breathe, utilizing overlapping elements and subtle tonal shifts to guide the eye.

---

## 2. Colors & Tonal Depth

The palette is rooted in soft, professional pastels and deep, sophisticated ink tones. 

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning or containment. Boundaries must be defined solely through background color shifts. Use `surface-container-low` (`#f0f4f7`) to define a section against the primary `background` (`#f7f9fb`). 

### Surface Hierarchy & Nesting
Treat the UI as a series of stacked, semi-opaque sheets.
- **Base Level:** `background` (#f7f9fb)
- **Section Level:** `surface-container` (#eaeff2)
- **Interactive/Floating Level:** `surface-container-lowest` (#ffffff)
This nesting creates a sense of "physical" depth without the clutter of lines.

### Signature Textures & Glassmorphism
To elevate the "out-of-the-box" feel, floating elements (like the FAB or modal overlays) should utilize **Glassmorphism**. Apply a `backdrop-blur` of 12px-20px to `surface_container_lowest` at 80% opacity. 
- **CTA Soul:** For main Action Buttons, use a subtle linear gradient from `primary` (#16658e) to `primary_container` (#87c8f6) at a 45-degree angle to provide a premium, tactile finish.

---

## 3. Typography: Editorial Authority

We use a dual-font strategy to balance modern functionality with editorial character.

- **Display & Headlines (Plus Jakarta Sans):** These are our "Statement" styles. Use `display-lg` for daily overviews to create a bold, confident focal point. The wide apertures of Plus Jakarta Sans lend a friendly yet precise feel.
- **Body & Labels (Manrope):** Chosen for its modern, geometric structure. Manrope ensures that even at `body-sm`, task descriptions remain hyper-legible.
- **Scale Contrast:** Don't be afraid of the "Big and Small" approach. Pair a `display-sm` date header with a `label-md` uppercase subtitle to create a sophisticated, high-end magazine aesthetic.

---

## 4. Elevation & Depth: Tonal Layering

Traditional drop shadows are often too "dirty" for a minimalist aesthetic. This system uses **Ambient Light** principles.

- **The Layering Principle:** Achieve lift by stacking. A `surface-container-lowest` card placed on a `surface-container-low` background creates a natural, soft highlight.
- **Ambient Shadows:** When a true "float" is required (e.g., a Task Card being dragged), use an extra-diffused shadow: `box-shadow: 0 20px 40px rgba(44, 52, 55, 0.06)`. Note the use of `on-surface` (#2c3437) as the shadow tint rather than pure black.
- **The "Ghost Border" Fallback:** If a container requires further definition (e.g., in high-sunlight environments), use an `outline-variant` (#acb3b7) at **15% opacity**. High-contrast, opaque borders are strictly forbidden.

---

## 5. Components

### Task Cards & Time Slots
- **Rule:** Absolute prohibition of divider lines. 
- **Style:** Use a vertical `spacing-5` (1.7rem) gap between tasks. Task blocks use the `DEFAULT` (1rem) corner radius.
- **Color Coding:** Use `primary_container` (Blue), `secondary_container` (Green), and `tertiary_container` (Orange) for task categorization. These should be soft, pastel washes that don't compete with the text.

### Floating Action Button (FAB)
- **Shape:** `full` roundedness (Pill/Circle).
- **Style:** Use the **Signature Texture** gradient. It should sit in the bottom right, utilizing an **Ambient Shadow** to feel like it is hovering significantly above the agenda.

### Input Fields
- **Style:** No bottom lines or boxed outlines. Use `surface_container_high` as a subtle background fill with `md` (1.5rem) rounded corners.
- **States:** On focus, the background shifts to `surface_container_lowest` with a "Ghost Border" of `primary` at 20% opacity.

### Navigation / Time Indicators
- **Style:** Use `label-md` for time stamps (08h, 09h). These should be rendered in `on_surface_variant` (#596064) to remain present but secondary to the tasks themselves.

---

## 6. Do’s and Don’ts

### Do
- **Do** lean into asymmetry. Off-center your headers or allow cards to have varied heights to mimic a more natural, human touch.
- **Do** use `primary_fixed_dim` for subtle interactive states like hover or press on a card.
- **Do** prioritize "Breathing Room." If a layout feels crowded, increase the spacing from `8` to `12`.

### Don’t
- **Don't** use 1px dividers between agenda items. Use whitespace or a subtle shift from `surface` to `surface-container`.
- **Don't** use pure black (#000000) for text. Always use `on_surface` (#2c3437) to maintain the soft, premium feel.
- **Don't** use "Sharp" corners. Every element must have at least a `sm` (0.5rem) radius to stay "friendly" and "approachable."