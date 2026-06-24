# PRD: DailyBrief — Offline-First News App

## Problem Statement

Users want a modern, fast, and visually rich news reading experience on mobile. Existing news apps are cluttered, slow on startup, and break when offline. Users want a curated, swipeable news feed that works instantly regardless of network conditions, with heavy animation polish and a clean dark-mode design language.

## Solution

DailyBrief is an Android news app that provides a Tinder-style card stack of curated news articles. It is offline-first: the app renders cached content instantly, then refreshes in the background. The UI follows an iOS 26-inspired dark-mode design with a floating capsule navigation bar, spring animations, shimmer loading, and a frosted-glass aesthetic. Articles open in Chrome Custom Tabs, with a lightweight fallback detail screen for offline reading of saved articles.

## User Stories

1. As a news reader, I want to see a stack of article cards on the home screen, so that I can quickly browse headlines in a fun, swipeable format.
2. As a news reader, I want to swipe right to skip an article and advance to the next one, so that I can rapidly scan the feed.
3. As a news reader, I want to swipe left to go back to a previous article, so that I can revisit something I missed.
4. As a news reader, I want the card stack to have a buttery smooth drag-and-snap animation with spring physics, so that the interaction feels polished and responsive.
5. As a news reader, I want the card behind the active card to be visible and scaling up as I swipe, so that the stack feels 3D and tactile.
6. As a news reader, I want to see category pills (Trending, Health, Sports, etc.) horizontally scrollable above the card stack, so that I can filter articles by topic.
7. As a news reader, I want the active category pill to slide with an animated indicator, so that the tab switch feels magic-move smooth.
8. As a news reader, I want a filter button that opens a bottom sheet with sort order, source, and date range options, so that I can refine the feed.
9. As a news reader, I want articles to appear immediately from a local cache on app launch, so that I never stare at a blank loading screen.
10. As a news reader, I want fresh articles to replace cached ones automatically in the background, so that I always have the latest news without manual refresh.
11. As a news reader, I want to pull down on the card stack to manually refresh, with a custom spinning Z logo indicator, so that I can force an update when I want.
12. As a news reader, I want to see shimmer skeleton loading states when the cache is empty or a new category is first loaded, so that I know content is coming.
13. As a news reader, I want to tap a card to open the full article in a Chrome Custom Tab, so that I can read the complete story in a proper browser view.
14. As a news reader, I want the transition from card to article to have a hero expand animation, so that the navigation feels continuous and polished.
15. As a news reader, I want to save (bookmark) an article from the card stack by swiping right or tapping a bookmark icon, so that I can read it later.
16. As a news reader, I want saved articles displayed as an overlapping fanned stack on the Saved screen, so that it feels like a physical collection of clippings.
17. As a news reader, I want to search for articles by keyword on the Search screen, so that I can find news about specific topics.
18. As a news reader, I want search results to appear with staggered entrance animations, so that the experience feels responsive and lively.
19. As a news reader, I want to filter my search by source, date range, and sort order using the same filter sheet as the home screen, so that I can narrow results.
20. As a news reader, I want to see trending search suggestions as horizontally scrolling chips, so that I can explore popular topics.
21. As a news reader, I want to see an empty state with an animated bookmark icon when I have no saved articles, so that I understand how to use the feature.
22. As a first-time user, I want an interests onboarding screen where I select topics I care about, so that the feed is personalized from the start.
23. As a first-time user, I want the selection animation on the onboarding screen to feel delightful (scale-in check circles), so that the first impression is positive.
24. As a user on a tablet, I want the layout to adapt to the larger screen with a multi-pane view, so that I can see more content at once.
25. As a user in landscape, I want the layout to rearrange itself, so that the card stack remains usable on a wider screen.
26. As a user with poor connectivity, I want saved articles to be readable offline, so that I can still catch up on my saved list without internet.
27. As a user, I want a floating bottom capsule navigation bar with three tabs (Home, Search, Saved), so that I can switch between sections with one thumb tap.
28. As a user, I want the nav bar to have frosted-glass styling and active tab animations (icon scale-up, dot indicator), so that the UI feels premium and modern.
29. As a user, I want error states to show a non-intrusive toast at the top of the screen with a shake animation, so that I know something failed without disrupting my flow.
30. As a developer, I want all ViewModels unit-testable with JUnit5 and Turbine using mocked repositories, so that business logic is verified without an emulator.

