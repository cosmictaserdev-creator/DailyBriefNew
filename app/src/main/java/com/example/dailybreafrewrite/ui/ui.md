# DailyBrief App — Complete AI Design & Development Prompt
*Ultra-detailed screen-by-screen specification for a dark-mode iOS news app*

---

## 0. BRAND & DESIGN SYSTEM

### Identity
- **App name:** DailyBrief
- **Logo:** Stylized "Z" inside a rounded square, white on dark, slightly tilted ~8°. Rendered as an SF Symbol–style glyph.
- **Tagline / voice:** Fast. Curated. Yours.

### Color Tokens (Dark Mode Only)
```
--bg-primary:       #0D0D0D   /* true near-black base */
--bg-surface:       #141414   /* card/sheet surfaces */
--bg-elevated:      #1C1C1E   /* modals, nav pills */
--bg-glass:         rgba(28,28,30,0.72)  /* frosted glass layers */

--card-yellow:      #F5EFA0   /* warm cream-yellow card */
--card-pink:        #F5D0CC   /* blush-pink card */
--card-blue:        #C7D9F5   /* soft sky-blue card */
--card-lavender:    #E0D5F5   /* pale violet card */
--card-mint:        #C8EFE4   /* fresh mint card */

--accent-red:       #FF3B30   /* LIVE badge, alerts */
--accent-white:     #FFFFFF
--text-primary:     #FFFFFF
--text-secondary:   #8E8E93
--text-tertiary:    #636366
--divider:          rgba(255,255,255,0.08)

--shimmer-base:     #1C1C1E
--shimmer-highlight:#2C2C2E
```

### Typography
- **Display / Headlines:** `SF Pro Display` — Bold, weight 700–800, -0.5px letter-spacing. Used for article titles.
- **Body:** `SF Pro Text` — Regular 400 / Medium 500. Line-height 1.55.
- **UI Labels / Tabs:** `SF Pro Rounded` — Semibold 600. Gives the bouncy friendly feel.
- **Timestamps / Meta:** `SF Mono` — Light 300, 11px. Used for "Updated X mins ago."

### Spacing Scale
4 / 8 / 12 / 16 / 20 / 24 / 32 / 48 / 64 px

### Border Radii
- Cards: `24px`
- Pills / Chips: `100px` (fully round)
- Input fields: `16px`
- Bottom-sheet: `28px` top corners only
- Nav capsule: `100px`

### Motion Tokens
```
--spring-snappy:   type=spring, stiffness=400, damping=28, mass=0.9
--spring-bouncy:   type=spring, stiffness=280, damping=22, mass=1.0
--spring-gentle:   type=spring, stiffness=200, damping=30
--ease-out-expo:   cubic-bezier(0.16, 1, 0.3, 1)   duration 320ms
--micro:           duration 120ms, ease-out
```

All transitions default to `--spring-bouncy`. Reserve `--spring-snappy` for swipe-snap, `--spring-gentle` for large modals.

---

## 1. GLOBAL NAVIGATION — iOS 26 Floating Capsule Nav

### Visual Anatomy
A pill-shaped floating bottom navigation bar that hovers 16px above the safe-area bottom edge.

```
┌──────────────────────────────────┐
│  [🏠 Home]  [🔍 Search]  [🔖 Saved] │  ← capsule, ~220px wide
└──────────────────────────────────┘
         ↑ 16px above safe area
```

**Exact Spec:**
- Container: `background: var(--bg-glass)`, `backdrop-filter: blur(24px) saturate(180%)`, `border: 1px solid rgba(255,255,255,0.1)`, `border-radius: 100px`, `padding: 10px 20px`, `box-shadow: 0 8px 32px rgba(0,0,0,0.5), 0 1px 0 rgba(255,255,255,0.06) inset`
- Width: `fit-content`, centered horizontally
- Icons: SF Symbols, size 22px. Active icon fills white with a soft white glow (`filter: drop-shadow(0 0 6px rgba(255,255,255,0.5))`). Inactive icons at 40% opacity.
- Active tab: icon scales to 1.15 with `--spring-snappy`, white dot 4px appears below.
- Tab switch: icons cross-fade + scale simultaneously. Background pill (white, 8% opacity, `border-radius: 100px`) slides to active tab using layout animation — "Magic Move" style.
- Long-press on nav: haptic feedback (medium) + nav capsule does a subtle jiggle (rotate -2° → 2° → 0°, 200ms).

