# Sinsay Branding System

> Extracted from https://www.sinsay.com/pl/pl/ on 2026-03-27.
> All styles sourced from live computed styles and CSS design tokens.

## Brand Overview

Sinsay is a fashion retailer targeting young adults with accessible pricing. The visual identity is clean, minimal,
and modern — built around a warm amber/terracotta primary colour with a dark neutral palette. The brand tone is casual
and energetic (Polish-language UI with emojis in promotional copy).

**Site tagline**: "Great fashion, great prices"

---

## Assets

| File | Description |
|------|-------------|
| `docs/assets/sinsay-logo.svg` | Wordmark SVG (84×31 px, dark fill `#16181D`) |
| `docs/assets/sinsay-logo.png` | Wordmark raster screenshot |
| `docs/assets/sinsay-header.png` | Header bar screenshot |
| `docs/assets/sinsay-homepage.png` | Full homepage viewport screenshot |

---

## Logo

The Sinsay logo is a custom wordmark SVG (`84×31 px`). The letterforms spell "Sinsay" with a distinctive double-dot
detail on the letter **i** (separate dot glyph used as a design accent).

- **Fill colour**: `#16181D` (near-black, matches `--color-dark-90`)
- **Usage**: white header bar; the SVG is inline in the page HTML inside `.brand-logo-button`
- **Do not**: recolour the logo outside of `#16181D` or `#ffffff` (for dark backgrounds)

**Icon asset** (chevron/store selector):
```
https://www.sinsay.com/pl/pl/media/SHARED/stronywizerunkowe/sinsay/homepage-new/src/assets/icons/icon_chevron_redesign.svg
```

---

## Color Palette

Sourced from `design-tokens.css` CSS custom properties on `:root`.

### Primary — Amber/Terracotta

The brand's signature warm orange used for CTAs, highlights, and loyalty programme.

| Token | Hex | Usage |
|-------|-----|-------|
| `--color-primary-50` | `#E09243` | **Primary** — buttons, active states |
| `--color-primary-40` | `#F2B06D` | Hover / lighter tint |
| `--color-primary-60` | `#B2671B` | Darker accent |
| `--color-primary-30` | `#F2C291` | Subtle background tint |
| `--color-primary-20` | `#FFDFBF` | Light background |
| `--color-primary-10` | `#FFF2E5` | Pale background / disabled |
| `--color-primary-70` | `#804306` | Deep variant |
| `--color-primary-80` | `#4D2600` | Very dark |
| `--color-primary-90` | `#1A0D00` | Near-black tint |

### Dark / Neutral

| Token | Hex | Usage |
|-------|-----|-------|
| `--color-dark-100` | `#000000` | Pure black |
| `--color-dark-90` | `#18191A` | Near-black — logo colour |
| `--color-dark-80` | `#303133` | Dark text |
| `--color-dark-70` | `#494A4D` | Secondary text |
| `--color-dark-60` | `#616366` | Muted text |
| `--color-dark-50` | `#7B7D80` | Placeholder text |
| `--color-dark-40` | `#949699` | Disabled |
| `--color-dark-30` | `#AFB0B2` | Border light |
| `--color-dark-20` | `#C8C9CC` | Divider |
| `--color-dark-10` | `#E3E4E5` | Background subtle |
| `--color-dark-5` | `#F1F2F4` | Background surface |
| `--color-white-100` | `#FFFFFF` | White |

### Semantic colours

| Token | Hex | Usage |
|-------|-----|-------|
| `--color-red-50` | `#FF0023` | Error / sale badge |
| `--color-red-60` | `#CC001C` | Dark error |
| `--color-green-50` | `#0DB209` | Success / in-stock |
| `--color-blue-50` | `#2E90E5` | Info / link |
| `--color-orange-50` | `#FF9900` | Warning |

### Alpha tokens (rgba overlays)

| Token | Value |
|-------|-------|
| `--color-dark-alpha-50` | `rgba(0,0,0,0.5)` |
| `--color-dark-alpha-20` | `rgba(0,0,0,0.2)` |
| `--color-white-alpha-80` | `rgba(255,255,255,0.8)` |

---

## Typography

### Font Families

| Context | Stack |
|---------|-------|
| Body / global | `Euclid, Arial, Helvetica, "Helvetica Neue", sans-serif` |
| Header / nav | `Euclid, sans-serif` |
| Buttons / UI components | `"Euclid Circular B", Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif` |

**Primary typeface**: **Euclid** (proprietary geometric sans-serif, Swiss Typefaces).
**Secondary typeface**: **Euclid Circular B** (rounded variant for interactive elements).

Font files served from:
```
https://www.sinsay.com/skin/frontend/dev/narch/public/fonts/
```

### Type Scale

| Role | Size | Weight | Line height | Letter spacing |
|------|------|--------|-------------|----------------|
| Body default | 16px | 400 | — | normal |
| Header / nav | 14px | 400 | — | normal |
| H1 (modal) | 24px | 600 | 24px | -0.2px |
| H3 / section heading | 16px | 600 | 24px | 0.15px |
| Paragraph | 14px | 400 | 24px | normal |
| Button label | 16px | 600 | — | uppercase |
| Category name | 10px | 400 | 12px | normal |
| Price | 14px | 500 | — | — |

---

## Component Styles

### Buttons

#### Primary Button (CTA)

