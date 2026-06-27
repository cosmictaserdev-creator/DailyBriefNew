# PRD: Home Card Deck + iOS-Style Reading Sheet

> Triage: `ready-for-agent`. Tracker: cosmictaserdev-creator/DailyBriefNew.
> Status: Ready. Phased delivery (1 → 2 → 3).

## Problem Statement

As a reader using Daily Brief, the Home feed feels static and the reading experience feels broken in two ways:

1. The card stack only animates the top card. When I drag it, the cards behind sit frozen, so the deck feels lifeless and I can't tell that "the next story is coming up." It doesn't feel like the smooth, physical iOS-style card deck I want.
2. When I tap an article, the app throws me out into a separate full-screen Chrome tab that looks like a completely different app. It's jarring, and there's no graceful experience when I'm offline — even though this is supposed to be an offline-first app.

I want the Home deck to feel alive and tactile, and I want reading an article to feel like an in-app iOS bottom sheet that respects the offline-first promise.

## Solution

Two connected experiences, delivered in three phases:

1. **A living card deck.** Dragging the top card makes the card directly behind it scale up and rise toward the front, proportional to how far I drag — "depth promotion." Swiping is navigation, not judgment: **swipe left = next story**, **swipe right = previous story** (the previous card flies back in and re-stacks on top). At the start of the deck a backwards swipe rubber-bands; near the end a forward swipe quietly loads more so it feels endless.

2. **A redesigned card** that matches the pastel mockups using only data the news source actually provides — colored card, title, source, time-ago, image, plus working bookmark and share. No fabricated badges, avatars, follow buttons, or like counts.

3. **An in-app iOS reading sheet.** Tapping any article (from Home, Search, or Saved) opens a bottom sheet that renders the real web page inside it, with a grabber, rounded top, drag-to-dismiss, and the app behind scaling/dimming like an iOS card-stack presentation. Offline or on load failure, the same sheet shows a native fallback built from cached data instead of a broken page.

## User Stories

### Card deck — navigation & feel
1. As a reader, I want the card directly behind the top card to scale up and rise as I drag the top card away, so that the deck feels alive and I can see the next story coming up.
2. As a reader, I want the top card to translate and tilt as I drag it, so that the swipe feels physical and responsive.
3. As a reader, I want a left swipe to advance to the next story, so that I can move forward through the feed with one gesture.
4. As a reader, I want a right swipe to bring back the previous story, so that I can revisit something I just passed without hunting for it.
5. As a reader, I want a partial drag that I release to spring back into place, so that I don't accidentally change stories when I only nudge the card.
6. As a reader, I want completing a swipe to leave the next (or previous) story already settled on top, so that navigation feels continuous rather than stuttery.
7. As a reader, when I'm on the very first story and swipe to go back, I want the card to rubber-band with a bounce, so that I get tactile feedback that there's nothing before it.
8. As a reader, when I approach the end of the loaded stories and swipe forward, I want more stories to load automatically, so that the feed feels infinite and I'm never stranded.
9. As a reader, I want the deck to remember which story was on top after I rotate the device or the app is restored from the background, so that I don't lose my place.
10. As a reader, I want the swipe and depth-promotion to stay smooth (no stutter) even while images load, so that the experience feels premium.

### Card visual
11. As a reader, I want each card to have a soft pastel color, so that the feed looks warm and distinct like the mockups.
12. As a reader, I want a card to keep the same color as it rises through the stack, so that the deck doesn't visually glitch during a swipe.
13. As a reader, I want to see the article title, source, and how long ago it was published, so that I can judge relevance at a glance.
14. As a reader, I want a representative image on the card when one exists, so that the card is visually engaging.
15. As a reader, I want a graceful placeholder when an article has no image, so that imageless cards still look intentional.
16. As a reader, I want a bookmark control on the card, so that I can save a story to read later.
17. As a reader, I want the bookmark state to reflect whether the story is already saved, so that I know its status without opening it.
18. As a reader, I want a share control on the card, so that I can send a story to someone via the system share sheet.