---

## 2. HOME SCREEN — Tinder-style Swipeable Card Stack

### Layout Overview
```
┌─────────────────────────────────┐
│  DailyBrief logo        [⊞ filter] │  ← header
│  Trending  Health  Sports  ...  │  ← horizontal pill tabs
├─────────────────────────────────┤
│                                 │
│    ┌───────────────────────┐    │
│    │   CARD N+1 (peeking)  │    │  ← offset: -8px Y, scale 0.95
│    │  ┌─────────────────┐  │    │
│    │  │  ACTIVE CARD N  │  │    │  ← full visible
│    │  │                 │  │    │
│    │  └─────────────────┘  │    │
│    └───────────────────────┘    │
│                                 │
│         [Like] [Skip]           │  ← action hints (optional)
└─────────────────────────────────┘
         [Floating Nav]
```

### Card Stack Rendering
- **3 cards** rendered at a time:
    - **Card 0 (Active/Top):** full size, `translateY(0)`, `scale(1.0)`, `z-index: 3`
    - **Card 1 (Behind):** `translateY(-10px)`, `scale(0.95)`, `z-index: 2`, slightly visible above active card
    - **Card 2 (Far behind):** `translateY(-18px)`, `scale(0.90)`, `z-index: 1`, barely peeking
- Each card cycles through the color palette: `yellow → pink → blue → lavender → mint → repeat`

### Card Anatomy
```
┌──────────────────────────────┐
│  [LIVE ●]                    │  ← red pill badge, only if breaking
│                              │
│  Demand for Indian           │
│  generic drugs               │  ← SF Pro Display Bold, 26px, dark text on light card
│  skyrockets in...            │
│                              │
│  Updated just now.           │  ← timestamp, SF Mono Light 11px
│                              │
│  [Avatar] Published by       │
│  Wade Warren    [Follow →]   │  ← author row
│                              │
│  The demand for Indian       │
│  generic drugs has shot up   │  ← body preview, 3 lines max, clipped
│  in China amid...            │
│                              │
│  [👍] [🔖] [↗ Share]         │  ← action row, bottom of card
└──────────────────────────────┘
```

**Card Text Colors:** Since cards are light-colored (yellow, pink, etc.), text is `#1A1A1A` (near-black), not white.

**Card Dimensions:** `calc(100vw - 48px)` wide, `auto` height (max ~72% of viewport height). `border-radius: 24px`. `box-shadow: 0 20px 60px rgba(0,0,0,0.4), 0 4px 16px rgba(0,0,0,0.2)`.

### Swipe Mechanics (Buttery Animation)

**Drag Behavior:**
- Attach pan gesture to top card.
- Real-time: card follows finger with `translateX` + `translateY`. Apply rotation: `rotate = translateX / 20` (in degrees). Max rotation ±15°.
- Simultaneously: Card 1 behind scales from `0.95 → 1.0` and `translateY(-10px → 0px)` proportionally as top card moves away. Card 2 scales from `0.90 → 0.95`.
- Resistance: slight rubber-band on Y axis (0.4 damping multiplier).

**Swipe Right (Next Article):**
- Velocity threshold: 500px/s OR displacement >40% screen width.
- Card flies out to the right: `translateX(+150%)`, `rotate(30deg)`, `opacity: 0`. Duration: `--spring-snappy`.
- Card 1 snaps forward to position 0 with `--spring-bouncy` (slight overshoot, settles back).
- Card 2 moves to Card 1 position.
- New card enters at the back (Card 2 position) with a `scale(0.90)` pop-in.
- **Haptic:** medium impact on snap.

