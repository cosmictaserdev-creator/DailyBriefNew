# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

Use the Gradle wrapper (`./gradlew` on Unix, `gradlew.bat` / `.\gradlew` on Windows PowerShell).

```bash
./gradlew assembleDebug              # build the app
./gradlew installDebug               # build + install on a connected device/emulator
./gradlew test                       # run all JVM unit tests
./gradlew :feature:home:test         # run unit tests for one module
./gradlew :feature:home:testDebugUnitTest --tests "*HomeViewModelTest"          # single test class
./gradlew :feature:home:testDebugUnitTest --tests "*HomeViewModelTest.initial*" # single test method
./gradlew connectedAndroidTest       # instrumented/Compose UI tests (needs device)
./gradlew lint                       # Android lint
```

There is no auto-formatter configured; match surrounding style.

### Required setup
`NEWS_API_KEY` must be set in `local.properties` (untracked). It is read in `app/build.gradle.kts`, exposed via `BuildConfig.NEWS_API_KEY`, and injected as a NewsAPI query param by an OkHttp interceptor (`core/data/.../di/DataModule.kt`). Without it, remote refresh fails and only cached articles show.

## Architecture

Multi-module Android app (NewsAPI-backed "Daily Brief" news reader). Single source of truth is the **Room database**; the network only ever writes into Room, and the UI only ever reads from Room.

### Module graph
```
app  ──► feature:{home, search, saved, onboarding} ──► core:{ui, domain, data, common}
                                                        core:data ──► core:{domain, common}
```
- **core:common** — framework-agnostic primitives: `Resource<T>` (Success/Error result type), `UiText` (Dynamic vs StringResource, for error/text that resolves in the UI), `AppConstants` (all tunable values: categories, page size, animation springs, card/nav dimensions).
- **core:domain** — pure Kotlin: domain models (`Article`, `Category`, `FilterPreferences`) and the `ArticleRepository` interface. No Android deps.
- **core:data** — implements `ArticleRepository`. Retrofit `NewsApi`, Room (`DailyBriefDatabase`, `ArticleDao`, `ArticleEntity`), DTOs, mappers, and the Hilt `DataModule` wiring all of it. Owns the offline-first logic.
- **core:ui** — shared Compose: theme, `FloatingCapsuleNavBar`, motion tokens, shimmer modifier, custom shapes. No feature/business logic.
- **feature:\*** — one screen area each. Each owns a `*Screen` composable + `*ViewModel` + supporting composables. Features depend on core only, never on each other.
- **app** — `MainActivity`, `DailyBreafApp` (Hilt `Application`), navigation host, and `AppModule` (provides `NEWS_API_KEY`).

### Offline-first data flow
`ArticleRepositoryImpl` splits reads from writes:
- `observe*` / `search*` return a **Flow from the DAO** — the reactive read path the UI collects.
- `refreshHeadlines` / `searchRemote` are **suspend** calls that hit the API and `upsertAll` into Room, returning `Resource<Unit>`. They never return data directly; the DAO Flow re-emits.
- So a typical ViewModel collects `observeHeadlines(category)` for display, and separately calls `refreshHeadlines(...)` to repopulate the cache (e.g. on empty cache, pull-to-refresh, or pagination prefetch). On network failure the repository returns `Resource.Error` with a `UiText` message but keeps showing cached rows.
- Saved state is just an `isSaved` boolean column toggled via `toggleSaved(url)`; the Saved screen observes `getSavedArticles()`.

### Presentation pattern (MVI-lite)
Each ViewModel exposes:
- a single immutable `data class *UiState` via `StateFlow` (`_state` / `state.asStateFlow()`),
- a `sealed interface *Action` with one `onAction(action)` entry point that `when`-dispatches,
- state mutations through `_state.update { it.copy(...) }`.

ViewModels are `@HiltViewModel` with constructor-injected repository; composables get them via `hiltViewModel()`. See `feature/home/HomeViewModel.kt` as the reference implementation (note category-switch job cancellation and pagination prefetch via `PREFETCH_THRESHOLD`).

### Navigation
Type-safe Compose Navigation with `@Serializable` route objects in `app/navigation/Route.kt`. `DailyBriefNavGraph` defines destinations; `NavActions.navigateToTab` handles bottom-nav with `popUpTo`/`saveState`/`restoreState`. Article detail is **not** a screen — `Route.ArticleDetail` opens the URL in a Chrome Custom Tab via `LaunchedEffect` then immediately `popBackStack()`. Onboarding gates the whole app in `DailyBreafApp` via local state (not yet persisted), shown before the nav graph + `FloatingCapsuleNavBar`.

## Conventions
- **DI is Hilt** (not Koin), **networking is Retrofit + kotlinx.serialization** (not Ktor), **results use the local `Resource<T>`** (not a `Result<T, E>` wrapper). If a skill suggests Koin/Ktor/Result, defer to what this repo actually uses.
- Source lives under `src/main/kotlin` for library/feature modules; `app` uses `src/main/java`.
- Package root is `com.example.dailybreafrewrite`; each module namespaces under it (`core.data`, `feature.home`, …).
- Logging via Timber.
- Tests: JVM unit tests use JUnit4 runner + Turbine + Mockito + coroutines-test. The established pattern is a hand-written `FakeArticleRepository` (exposing `MutableStateFlow`s) plus `Dispatchers.setMain(StandardTestDispatcher())` in `@Before` / `resetMain()` in `@After`. Follow `HomeViewModelTest` for new ViewModel tests.
- Room schemas are exported to each data-owning module's `schemas/` dir.