### Reading sheet
19. As a reader, I want tapping a card to open the article in an in-app bottom sheet, so that I stay in the app instead of being thrown into a separate browser.
20. As a reader, I want the sheet to render the real web article inside it, so that I can read the full story, not a truncated snippet.
21. As a reader, I want the sheet to have a grabber, rounded top, and drag-to-dismiss, so that it feels like a native iOS sheet.
22. As a reader, I want the app behind the sheet to scale down, round its corners, and dim, so that the presentation feels like the iOS card-stack style.
23. As a reader, I want to drag the sheet down to dismiss it and return to exactly where I was, so that browsing feels uninterrupted.
24. As a reader, I want to scroll the article without accidentally dismissing the sheet, and to dismiss the sheet only when the article is already scrolled to the top, so that reading and dismissing never fight each other.
25. As a reader, I want the sheet to open without a visible hitch on the first tap, so that the experience feels instant.
26. As a reader on a slow connection, I want heavy pages to still render as well as they would in Chrome, so that the in-app sheet isn't a downgrade.
27. As an offline reader, I want tapping a story to still open the sheet and show a clean native view built from the cached title, image, source, time, and snippet, so that the app stays useful without a connection.
28. As an offline reader, I want a Retry / open-full-article action in the fallback, so that I can load the full page once I'm back online.
29. As a reader, I want to open articles from Search and Saved into the same reading sheet, so that the reading experience is consistent everywhere.

## Implementation Decisions

### Card deck mechanics
- **Animation model: depth promotion.** As the top card's drag offset grows, the card in the next slot interpolates its scale and vertical offset toward the front slot. The top card applies `translationX`, `rotationZ` (tilt), and alpha. All reactions are computed inside `graphicsLayer` lambdas reading the live drag value — no per-frame recomposition.
- **Swipe is navigation, not selection.** Left = next, right = previous. The previous card animates in from the leading edge and re-stacks on top; the current card drops back into the stack.
- **`HomeAction` changes.** Replace the symmetric `SwipeLeft`/`SwipeRight` (both currently advance) with directional **Next** and **Previous** intents. `ToggleSaved`, `Refresh`, filter, and category actions are retained.
- **State ownership split (performance-critical).**
  - The discrete **`topIndex` is hoisted into `HomeViewModel`** as the single source of truth. It drives prefetch and is persisted via `SavedStateHandle` so it survives configuration change and process death.
  - The **live drag offset stays a local `Animatable`** inside the deck composable. Back cards read it via `graphicsLayer`. The ViewModel never sees per-frame drag values.
  - This removes the current dual-source bug where `CardStackLayout` owns `currentIndex` while `HomeViewModel` separately counts `swipedCount`.
- **Boundaries.**
  - At `topIndex == 0`, a Previous gesture rubber-bands (spring back, no index change).
  - When remaining loaded articles fall below the existing prefetch threshold on a Next gesture, the existing `refreshHeadlines(nextPage)` pagination path is triggered.
- **Scope:** the prev/next deck is **Home-only**. Search keeps its results list and Saved keeps its fan stack.

### Card visual
- Pastel color drawn from the existing `CardColorCycle` palette, **assigned stably per article** (derived from the article identity / list position, not the stack slot) so color does not change as a card is promoted.
- Card content is limited to fields the news source actually returns: title, source name, time-ago, image (with placeholder fallback), plus a **bookmark** control wired to the existing `toggleSaved` repository call and a **share** control using the Android system share intent with the article URL.
- **Explicitly omitted** (no backing data, would be dead/misleading UI): LIVE badge, author avatar, Follow button, like counts. This is a deliberate honesty-over-fidelity decision; the card will look plainer than the mockup.

### Reading sheet
- Replaces the current `Route.ArticleDetail` navigation destination and its immediate `CustomTabsIntent` launch. **That route and the Custom Tab code are removed.**
- The sheet is an **app-level overlay** hosted above the navigation bar so the entire app can scale/dim behind it. The app content host (currently `DailyBreafApp` in `MainActivity`) becomes the scalable container.
- A **shared app-level sheet state-holder** owns: which article is open, open/dismiss state, and the **content decision** — online ⇒ render embedded WebView; offline or load failure ⇒ render native fallback from cached article fields. Home, Search, and Saved all trigger it through a shared callback rather than each navigating independently.
- **Engine: embedded Android WebView** inside a Compose sheet with custom grabber, rounded top, ~92% single-height detent, and drag-to-dismiss. iOS-style scale/dim of the app behind.
- **Lag mitigations are in scope and required:**
  - Nested-scroll coordination so the sheet only drags when the WebView is scrolled to the top; otherwise the page scrolls.
  - A pre-warmed / reused WebView instance to eliminate the first-open instantiation hitch.
  - Hardened WebView settings (no unnecessary JS-to-native bridges, no file access).
  - Heavy news pages remain as heavy as in Chrome — same engine, not a regression.