**Swipe Left (Previous Article):**
- Previous card slides in from left: `translateX(-100%) → translateX(0)` with `--spring-bouncy`.
- Current top card pushes back to Card 1 position.
- **Haptic:** light impact.

**Release without threshold:**
- Card snaps back to center with `--spring-bouncy` (satisfying rubbery bounce).

**Micro-interactions on drag:**
- At 30% swipe-right threshold: subtle green tint overlay on card (`rgba(52,199,89,0.12)`) — "saving" hint.
- At 30% swipe-left threshold: subtle arrow-left indicator.

### Header
- **Logo:** `DailyBrief` in `SF Pro Rounded Bold 28px`, white. Z icon 32px to the left.
- **Filter button:** `⊞` icon inside a `28px` circle, `bg: var(--bg-elevated)`, `border: 1px solid var(--divider)`. Tap triggers Filter Bottom Sheet (see §2.1).
- Header is NOT sticky — it scrolls away. Only the card stack is the focus.

### Category Tabs (Horizontal Scroll)
```
[Trending ●]  [Health]  [Sports]  [Finance]  [Politics]  [Tech]  →
```
- Each tab: pill shape, `padding: 8px 16px`, `border-radius: 100px`.
- Active: `background: #FFFFFF`, `color: #000000`, weight 600.
- Inactive: `background: var(--bg-elevated)`, `color: var(--text-secondary)`.
- Switching tabs: active pill slides with layout animation (`--spring-snappy`). Cards re-stack with a staggered entrance (Card 0 enters after 0ms, Card 1 after 50ms, Card 2 after 100ms).
- Scroll snapping on tab rail.

### 2.1 Filter Bottom Sheet
**Trigger:** Tap `⊞` filter button.

**Animation:** Sheet rises from bottom with `--spring-bouncy`, slight overshoot then settles. Background dims with `rgba(0,0,0,0.6)` fade.

**Contents:**
```
┌────────────────────────────────┐
│  ●●●  (drag handle, 40x4px)   │
│  Filter News             [✕]  │
│                                │
│  SORT BY                       │
│  ○ Latest First                │
│  ● Trending First              │
│  ○ Most Saved                  │
│                                │
│  SOURCE                        │
│  [BBC]  [Reuters]  [AP]  [+]  │
│                                │
│  DATE RANGE                    │
│  [Today ▾]                     │
│                                │
│  [  Apply Filters  ]           │
└────────────────────────────────┘
```
- Dragging handle: sheet follows finger with rubber-band. Release below 40% height = dismiss with `--spring-snappy`.
- Radio buttons animate with a scale-in fill circle (`--spring-bouncy`).

### Shimmer Loading State
When fetching first batch of articles:
- 3 ghost cards render in the stack, same dimensions as real cards.
- Each ghost has a shimmer sweep: `background: linear-gradient(90deg, var(--shimmer-base) 25%, var(--shimmer-highlight) 50%, var(--shimmer-base) 75%)`, `background-size: 200% 100%`, `animation: shimmer 1.4s infinite ease-in-out`.
- Ghost cards also have the stacked 3D arrangement.
- Category tabs show 5 ghost pills shimmer left to right.

---

## 3. ARTICLE DETAIL SCREEN — Chrome Tab WebView

### Navigation into Article
**Transition animation:**
- Tap the article card OR "Read More" link.
- Card scales up from its current position: `scale(1.0 → 1.05)` then the WebView slides in over it covering the full screen. Use a "hero expand" transition — the card's rounded corners morph from `24px → 0px` as it fills the screen. Duration: 320ms `--ease-out-expo`.

### Screen Anatomy
```
┌────────────────────────────────┐
│  ← Back    [article URL bar]  │  ← top bar, frosted glass
│            [progress bar]     │  ← thin accent line, red/white
├────────────────────────────────┤
│                                │
│     [Chrome Custom Tab /      │
│      SFSafariViewController]  │  ← full screen web content
│                                │
│                                │
├────────────────────────────────┤
│  [👍 Like] [🔖 Save] [↗ Share] │  ← floating bottom action bar
└────────────────────────────────┘
```