## Implementation Decisions

### Module Structure
The project follows a multi-module clean architecture layout:

- **`:app`** — DI wiring (Hilt), navigation host, theme application, splash screen, Application class.
- **`:core:common`** — Pure Kotlin module. Constants (`AppConstants`), `Resource<T>` sealed interface, `UiText` sealed interface, extension functions, helper types. Zero Android dependencies.
- **`:core:domain`** — Pure Kotlin module. Domain models (`Article`, `Category`, `FilterPreferences`), repository interfaces (`ArticleRepository`, `FilterRepository`), use cases (if any). Depends only on `:core:common`.
- **`:core:data`** — Room DAOs (`ArticleDao`), Retrofit API interface (`NewsApi`), DTOs (`NewsApiDto`, `ArticleResponse`), entity mappers (`ArticleMapper`), repository implementations, `NewsMediator` class. Depends on `:core:domain` and `:core:common`.
- **`:core:ui`** — Design system composables: `DailyBriefTheme` (color tokens, typography, shapes), `ContinuousRoundedCornerShape`, `ShimmerModifier`, `FloatingCapsuleNavBar`, `ToastHost`, `CardStackLayout`. Depends on `:core:common`.
- **`:feature:home`** — Home screen: `HomeViewModel`, state/actions/events, `HomeScreen` (top-level), `CompactHomeLayout`, `LandscapeHomeLayout`, `TabletHomeLayout`, `CardStack`, `CategoryTabRow`, `FilterBottomSheet`.
- **`:feature:search`** — Search screen: `SearchViewModel`, state/actions/events, `SearchScreen`, `SearchInput`, `SearchResultList`, `TrendingChips`.
- **`:feature:saved`** — Saved screen: `SavedViewModel`, state/actions/events, `SavedScreen`, `SavedCardFan`, `SearchWithinSaved`.
- **`:feature:onboarding`** — Interests onboarding: `OnboardingViewModel`, state/actions/events, `OnboardingScreen`, `InterestRow`, `SubmitButton`.

### Architecture & Data Flow

**Offline-first Mediator pattern:**
- Room is the single source of truth. UI always observes Room via `Flow`.
- On screen load: ViewModel calls `observeArticles()` which returns a `Flow<Resource<List<Article>>>` from Room immediately (fast cache hit).
- ViewModel then calls `refreshArticles()` which launches a coroutine to fetch from the network, save to Room, and let Room re-emit the updated data.
- On network failure: Room data remains visible. ViewModel emits a side-effect event for a non-intrusive top toast ("Couldn't refresh. Showing cached articles.").
- Pagination: Manual threshold at 6 remaining cards triggers next page fetch. Current page index tracked in ViewModel.

**Three-layer model mapping:**
```
NewsApiDto (Retrofit) → ArticleEntity (Room) → Article (domain/UI)
```
Three distinct model classes with dedicated mappers. `Article` is annotated `@Immutable` for Compose stability.

**Resource sealed interface:**
```kotlin
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: UiText, val cachedData: Any? = null) : Resource<Nothing>
}
```

**UiText for error messages:**
```kotlin
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    data class StringResource(val id: Int, val args: List<Any> = emptyList()) : UiText
}
```

### Navigation

- **Library:** `navigation-compose 2.9.8` (stable).
- **Type-safe routes** via `@Serializable` sealed interface. Three bottom tabs (Home, Search, Saved) as a nested nav graph. Article detail is a top-level route that opens Chrome Custom Tab.
- **Floating capsule nav** is a custom composable (`FloatingCapsuleNavBar`) integrated via a custom `Scaffold` setup. Nav bar is hidden on the article detail route.
- **Chrome Custom Tab** for article detail. Fallback to in-app WebView/Compose detail screen if CCT is unavailable or user is offline (saved articles).
- **Deep linking** route pattern `dailybrief://article/{url}` prepared in nav graph for future notification integration.

