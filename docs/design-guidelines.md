# Sinsay Design Guidelines

> Derived from live site (https://www.sinsay.com/pl/pl), `docs/BRANDING.md`, and `docs/assets/design-tokens.json`.
> Source captured: 2026-03-27.

These guidelines govern the visual implementation of the Sinsay customer advisory chat (complaint/return form). All UI
must match the Sinsay brand and use Polish language exclusively.

---

## 1. Brand Identity

- **Brand name**: Sinsay
- **Tagline**: "Great fashion, great prices"
- **Tone**: casual, energetic, direct — Polish-language UI with emoji acceptable in promotional copy
- **Target audience**: young adults, fashion-forward, price-conscious

---

## 2. Logo

| Property | Value |
|----------|-------|
| Format | SVG wordmark (`sinsay-logo.svg`) |
| Dimensions | 84×31 px |
| Fill colour | `#16181D` (on white) / `#FFFFFF` (on dark) |
| Placement | Inside `.brand-logo-button`, white header bar |

**Rules:**
- Never recolour outside `#16181D` or `#FFFFFF`
- Do not stretch, rotate, or apply effects
- Maintain clear space of at least `--size-m` (16px) around the logo

---

## 3. Colour Palette

### 3.1 Primary — Amber/Terracotta

| Token | Hex | Use |
|-------|-----|-----|
| `--color-primary-50` | `#E09243` | **Main brand colour** — CTAs, active states, focus rings |
| `--color-primary-40` | `#F2B06D` | Hover state |
| `--color-primary-60` | `#B2671B` | Pressed / darker accent |
| `--color-primary-30` | `#F2C291` | Subtle highlight |
| `--color-primary-20` | `#FFDFBF` | Light background tint |
| `--color-primary-10` | `#FFF2E5` | Disabled / pale surface |

### 3.2 Neutral / Dark

| Token | Hex | Use |
|-------|-----|-----|
| `--color-dark-90` | `#18191A` | Logo, headings |
| `--color-dark-80` | `#303133` | Body text, nav links |
| `--color-dark-70` | `#494A4D` | Secondary text |
| `--color-dark-60` | `#616366` | Muted / helper text |
| `--color-dark-50` | `#7B7D80` | Placeholder text |
| `--color-dark-40` | `#949699` | Disabled text |
| `--color-dark-30` | `#AFB0B2` | Input borders |
| `--color-dark-20` | `#C8C9CC` | Dividers |
| `--color-dark-10` | `#E3E4E5` | Subtle background |
| `--color-dark-5` | `#F1F2F4` | Surface background |
| `--color-white-100` | `#FFFFFF` | Page / card background |

### 3.3 Semantic

| Token | Hex | Use |
|-------|-----|-----|
| `--color-red-50` | `#FF0023` | Error, validation failure |
| `--color-red-60` | `#CC001C` | Dark error variant |
| `--color-green-50` | `#0DB209` | Success, in-stock confirmation |
| `--color-blue-50` | `#2E90E5` | Informational, links on light bg |
| `--color-orange-50` | `#FF9900` | Warning / alert |

### 3.4 Alpha Overlays

| Token | Value | Use |
|-------|-------|-----|
| `--color-dark-alpha-50` | `rgba(0,0,0,0.5)` | Modal backdrop |
| `--color-dark-alpha-20` | `rgba(0,0,0,0.2)` | Hover overlay |
| `--color-white-alpha-80` | `rgba(255,255,255,0.8)` | Secondary button border on dark |

---

## 4. Typography

### 4.1 Font Families

| Context | Stack |
|---------|-------|
| Body / global | `Euclid, Arial, Helvetica, "Helvetica Neue", sans-serif` |
| Navigation / header | `Euclid, sans-serif` |
| Buttons / interactive UI | `"Euclid Circular B", Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif` |

**Euclid** is the proprietary geometric sans-serif (Swiss Typefaces). **Euclid Circular B** is its rounded variant
used for interactive elements. Fonts are served from the Sinsay CDN — ensure the correct `@font-face` declarations are
present.

### 4.2 Type Scale

| Role | Size | Weight | Line height | Letter spacing |
|------|------|--------|-------------|----------------|
| Page body default | 16px | 400 | — | normal |
| Nav / header text | 14px | 400 | — | normal |
| H1 (modal/dialog) | 24px | 600 | 24px | -0.2px |
| H2 / section heading | 20px | 600 | — | normal |
| H3 / card heading | 16px | 600 | 24px | 0.15px |
| Paragraph / label | 14px | 400 | 24px | normal |
| Button label | 16px | 600 | — | uppercase |
| Small / category | 10px | 400 | 12px | normal |
| Price display | 14px | 500 | — | — |

---

## 5. Spacing System

Based on CSS custom properties from design tokens:

| Token | Value | Use |
|-------|-------|-----|
| `--size-s` | `4px` | Icon gaps, tight inline spacing |
| `--size-sm` | `8px` | Label-to-input gap, small gaps |
| `--size-m` | `16px` | Default form field gap |
| `--size-l` | `32px` | Section padding, card padding |
| `--size-xl` | `64px` | Large section spacing |
| `--size-xxl` | `128px` | Hero sections |

**Rule**: use multiples of `--size-m` (16px) as the baseline grid unit.

---

## 6. Border Radius

| Token | Value | Used for |
|-------|-------|----------|
| `--radius-base-xs` | `2px` | Micro elements |
| `--radius-base-s` | `4px` | Small cards |
| `--radius-base-sm` | `8px` | Inputs, badges |
| `--radius-base-m` | `16px` | Cards, panels |
| `--radius-base-l` | `32px` | Pill-shaped large elements |
| `--radius-pill-m` | `20px` | Default pill |
| `--radius-pill-xl` | `32px` | Large pill |

> **Important**: Primary CTA buttons use `border-radius: 0px` — sharp corners are a Sinsay brand signature.

---

## 7. Elevation (Shadows)

| Token | Value | Use |
|-------|-------|-----|
| `--elevation-01` | `0px 2px 12px rgba(24,25,26,0.08), 0px 1px 2px rgba(26,13,0,0.08)` | Cards, dropdowns |
| `--elevation-02` | `0px 4px 16px rgba(24,25,26,0.10), 0px 1px 4px rgba(26,13,0,0.10)` | Floating panels |
| `--elevation-03` | `0px 6px 24px rgba(24,25,26,0.12), 0px 1px 6px rgba(26,13,0,0.10)` | Modals |
| `--elevation-04` | `0px 8px 40px rgba(24,25,26,0.14), 0px 2px 12px rgba(26,13,0,0.12)` | Top-level overlays |

---

## 8. Components

### 8.1 Buttons

#### Primary (CTA)
```css
background-color: #E09243;        /* --color-primary-50 */
color: #FFFFFF;
border: 2px solid #E09243;
border-radius: 0px;
padding: 12px 32px;
font-size: 16px;
font-weight: 600;
text-transform: uppercase;
font-family: "Euclid Circular B", sans-serif;
cursor: pointer;
```
- Hover: `background-color: #F2B06D` (`--color-primary-40`)
- Active/pressed: `background-color: #B2671B` (`--color-primary-60`)
- Disabled: `background-color: #FFF2E5`, `color: #949699`

#### Secondary / Outline (on dark background)
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

#### Key rules
- **Always** `border-radius: 0px` on buttons
- Labels in **UPPERCASE**
- Minimum touch target: 44px height

### 8.2 Inputs & Form Fields

```css
/* Wrapper/container */
border: 1px solid #AFB0B2;        /* --color-dark-30 */
border-radius: 0px;
padding: 12px 16px;
background-color: #FFFFFF;

/* Inner input element */
background-color: transparent;
color: #303133;                   /* --color-dark-80 */
border: none;
font-size: 14px;
font-family: Euclid, sans-serif;
```

- Placeholder: `color: #7B7D80` (`--color-dark-50`)
- Focus border: `1px solid #E09243` (`--color-primary-50`) + outline matching primary colour
- Error border: `1px solid #FF0023` (`--color-red-50`)
- Error message: `color: #FF0023`, `font-size: 12px`, below the field

### 8.3 Form Layout

```
┌─────────────────────────────────┐
│  Label (14px/400/#303133)       │
│  ┌───────────────────────────┐  │
│  │ Input field               │  │
│  └───────────────────────────┘  │
│  Helper / error text (12px)     │
└─────────────────────────────────┘
```

- Gap between label and input: `--size-sm` (8px)
- Gap between fields: `--size-m` (16px)
- Section gap: `--size-l` (32px)

### 8.4 Links

```css
/* On light background */
color: #303133;
font-size: 14px;
font-weight: 500;
text-decoration: none;

/* On dark background */
color: #FFFFFF;
```
- Hover: `color: #E09243` (primary amber)

### 8.5 File Upload (image attachment)

- Label: "Dodaj zdjęcie"
- Drag-and-drop area with dashed border `1px dashed #AFB0B2`
- Accept icon in `--color-primary-50`
- Accepted formats display in `--color-dark-60` text

### 8.6 Status / Feedback States

| State | Colour | Icon hint |
|-------|--------|-----------|
| Success | `#0DB209` (`--color-green-50`) | checkmark |
| Error | `#FF0023` (`--color-red-50`) | × |
| Warning | `#FF9900` (`--color-orange-50`) | ! |
| Info | `#2E90E5` (`--color-blue-50`) | ℹ |
| Loading | animated amber spinner (`--color-primary-50`) | — |

---

## 9. Layout & Breakpoints

| Token | Value | Breakpoint |
|-------|-------|------------|
| `--viewport-xs` | `320px` | Small mobile |
| `--viewport-s` | `640px` | Mobile |
| `--viewport-m` | `1008px` | Tablet |
| `--viewport-l` | `1540px` | Desktop |

- Max content width: `1540px`, centred with horizontal padding `--size-l` (32px)
- Form container: max-width `640px`, centred on desktop, full-width on mobile
- Header height: ~56px (white bg, `box-shadow: --elevation-01`)

---

## 10. Navigation / Header

| Element | Style |
|---------|-------|
| Background | `#FFFFFF` |
| Logo | `#16181D` fill SVG, centred |
| Nav links | `14px / 400 / #303133` |
| Active / hover link | `color: #E09243` |
| Promo banner | Dark background, white text, amber highlights |
| Nav categories (PL) | Polecane · Kobieta · Dom · Dziecko · Mężczyzna |

---

## 11. Complaint / Return Form — Specific Rules

### Polish copy reference

| Field/action | Polish label |
|---|---|
| Form heading — complaint | "Reklamacja" |
| Form heading — return | "Zwrot" |
| Request type selector | "Rodzaj zgłoszenia" |
| Order number | "Numer zamówienia" |
| Problem description | "Opis problemu" |
| Upload photo | "Dodaj zdjęcie" |
| Submit button | "Wyślij" |
| Required field marker | "Pole wymagane" |
| Success message | "Twoje zgłoszenie zostało przyjęte." |
| Error (validation) | "Proszę uzupełnić wymagane pola." |

### Visual implementation checklist

1. White page background (`#FFFFFF`), dark text (`#303133`)
2. Submit button: amber primary, sharp corners, uppercase — `#E09243`
3. Input borders: `--color-dark-30` (#AFB0B2) at rest, `--color-primary-50` on focus
4. Validation errors: `--color-red-50` (#FF0023) below the field
5. Success confirmation: `--color-green-50` (#0DB209)
6. Body text 14–16px `Euclid`, weight 400; headings weight 600
7. Field gaps: multiples of 16px; label-to-input: 8px
8. Section heading: 16px / 600 / `#18191A`
9. Placeholder text: `#7B7D80`
10. Focus ring: `outline: 2px solid #E09243`
11. AI streaming response area: white card with `--elevation-02` shadow
12. All text and labels in **Polish only**

---

## 12. Accessibility

- Minimum contrast ratio 4.5:1 for body text (WCAG AA)
- Focus indicators visible at all times (`outline: 2px solid #E09243`)
- All interactive elements accessible via keyboard
- Form fields have visible labels (not placeholder-only)
- Error messages associated with inputs via `aria-describedby`
- Image attachments require `alt` text or `aria-label`

---

## 13. Asset References

| File | Description |
|------|-------------|
| `docs/assets/sinsay-logo.svg` | Wordmark SVG (84×31 px) |
| `docs/assets/sinsay-logo.png` | Wordmark raster |
| `docs/assets/sinsay-header.png` | Header screenshot |
| `docs/assets/sinsay-homepage.png` | Homepage viewport |
| `docs/assets/sinsay-favicon.ico` | Favicon |
| `docs/assets/design-tokens.json` | Full CSS custom properties dump |
| `docs/BRANDING.md` | Extended branding notes and stylesheet URLs |