**Top bar:**
- `background: var(--bg-glass)`, `backdrop-filter: blur(20px)`.
- Back button: `←` chevron, `SF Pro Rounded Semibold 17px`, white.
- URL bar: pill shaped, `bg: var(--bg-elevated)`, shows truncated domain (e.g., `bbc.com`), lock icon.
- Load progress bar: 2px tall, `background: linear-gradient(90deg, #FF3B30, #FF9F0A)`, animates from 0% → 100% on page load. Disappears after load with fade.

**Bottom action bar:**
- Frosted glass strip: `backdrop-filter: blur(20px)`, `bg: var(--bg-glass)`.
- 3 icon buttons equally spaced. Each has a label below the icon.
- `Save` button: when tapped, icon does a `scale(1 → 1.4 → 1)` bounce + color changes to `accent-red`. A tiny confirmation toast appears: "Saved to your list ✓" — slides up from button, fades after 1.5s.
- `Share` opens native iOS share sheet.

---

## 4. SEARCH SCREEN

### Layout
```
┌────────────────────────────────┐
│  Search                        │  ← large title, SF Pro Display Bold 34px
│  ┌──────────────────────────┐  │
│  │ 🔍  Search news...   [⊞] │  │  ← search input + filter icon
│  └──────────────────────────┘  │
│                                │
│  TRENDING SEARCHES             │  ← section label
│  [India] [Apple] [AI] [Meta]  │  ← horizontally scrolling chips
│                                │
│  RESULTS                       │
│  ┌─ card stack result 1 ─────┐ │
│  └───────────────────────────┘ │
│  ┌─ card stack result 2 ─────┐ │
│  └───────────────────────────┘ │
└────────────────────────────────┘
```

### Search Input
- Input bar: `height: 52px`, `border-radius: 16px`, `background: var(--bg-elevated)`, `border: 1px solid var(--divider)`.
- On focus: border transitions to `rgba(255,255,255,0.3)`, subtle `box-shadow: 0 0 0 3px rgba(255,255,255,0.08)`. Animated with `--micro`.
- Filter icon `⊞` inside input on right end. Tapping opens the same filter bottom sheet as Home.
- Keyboard slides up with standard iOS behavior. Search bar anchors at top.

### Typing Behavior
- Debounce: 300ms after last keypress → fire API call.
- While fetching: shimmer cards appear below.
- Results animate in staggered: first card at 0ms, second at 60ms, third at 120ms — each does `translateY(20px) → translateY(0)` + `opacity: 0 → 1` with `--spring-bouncy`.

### Result Cards (Compact List Style)
Results appear as a **vertical scrollable list** of compact cards — same card design as Home (colored background, bold headline, author) but shorter height (~180px). Cards have the same colored backgrounds cycling through the palette.

**On scroll:** Cards have a subtle parallax inner content shift (headline moves 20% slower than scroll). Elastic overscroll at top/bottom — list stretches and snaps back with `--spring-bouncy`.

### Empty State
If no results:
```
        [🗞️ emoji, 48px]
   No results for "xyz"
   Try a different keyword
   or check your filters.
```
Text centered, `color: var(--text-secondary)`. Emoji does a gentle float animation (up 4px → down 4px, 2s loop, ease-in-out). Appears with `scale(0.8 → 1)` + `opacity` fade, `--spring-bouncy`.

### Shimmer Loading (Search)
- 4 ghost cards appear, each the same compact card height.
- Shimmer sweeps left to right with 150ms stagger between cards.

---

## 5. SAVED NEWS SCREEN