```css
background-color: #E09243;        /* --color-primary-50 */
color: #FFFFFF;
border: 2px solid #E09243;
border-radius: 0px;               /* sharp corners */
padding: 12px 32px;
font-size: 16px;
font-weight: 600;
text-transform: uppercase;
font-family: "Euclid Circular B", sans-serif;
```

#### Secondary / Outline Button

```css
background-color: transparent;
color: #FFFFFF;
border: 2px solid rgba(255,255,255,0.8);
border-radius: 0px;
padding: 12px 32px;
font-size: 16px;
font-weight: 600;
text-transform: uppercase;
```

#### Key characteristics
- **No border-radius** — Sinsay uses sharp (0px) corners on all buttons
- Uppercase text labels
- Padding `12px 32px` (comfortable touch target)
- Primary colour `#E09243` matches `--color-primary-50`

### Links

```css
color: #FFFFFF;                   /* on dark backgrounds */
color: rgb(34, 37, 42);          /* on light backgrounds */
font-size: 14px;
font-weight: 500;
text-decoration: none;
```

### Inputs

```css
background-color: transparent;
color: rgb(34, 37, 42);
border: none;                     /* border typically on wrapper */
border-radius: 0px;
padding: 0px;                     /* padding on container */
font-size: 14px;
font-family: Euclid, sans-serif;
```

Newsletter email input class: `.newsletter-input`

---

## Spacing System

Sourced from CSS custom properties:

| Token | Value | Usage |
|-------|-------|-------|
| `--size-s` | `4px` | Tight spacing, icon gaps |
| `--size-sm` | `8px` | Small gaps |
| `--size-m` | `16px` | Default spacing unit |
| `--size-l` | `32px` | Section padding |
| `--size-xl` | `64px` | Large sections |
| `--size-xxl` | `128px` | Hero sections |

---

## Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| `--radius-base-xs` | `2px` | — |
| `--radius-base-s` | `4px` | Small cards |
| `--radius-base-sm` | `8px` | Inputs, badges |
| `--radius-base-m` | `16px` | Cards, panels |
| `--radius-base-l` | `32px` | Pill-shaped large |
| `--radius-pill-m` | `20px` | Default pill button |
| `--radius-pill-xl` | `32px` | Large pill |

> Note: the primary CTA buttons use `border-radius: 0px` — the radius tokens are used in cards/badges, not buttons.

---

## Elevation (Shadows)

| Token | Value |
|-------|-------|
| `--elevation-01` | `0px 2px 12px rgba(24,25,26,0.08), 0px 1px 2px rgba(26,13,0,0.08)` |
| `--elevation-02` | `0px 4px 16px rgba(24,25,26,0.10), 0px 1px 4px rgba(26,13,0,0.10)` |
| `--elevation-03` | `0px 6px 24px rgba(24,25,26,0.12), 0px 1px 6px rgba(26,13,0,0.10)` |
| `--elevation-04` | `0px 8px 40px rgba(24,25,26,0.14), 0px 2px 12px rgba(26,13,0,0.12)` |

---

## Breakpoints / Viewports

| Token | Value |
|-------|-------|
| `--viewport-xs` | `320px` |
| `--viewport-s` | `640px` |
| `--viewport-m` | `1008px` |
| `--viewport-l` | `1540px` |

---

## Navigation & Header

- **Header background**: `#FFFFFF` (white)
- **Header text / links**: `rgb(51,51,51)` — `--color-dark-80` (`#303133`)
- **Promotional banner**: dark background with white text + amber highlights
- **Nav items**: font 14px/400, `Euclid, sans-serif`
- **Active/hover**: uses primary colour `#E09243`

Navigation categories (PL): Polecane, Kobieta, Dom, Dziecko, Mężczyzna

---

## UI Patterns Relevant to the Complaint/Return Form

### Form Design Recommendations

1. **Colours**: use white background (`#FFFFFF`), dark text (`#303133`), amber primary for submit button (`#E09243`)
2. **Buttons**: sharp corners (`border-radius: 0`), uppercase labels, padding `12px 32px`
3. **Inputs**: borderless inner field, border on wrapper container, `--color-dark-30` (`#AFB0B2`) for border colour
4. **Error state**: `--color-red-50` (`#FF0023`) for validation errors
5. **Success state**: `--color-green-50` (`#0DB209`) for confirmation messages
6. **Typography**: body 14–16px `Euclid` sans-serif, weight 400 for labels, 600 for headings/buttons
7. **Spacing**: multiples of `--size-m` (16px) for form field gaps; `--size-sm` (8px) for label-to-input
8. **Section heading**: 16px/600/`#18191A`
9. **Placeholder text**: `--color-dark-50` (`#7B7D80`)
10. **Focus ring**: use `--color-primary-50` (`#E09243`) as outline colour to match brand

### Form Label Copy (Polish)

- Submit / send: "Wyślij"
- Required field: "Pole wymagane"
- Upload photo: "Dodaj zdjęcie"
- Complaint type: "Rodzaj zgłoszenia" with options "Reklamacja" / "Zwrot"
- Order number: "Numer zamówienia"
- Description: "Opis problemu"

---

## Stylesheet References

Design tokens:
```
https://www.sinsay.com/pl/pl/skin/frontend/6.444.0/narch/design-tokens/design-tokens.css
```

Design system / icons:
```
https://www.sinsay.com/pl/pl/skin/frontend/6.444.0/narch/design-system/design-icons/sinsay.css
```

Shared components:
```
https://www.sinsay.com/cms-statics/shared-components/0.91.25-rc-39/sinsay.css
```