### UI Strategy Pattern

- **`WindowSizeClass`** (via `material3-adaptive`) drives a `LayoutStrategy` enum: `COMPACT` (phone portrait), `MEDIUM` (phone landscape), `EXPANDED` (tablet).
- `LayoutStrategyResolver` is injectable and testable.
- Each feature screen maps to three layout composables (e.g., `CompactHomeLayout`, `LandscapeHomeLayout`, `TabletHomeLayout`).
- ViewModel is pure — it doesn't know about form factor. Layout strategy is a UI-only concern.

### Card Stack Animation

- `CardStackLayout` composable encapsulates all drag/swipe animation logic.
- Uses `Modifier.pointerInput` for pan gesture detection, `animate*AsState` with spring specs for snap-back and fly-out.
- 3 cards rendered: active (z=3, scale 1.0), behind (z=2, scale 0.95), far behind (z=1, scale 0.90).
- Rotation on drag: `rotate = translateX / 20`, max ±15°.
- Swipe right threshold: velocity > 500px/s OR displacement > 40% width.
- Spring specs match motion tokens from ui.md (`--spring-snappy`: stiffness=400, damping=28, mass=0.9; `--spring-bouncy`: stiffness=280, damping=22, mass=1.0).
- ViewModel exposes `List<Article>`, `currentIndex`, and `onSwipeLeft/Right/Refresh` actions. No animation state lives in ViewModel.

### Theme & Design System