- **Offline fallback** renders title, image, source, time, and the cached snippet, plus a Retry / open-full-article action that loads the WebView when connectivity returns.

### Sequencing
Phased, each phase independently runnable and testable, in order:
1. Card-stack mechanics (depth promotion, prev/next, ViewModel `topIndex`, boundaries).
2. Card visual redesign (pastel + real-data row).
3. WebView reading sheet (app-level overlay, nested-scroll, pre-warm, offline fallback, scale-behind).

## Testing Decisions

**What makes a good test here:** assert externally observable behavior (resulting state and emitted UI state), not animation internals or Compose layout details. Drag math, depth-promotion interpolation, WebView rendering, nested-scroll, and scale-behind are visual/interactive and verified by eye or, optionally, instrumented tests — not unit-tested.

**Two seams, both at the highest sensible point, both reusing the project's established `ViewModel` + fake-repository + Turbine pattern (see `HomeViewModelTest`):**

1. **`HomeViewModel` (existing seam) — deck logic.**
   - Next advances `topIndex`; Previous decrements it.
   - Previous at `topIndex == 0` is a no-op (rubber-band has no state effect).
   - Next near the end of loaded articles triggers the pagination prefetch path.
   - `topIndex` is restored from `SavedStateHandle`.
   - Prior art: existing `HomeViewModelTest` with `FakeArticleRepository` exposing `MutableStateFlow`s, `Dispatchers.setMain(StandardTestDispatcher())` / `resetMain()`.

2. **New app-level reading-sheet state-holder — content decision.**
   - `open(article)` while online ⇒ state resolves to the web/loading content.
   - `open(article)` while offline or on load failure ⇒ state resolves to native fallback carrying the cached article fields.
   - Dismiss returns to the closed state.
   - Prior art: same ViewModel-style unit test pattern as above, with a fake connectivity/source input.

**Phase 2** introduces no new unit seam. If a `timeAgo` formatting helper does not already exist in `core:common` extensions, it is a small pure function and may be unit-tested directly.

## Out of Scope

- Any fabricated card chrome: LIVE badge, author avatars, Follow relationships/buttons, like counts or like functionality.
- Prev/next deck swiping on Search or Saved — those screens keep their existing list and fan layouts.
- Two-detent (half → full) sheet interaction; the reading sheet has a single ~92% detent.
- Wrap-around / looping at the deck ends, and hard-stop boundary behavior (rubber-band + prefetch is the chosen model).
- A full native article reader for online reading (online always uses the WebView); the native view is only the offline/failure fallback.
- Downloading/caching full article web pages for offline reading (offline shows cached metadata + snippet only).
- Persisting "Follow"/subscription state or any social features.
- Changes to onboarding, category pills, or the filter sheet beyond what the deck rewrite necessarily touches.
- Instrumented/Compose UI tests (may be added later but are not required by this PRD).

## Further Notes

- Tunable defaults intentionally left to implementation (not blocking): exact tilt magnitude and spring constants (reuse the existing `AppConstants.Animation` springs), the precise per-article color-assignment formula, share-intent formatting, time-ago formatting, and the WebView pre-warm lifecycle location.
- Domain alignment: "save" maps to the existing `toggleSaved` / `isSaved` repository contract and the `isSaved` Room column; no schema changes are required for this feature.
- Offline-first is a hard constraint throughout: the deck reads from Room (the single source of truth) and the reading sheet must degrade to cached data rather than a network error.
- Structural consequence to remember: removing `Route.ArticleDetail` and the `CustomTabsIntent` path, and converting the app content host into a scale-capable container that sits beneath the global sheet overlay.
- Published to the cosmictaserdev-creator/DailyBriefNew issue tracker with the `ready-for-agent` label.
