# DailyBrief — Issue Breakdown

Vertical slices in dependency order. Each slice cuts through all layers (schema, API, UI, tests) and is independently demoable.

---

## 1. Core Infrastructure
**Blocked by:** None — can start immediately

Set up multi-module project (`:core:common`, `:core:domain`, `:core:data`, `:core:ui`, feature stubs). AppConstants, Resource sealed interface, UiText, theme (colors, typography, motion tokens), ContinuousRoundedCornerShape, shimmer modifier. App compiles, theme renders a placeholder screen.

---

## 2. Navigation Shell
**Blocked by:** 1

Type-safe routes (`@Serializable` sealed interface), NavHost with 3 nested tab graphs, `FloatingCapsuleNavBar` composable with frosted-glass styling, active-tab animation, nav bar visibility logic (hidden on article detail).

---

## 3. Data Layer — API + Room + Domain Model
**Blocked by:** 1

Retrofit NewsApi interface (top-headlines, everything), Room schema (articles table with isSaved flag), DTO/Entity/Domain models, three-layer mappers, ArticleDao, NewsMediator with offline-first cache-then-network strategy.

---

## 4. Home Screen — Card Stack UI (Mock Data)
**Blocked by:** 1, 2

CardStackLayout with 3-card render, spring drag/swipe/snap animations, rotation on drag, threshold fly-out, card color cycling, action row (save, share). HomeViewModel with mock data. Category pills as LazyRow with animated active pill. Full ViewModel test coverage.

---

## 5. Home Screen — API Integration + Offline-First
**Blocked by:** 3, 4

Wire NewsMediator into HomeViewModel. Pull-to-refresh with custom Z logo indicator. Shimmer skeletons on first load. Pagination at 6-card remaining threshold. Offline banner toast on network failure.

---

## 6. Search Screen
**Blocked by:** 3, 2

SearchInput with debounce (300ms), search results as compact card list with stagger entrance animation, trending search chips, empty state, error state, shimmer on fetch. SearchViewModel with full Turbine test coverage.

---

## 7. Saved Screen
**Blocked by:** 3, 2, 4

Bookmark action on Home cards (swipe-right gesture + bookmark icon tap, with bounce animation and toast). SavedCardFan composable (overlapping rotated cards). Search within saved. Offline reading fallback compose detail screen. SavedViewModel tests.

---

## 8. Interests Onboarding + Launch Flow
**Blocked by:** 1, 2

Onboarding screen with interest selection rows, animated check circles, warm background, submit button. DataStore for `onboarding_completed` flag. Splash → onboarding (first launch) → Home. OnboardingViewModel tests.

---

## 9. Filter Bottom Sheet
**Blocked by:** 1

Shared FilterBottomSheet composable (sort order, source chips, date range). Sheet spring animation, rubber-band drag dismiss. FilterPreferences model. Integrated into Home and Search. FilterViewModel tests.

---

## 10. Adaptive Layouts
**Blocked by:** 5, 6, 7

WindowSizeClass integration, LayoutStrategy enum (COMPACT/MEDIUM/EXPANDED), LayoutStrategyResolver. Three layout composables per feature. Compose UI tests per layout variant.
