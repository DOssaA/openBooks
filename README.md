# openBooks

A book-discovery app: search the [OpenLibrary](https://openlibrary.org/developers/api) catalog, open a book to read its details, and save books as **favorites** that persist across sessions.

Built with **Kotlin Multiplatform + Compose Multiplatform**. Only the **Android** target is wired up for this delivery; the architecture lives in `commonMain` so iOS/web can be added later as an additive step.

> **Status:** in active development. Architecture, navigation, dependency injection, and CI are in place; the data layer and feature screens are being built issue-by-issue — see [docs/BACKLOG.md](docs/BACKLOG.md).

---

## Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Compose Multiplatform + **Material 3** (incl. Material 3 Adaptive) |
| Architecture | Clean Architecture + MVVM |
| Navigation | JetBrains **Navigation3** with adaptive list/detail panes |
| DI | **Koin** |
| Networking | **Ktor Client** + kotlinx.serialization |
| Persistence | **Room** (KMP) for favorites |
| Images | **Coil 3** |
| Async/state | Coroutines, **Flow / StateFlow** |
| Testing | kotlin-test, kotlinx-coroutines-test, **Turbine**, **Mokkery**, Kotest |
| Quality | detekt, ktlint, Kover (coverage), lefthook |

---

## Architecture

Clean Architecture with three layers, all in the `shared` KMP module's `commonMain` (so they are platform-agnostic). The only platform seam is Koin's `platformModule` (`expect`/`actual`).

```
Composable → ViewModel → UseCase → Repository → RemoteSource (Ktor) / LocalSource (Room)
```

```
shared/commonMain/com.darioossa.openbooks/
├── data/        BooksRepository · remote/ (Ktor) · local/ (Room)
├── domain/      entities/Book · dataSource/BooksDataSource · use cases
├── presentation/  bookList/ · bookDetail/ · favorites/   (MVVM ViewModels + screens)
├── navigation/  Route · NavigationRoot · NavigationEntries
└── di/          KoinInit · Modules
```

- **Presentation (MVVM):** ViewModels expose UI state as `StateFlow`; Compose collects with `collectAsStateWithLifecycle()`. ViewModels never hold Android `Context` or Compose types, and survive configuration changes. UI state is modelled as sealed classes (loading / success / empty / error).
- **Domain:** pure Kotlin use cases over a `BooksDataSource` interface — no framework dependencies.
- **Data:** `BooksRepository` implements the domain interface and combines a **remote** source (Ktor → OpenLibrary) with a **local** source (Room → favorites).
- **Navigation:** Navigation3 + Material 3 Adaptive renders list and detail side-by-side on large screens automatically.

The domain language (notably **Book = an OpenLibrary _Work_**, identified by a _Work key_) is documented in [CONTEXT.md](CONTEXT.md).

---

## Key technical decisions

Detailed rationale lives in [docs/adr/](docs/adr/):

- **Compose Multiplatform, Android-only target for now** — earns the CMP bonus and keeps the codebase multiplatform-ready, without spending the budget on iOS/web app shells. ([ADR-0002](docs/adr/0002-compose-multiplatform-android-only-target.md))
- **Ktor instead of Retrofit** — the challenge permits justified library substitutions; Retrofit is JVM/Android-only, so Ktor keeps networking in `commonMain` and the data layer multiplatform. ([ADR-0001](docs/adr/0001-ktor-over-retrofit.md))
- **OpenLibrary Search API as the single list source, with an empty initial screen** — one endpoint serves list + search + pagination; the search-driven flow exercises all three required UI states (empty / loading / error). Detail is fetched per-Work. ([ADR-0003](docs/adr/0003-search-api-single-source.md))
- **Manual infinite scroll (via `derivedStateOf`), not Paging 3** — the mandatory requirement is "pagination *or* infinite scroll"; manual paging avoids a heavier integration. Paging 3 is a possible future bonus.
- **Koin over Hilt** — KMP-compatible DI. **Mokkery over Mockito** — KMP-compatible mocking.

---

## Getting started

**Prerequisites:** JDK 17, Android SDK (compileSdk 37), Android Studio (or the Gradle CLI). `minSdk` is 24.

```bash
# Build the Android app
./gradlew :androidApp:assembleDebug

# Install on a connected device/emulator
./gradlew :androidApp:installDebug

# Run shared tests
./gradlew :shared:allTests

# Run static analysis / formatting checks
./gradlew detektAll ktlintAll
```

You can also use the run configurations in Android Studio's toolbar.

---

## Testing

Logic layers (ViewModels, repository) are built **test-first (TDD)**. Tests run in `commonTest` (fast, no Android instrumentation) using `runTest`, Turbine for `Flow`/`StateFlow` assertions, and Mokkery for mocking. `BaseViewModelTest` sets `Dispatchers.Main` to a test dispatcher. Compose UI tests (`createComposeRule`) are a bonus, not part of the core suite.

---

## CI/CD

GitHub Actions, kept lean for fast PR feedback. `main` is the protected, always-green trunk; every change lands via a feature branch and a PR that must pass `pr-checks`.

- **`pr-checks`** (PR gate) — `actionlint`, Gradle wrapper validation, shared quality gates (`detektAll ktlintAll :shared:allTests`), and Android target verification (`:androidApp:lintDebug :androidApp:assembleDebug`). Only the Android target is verified today; the matrix is structured to add iOS later.
- **`ci-main`** — generates Kover coverage reports on the main branches.
- **`security`** — Gitleaks secret scanning and dependency review on PRs.
- **Dependabot** — weekly Gradle and GitHub Actions dependency-update PRs.

Local **lefthook** hooks (`pre-commit` formatting, `pre-push` heavier checks) bring part of this feedback closer to the developer machine; GitHub Actions remains the source of truth.

---

## Roadmap, trade-offs & not yet done

Work is tracked as issue-style items in [docs/BACKLOG.md](docs/BACKLOG.md), each delivered as a single PR. Current trade-offs and pending work:

- **Search-only home screen** — no seeded/trending list on launch; the screen is empty until the user searches (deliberate, see ADR-0003).
- **Android-only** — iOS/web targets are not wired up; the structure makes them an additive step, not a rewrite.
- **No Paging 3 yet** — manual infinite scroll is used; Paging 3 is a possible bonus.
- **Pending:** remote source (Ktor + Search API), favorites persistence (Room), the list/detail/favorites screens and their ViewModels, and rounding the suite out to the required 3–5 meaningful tests.