- Dark-mode only color palette as defined in ui.md (bg-primary #0D0D0D, card palette: yellow/pink/blue/lavender/mint, accent-red #FF3B30).
- Typography mapped to Compose `Typography` (SF Pro Display equivalent for headlines, SF Pro Text for body).
- ContinuousRoundedCornerShape (24dp radius) for all cards.
- Custom shimmer modifier with gradient sweep animation (1.4s cycle).
- Motion tokens mapped to Compose `SpringSpec` constants in `AppConstants.Animation`.

### Data Layer

**API (newsapi.org):**
```kotlin
interface NewsApi {
    suspend fun getTopHeadlines(category: String?, country: String, page: Int, pageSize: Int): NewsApiResponse
    suspend fun searchNews(query: String, page: Int, pageSize: Int, sortBy: String): NewsApiResponse
    suspend fun getHeadlinesBySource(sources: String, page: Int): NewsApiResponse
}
```

**Room schema:**
- `articles` table: `url` (PK), `title`, `description`, `content`, `sourceName`, `sourceId`, `author`, `urlToImage`, `publishedAt`, `category`, `isSaved` (boolean), `cachedAt` (timestamp).
- DAO operations: getByCategory (Flow), search (Flow), getSaved (Flow), upsertAll, updateSavedStatus, purgeOldCache.
- `isSaved` flag approach: single source of truth, no separate saved articles table.

### Constants File

`AppConstants` in `:core:common` holds:
- `API_BASE_URL`, `PAGE_SIZE`, `DEBOUNCE_MS`
- `Card.WIDTH_FRACTION`, `Card.CORNER_RADIUS`, `Card.STACK_SCALE_STEP`, `Card.SWIPE_VELOCITY_THRESHOLD`
- `Animation.SPRING_STIFFNESS`, `Animation.SPRING_DAMPING`, `Animation.SHIMMER_DURATION_MS`
- `Dimension.NAV_CAPSULE_HEIGHT`, `Dimension.NAV_CAPSULE_MARGIN_BOTTOM`
- Category names list
- API key in `BuildConfig` (secrets, not in version control)

### Pull-to-Refresh

Uses Material3 `PullToRefreshBox` with custom indicator slot showing the spinning Z logo animation.

### Shimmer Loading

Custom `Modifier.shimmer()` extension using `rememberInfiniteTransition` + `Brush.linearGradient` with colors `#1C1C1E` and `#2C2C2E`, 1.4s cycle, `FastOutSlowInEasing`.

### App Launch Flow

1. System splash screen (Android 12+ SplashScreen API).
2. Check `DataStore` for `onboarding_completed` flag.
3. If not completed → Interests Onboarding screen.
4. If completed → Home screen with animated card entrance.
5. Z logo entrance animation plays as part of Home screen's initial render.

### Floating UI & Notifications

Deferred to post-MVP. When implemented:
- In-app toast system (top of screen, below status bar) for success/error/info states.
- FCM + NotificationCompat for push notifications with "Open Article" and "Save" actions.
- No system-level overlay windows.

## Testing Decisions

### Testing Philosophy
- **One testing seam:** ViewModel tests are the primary and only seam. All business logic, state transitions, and error handling are verified through ViewModel state flows.
- **Test external behavior only:** Assert on state (`Resource.Success(data)` vs `Resource.Error(message)`) and side-effect events (toast channel emission). Never assert on internal ViewModel state or private function calls.
- **No direct DAO/Repository/Mediator tests.** These are exercised indirectly through ViewModel tests with fake implementations.

### What Makes a Good Test
- Given a fake repository with known data, when an action is dispatched, then the ViewModel state should match the expected value.
- Given a network failure in the fake repository, when refresh is triggered, then state should remain as cached data and an error event should be emitted.
- Given user swipes through articles, when the last few cards are reached, then a pagination event should be triggered.
- Tests use `UnconfinedTestDispatcher` for deterministic coroutine execution. State assertions use Turbine.

### Modules Tested
- **`:feature:home`** — ViewModel tests with JUnit5 + Turbine + Mockito.
- **`:feature:search`** — ViewModel tests.
- **`:feature:saved`** — ViewModel tests.
- **`:feature:onboarding`** — ViewModel tests.
- **`:core:domain`** — Use case tests (if use cases are created), domain model invariant tests.
- **`:core:ui`** — Compose UI tests with ComposeTestRule for layout strategies (separate, run on emulator).
- **`:core:common`** — Pure unit tests for `Resource`, `UiText`, `AppConstants` helpers.
- **`:core:data`** — Indirectly covered by ViewModel tests.

### Prior Art
- ViewModel tests follow the standard Turbine pattern: `viewModel.state.test { ... }` with `awaitItem()` assertions.
- Compose UI tests use `ComposeTestRule` with semantic node lookups.
- Mockito is used to create fake repositories injected via Hilt testing utilities.

## Out of Scope

- **Push notifications and FCM** — deferred to post-MVP. The notification infrastructure (FCM, NotificationCompat channels, deep linking) is not included in this PRD.
- **System-level floating UI / overlay** — not planned. All floating UI is in-app only (toasts, banners).
- **User accounts and authentication** — the app has no login/account system. Articles are not synced across devices.
- **Analytics and crash reporting** — not included. No Firebase, Sentry, or similar.
- **Widgets** — home screen widget for Android is out of scope.
- **Video content** — the app is text-news only. No video embeds or player.
- **Multiple languages / i18n** — English only. String resources are in a single `strings.xml`.
- **Performance benchmarking** — no formal benchmark suite, but recomposition is minimized via `@Immutable` models and `graphicsLayer` for GPU-only animations.
- **`navigation3` migration** — staying on `navigation-compose 2.9.8`. The `navigation3` dependency is dormant.

## Further Notes

- The design spec (`ui.md`) in the repo contains the full visual reference. Design assets (reference images) are in `app/src/main/java/com/example/dailybreafrewrite/ui/`.
- The `capsule` library (`io.github.kyant0:capsule`) is in the deps for potential bottom sheet/adaptive UI use but its adoption is TBD during implementation.
- Room schema exports are configured via `schemaDirectory("$projectDir/schemas")` for migration testing.
- Kotlin serialization is used everywhere (Retrofit converter, Room type converters, nav route serialization).
- AGP 9.2.1 with Kotlin 2.2.10 and Compose BOM 2026.02.01 — all modern, no legacy concerns.