### Layout
```
┌────────────────────────────────┐
│  ← Back                        │
│  Saved News                    │  ← SF Pro Display Bold 34px
│  ┌──────────────────────────┐  │
│  │ 🔍  Search news...       │  │  ← search within saved
│  └──────────────────────────┘  │
│                                │
│  [Stacked card fan display]    │  ← same 3D stack as Home
│                                │
└────────────────────────────────┘
```

### Card Fan / Stack Display
Saved articles displayed as an **overlapping fanned stack** (like physical saved cards on a desk):
- Cards are slightly rotated randomly: Card 0: `rotate(0deg)`, Card 1: `rotate(-3deg)`, Card 2: `rotate(2deg)`, Card 3: `rotate(-1.5deg)`.
- Tap a card: it un-fans, lifts up (`translateY(-20px)`, `scale(1.05)`, shadow intensifies), and becomes active.
- Swipe up on active card: opens article detail.
- Swipe down on active card: dismisses, card returns to fan with `--spring-bouncy`.

### Search Within Saved
Same input spec as Search Screen. Filters the fan stack in real-time — non-matching cards animate away (`scale(0.8)`, `opacity: 0`, `--spring-snappy`), matching cards rearrange.

### Empty Saved State
```
      [🔖 bookmark icon, animated stroke-draw, 56px]
   Nothing saved yet.
   Swipe right on stories
   you want to keep.
```
Bookmark icon draws itself (stroke animation, 600ms, ease-in-out) when this screen is first opened.

---

## 6. INTERESTS ONBOARDING SCREEN

### Layout
```
┌────────────────────────────────┐
│  Select Interests              │  ← SF Pro Display Bold 34px, top 48px
│                                │
│  ┌──────────────────────────┐  │
│  │ ✔  Health                │  │  ← selected: dark filled circle
│  └──────────────────────────┘  │
│  ┌──────────────────────────┐  │
│  │ ○  Technology            │  │  ← unselected: white outline circle
│  └──────────────────────────┘  │
│  ... (Finance, Sports, ...)    │
│                                │
│  [        Submit        ]      │  ← sticky bottom CTA
└────────────────────────────────┘
```

**Background:** Warm blush `#F5E6E0` (light mode for this screen only, as shown in reference). All text in `#1A1A1A`.

**Row animation on select:**
- Checkbox circle scales from `scale(0.8 → 1)` with `--spring-bouncy`.
- Fill color animates inward (radial clip-path: `circle(0% → 50%)`), `--spring-snappy`.
- Row gets a subtle background tint as it's selected.
- A tiny checkmark SVG draws itself (stroke-dashoffset animation, 150ms).

**Submit button:**
- Rounded pill, `height: 56px`, full width minus 32px padding.
- `background: #1A1A1A`, `color: #FFFFFF`.
- Inactive (0 selections): `opacity: 0.4`, non-tappable.
- Active: `opacity: 1`, on press does `scale(0.96)` then release with `--spring-bouncy`.

---

## 7. MICRO-INTERACTIONS CATALOG

| Interaction | Description |
|---|---|
| Card tap feedback | `scale(0.97)` for 80ms, releases with `--spring-bouncy` |
| Follow button | Fills with white, text changes "Follow → Following", scale bounce |
| Like (thumbs up) | Icon scales 1→1.5→1, color goes gray→white, +1 counter ticks up |
| Save bookmark | Icon fills, scale bounce, toast notification from bottom |
| Share button | Icon rotates 20° clockwise and back |
| Category tab select | White pill slides with magic-move layout animation |
| Pull to refresh | Stretch overscroll → spinner appears → cards cascade in |
| Long press card | Card lifts (scale 1.04, shadow intensifies), haptic feedback |
| Nav tab switch | Active dot slides, icons scale, background pill moves |
| Filter tag remove | Tag scales out (`scale → 0`) + fades, remaining tags reflow |
| Error toast | Slides up from bottom, shakes horizontally 2x (error shake), fades |
| App launch | Logo animates in: Z icon drops from above, "News" fades in, cards stack up from below |

---

## 8. PULL-TO-REFRESH

On the Home card stack, dragging downward past 80px:
1. Cards stretch downward (rubber-band elastic effect on Y axis).
2. A custom refresh indicator appears at top: spinning Z logo (rotates 360°, 800ms loop).
3. Release → logo completes rotation → new cards cascade in from top, staggered 80ms each.
4. Light haptic on release.

---

## 9. TOAST / NOTIFICATION SYSTEM

Toasts appear at the **top** of the screen (below status bar), not bottom (bottom is nav territory).

- Container: `border-radius: 16px`, `bg: var(--bg-elevated)`, `border: 1px solid var(--divider)`, `padding: 12px 16px`, max-width 90%.
- Entrance: `translateY(-20px) → translateY(0)` + `opacity: 0 → 1`, `--spring-bouncy`.
- Auto-dismiss after 2.5s: `opacity: 1 → 0`, `translateY(0 → -12px)`, 250ms.
- Types: `success` (white icon), `error` (red icon + shake), `info` (gray icon).

---

## 10. LOADING & SKELETON SCREENS

### Shimmer Spec
```css
@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
.skeleton {
  background: linear-gradient(
    90deg,
    var(--shimmer-base) 25%,
    var(--shimmer-highlight) 50%,
    var(--shimmer-base) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
  border-radius: 8px;
}
```

### Skeleton Layout Per Screen
**Home:** 3 stacked ghost cards (full card height), category tab skeletons (5 pill shapes).

**Search:** Search bar skeleton + 4 compact ghost cards staggered.

**Saved:** Fan of 3 ghost cards with slight random rotations matching real fan.

**Article Detail:** Top bar skeleton + full-bleed image placeholder + text line skeletons (3 widths: 100%, 80%, 60%).

---

## 11. TECHNICAL NOTES FOR DEVELOPERS

### API Integration
- **News source:** `newsapi.org` — use `/v2/top-headlines` and `/v2/everything` endpoints.
- Article detail: Open in `SFSafariViewController` (iOS) / Chrome Custom Tab (Android). Do NOT render HTML manually.
- Pagination: "Infinite" card stack — fetch next page when Card N-3 becomes active.

### Gesture System
- Use `react-native-gesture-handler` (or equivalent) for swipe detection.
- Swipe card: `PanGestureHandler` with `activeOffsetX: [-10, 10]` to prevent accidental triggers on scroll.
- Card animation values: use `Animated.ValueXY` or Reanimated 2 `useSharedValue`.

### State Management
- Current card index, saved articles list, selected interests, active filters — managed in global state (Zustand or Redux Toolkit).
- Saved articles persisted to AsyncStorage / local DB.

### Haptics
- Use `expo-haptics` or `react-native-haptic-feedback`.
- `impactAsync(ImpactFeedbackStyle.Medium)` — card snap.
- `impactAsync(ImpactFeedbackStyle.Light)` — tap interactions.
- `notificationAsync(NotificationFeedbackType.Success)` — save confirmation.

### Accessibility
- All interactive elements: `accessibilityLabel`, `accessibilityRole`.
- Reduce Motion: when `prefers-reduced-motion` is on, replace spring animations with simple `opacity` fades.
- Minimum tap target: 44×44pt (Apple HIG).

---

## 12. SCREEN TRANSITION MAP

```
Splash → Interests Onboarding (first launch only)
                ↓
            Home Screen
           /     |      \
    Tap card  Category  Filter Sheet (modal)
        ↓       Tab
   Article Detail
   (Chrome Tab)
        ↓
     ← Back → Home

Home ──[Nav: Search]──→ Search Screen
                           ↓
                      Tap result → Article Detail

Home ──[Nav: Saved]───→ Saved Screen
                           ↓
                      Tap card → Article Detail
```

All screen-to-screen transitions use the native iOS slide-from-right for push navigation. Bottom sheets use the rise-from-bottom motion defined in §2.1.

---

*End of prompt. This document fully describes all screens, interactions, animations, and technical requirements for the DailyBrief app.*