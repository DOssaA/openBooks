# openBooks — Agent Briefing

## What this project is

**openBooks** is a book-discovery app: the user **searches** the [OpenLibrary](https://openlibrary.org/developers/api) catalog, opens a book to read its details, and saves books as **favorites** that persist across sessions. It is the delivery for a Grupo Mariposa Android (semi-senior/senior) technical challenge.

It is built with **Compose Multiplatform**, but **only the Android target is wired up** for this delivery (see [ADR-0002](docs/adr/0002-compose-multiplatform-android-only-target.md)). The architecture lives in `commonMain` so iOS/web can be added later as an additive step.

> **Timeline reality:** ~1 day of build time remains against this delivery. Scope and sequencing below are tuned for that. The brief explicitly values *quality over quantity — finish what you deliver well*.

For domain language (Book vs Work, Favorite, Work key, etc.) see [CONTEXT.md](CONTEXT.md). For the "why" behind library deviations from the challenge, see [docs/adr/](docs/adr/).

---

## Confirmed plan & scope (read this first)

| Decision | Choice | Source |
|---|---|---|
| Platforms | CMP, **Android target only** for now | [ADR-0002](docs/adr/0002-compose-multiplatform-android-only-target.md) |
| Networking | **Ktor Client** (not Retrofit), in `commonMain` | [ADR-0001](docs/adr/0001-ktor-over-retrofit.md) |
| Persistence | **Room (KMP, 2.7+)** for favorites — mandatory, kept as-is | challenge |
| DI | **Koin** (chosen over Hilt for KMP) | challenge allows either |
| Images | **Coil 3** (KMP) for covers | bonus |
| Data source | **Search API only** — one list+search+pagination endpoint; **Works API** for detail | [ADR-0003](docs/adr/0003-search-api-single-source.md) |
| List UX | **Empty initial screen + search bar.** No list until the user types. | agreed |
| Pagination | **Manual infinite scroll** via `derivedStateOf` over `LazyListState`. **No Paging 3.** | agreed |
| Favorites list | Plain `Flow<List<Favorite>>` from Room → `LazyColumn`. **Not paged.** | agreed |
| Workflow | **TDD** for ViewModels / repository / list-screen logic; **one PR per build-order step**, each must pass CI; start on `feature/bookList`. | agreed |

### How the Search API satisfies three requirements at once
- **List** = search results.
- **Remote search** = the query box (native to `/search.json?q=…`).
- **Pagination** = `page`/`limit` on the same endpoint.
- **All three states** fall out of one flow: blank query → **empty** ("search for a book"); query in flight → **loading**; zero hits → **empty** ("no results"); failure → **error**.

Detail screen passes the **Work key** and fetches `/works/{key}.json` for the full description (Search returns only thin metadata). Two endpoints total.

---

## Architecture

Clean Architecture, all in the `shared` KMP module, all in `commonMain` except the Koin `platformModule` seam.

```
shared/commonMain/com.darioossa.openbooks/
├── data/
│   ├── BooksRepository            ← implements BooksDataSource; merges remote search + local favorites
│   ├── remote/  (network)         ← Ktor client, OpenLibrary DTOs, BooksRemoteSource impl
│   └── local/   (database)        ← Room entity/DAO/db, BooksLocalSource impl
├── domain/
│   ├── dataSource/BooksDataSource
│   ├── entities/Book
│   └── (use cases: search, getBookDetail, toggleFavorite, observeFavorites)
├── presentation/
│   ├── bookList/   (search list)
│   ├── bookDetail/ (NEW — does not exist yet)
│   └── favorites/  (NEW — does not exist yet)
├── navigation/  (Route, NavigationRoot, NavigationEntries)
└── di/          (KoinInit, Modules)
```

Dependency flow: `Composable → ViewModel → UseCase → Repository → RemoteSource / LocalSource`.

> ⚠️ **Package bug:** `data/local` and `data/remote` are currently **swapped** — the network source sits in `data.local` and the DB source in `data.remote`. Fixing this is PR-step 1.

### DI (Koin)
`presentationModule` (ViewModels) · `dataModule` (repository + sources) · `domainModule` (use cases) · `platformModule` (`expect`/`actual`; will hold the Android Room DB builder). Started in `MainApp.onCreate()` via `initKoin { androidContext(); androidLogger() }`.

### Navigation
JetBrains **Navigation3** + **Material 3 Adaptive** `ListDetailSceneStrategy` (list/detail/extra panes adapt on large screens). Routes: `BooksList` (list pane), `BookDetail(id)` (detail pane), `FavoritesList` (extra pane). Navigation = mutating the `mutableStateListOf` backstack. To open detail, push `BookDetail(workKey)`.

---

## The `Book` entity must be reshaped

Current `Book(title, description)` is **broken** for this app: no id for navigation, no key for favorites, no cover, no author. Target shape:

```kotlin
data class Book(
    val key: String,            // OpenLibrary Work key (e.g. "OL45804W") — id for nav + favorites
    val title: String,
    val authors: List<String>,
    val coverUrl: String?,      // derived from cover_i
    val firstPublishYear: Int?,
    val description: String?,   // null until the detail fetch populates it
)
```

A favorited Book is stored locally with enough to render the Favorites screen **offline** (key, title, author, cover) — no network re-fetch. See [CONTEXT.md](CONTEXT.md).

---

## State model needs extending

Current `ListState` (`Loading | Success | Error`) is missing the states the new UX needs. Target:

```kotlin
sealed interface ListState {
    data object Idle : ListState              // blank query — initial empty screen
    data object Loading : ListState           // first page in flight
    data class Success(
        val books: List<Book>,
        val loadingMore: Boolean,             // appending next page
        val endReached: Boolean,
    ) : ListState
    data object Empty : ListState             // query returned zero results
    data object Error : ListState
}
```

`BooksListViewModel` exposes `StateFlow<ListState>` plus a query input. Consume with `collectAsStateWithLifecycle()` — **never** `collectAsState()`.

---

## Workflow conventions (how to build this)

- **GitHub-PR workflow.** `main` is the protected, always-green trunk. One **feature branch per build-order step**, pushed to GitHub as a **real PR** into `main` that must pass `pr-checks` before a squash-merge. Put the issue's acceptance criteria in the PR body. Budget ~5–10 min CI per PR; work the next branch while it runs. **PR #1 carries the existing scaffolding** (all current work is uncommitted on `feature/bookList`), so it bundles Issues 1–2; go granular from Issue 3 onward. Front-load the dependency PRs (Room, Ktor) since new deps + KSP are the most likely to break a clean-machine build. Tightly-coupled items may share a PR rather than artificially splitting.
- **TDD the logic layers** — ViewModels, repository, and the list-screen logic. Write/adjust the failing test first, then implement to green. The plumbing already exists: `BaseViewModelTest` (sets `Dispatchers.Main`), Turbine (`.test {}`), Mokkery (`mock`, `everySuspend`, `verifySuspend`), Kotest. `@OpenForTest` + the `allOpen` plugin makes classes mockable only under test tasks.
- **UI tests are bonus.** True Compose UI tests (`createComposeRule`) are Android-instrumented and costlier in KMP — the *logic* of the list screen is tested through its ViewModel. Add one Compose UI test only if time remains; do not block a PR on it.
- No business logic in `@Composable`s · ViewModels never hold `Context`/Compose types · state via sealed classes, not loose booleans · run `./gradlew detektAll ktlintAll` before committing (lefthook enforces locally; GitHub Actions is source of truth) · dependabot PRs are low priority — ignore for now.

### Build order (hardest-risk-first, with a cut line)

1. **Fix the existing tests + ViewModel as a *re-spec*, not a wiring fix.** The current `BooksListViewModelTest` asserts auto-load-on-init (`Loading → Success`) — that's the **discarded** behavior. Rewrite it for search-driven flow (`Idle → query → Loading → Success/Empty/Error`); turn `GetBooksUseCase` into a query-taking search use case; wire it into the VM. *(current branch)*
2. **Room-KMP spike** — Favorite entity + DAO + DB builder + `expect`/`actual` in `platformModule`; prove persistence on Android. **Highest time-risk; do it early.**
3. Reshape `Book` (+`key`, cover, authors).
4. Ktor + Search API + DTO→`Book` mapping in the (corrected) remote source.
5. `BooksListViewModel` — debounced query, search, manual paging, full state model, favorite toggle. *(TDD)*
6. `BooksListScreen` — search bar + list + Coil covers + state handling.
7. `FavoritesScreen` + VM — observe Room flow. *(TDD the VM)*
8. `BookDetailScreen` + VM — fetch Work, show description, favorite toggle. *(TDD the VM)*
9. Round out tests to 3–5 meaningful ones (VM + repository).
10. README (install/run, decisions, trade-offs, not-done) **and delete the false iOS/JS/Wasm CI claims** (see Known issues).

**— Cut line — below is "only if time remains":** Paging 3 · Compose UI tests · animations (`AnimatedVisibility`, `animateContentSize`) · dark theme / dynamic color (Material You).

---

## Testing approach

| Tool | Purpose |
|------|---------|
| `kotlinx-coroutines-test` | `runTest`, `StandardTestDispatcher` |
| Turbine | `StateFlow`/`Flow` assertions (`.test {}`) |
| Mokkery | KMP-native mocking |
| Kotest | assertions |
| `BaseViewModelTest` | sets `Dispatchers.Main` to a test dispatcher |

---

## Known issues / cleanup owed

- [ ] `data/local` ↔ `data/remote` packages are swapped (PR-step 1).
- [ ] Existing `BooksListViewModelTest` encodes discarded auto-load behavior — **re-spec for search**, don't just wire it green.
- [ ] `Book` entity lacks `key`/cover/author — reshape required.
- [ ] `ListState` lacks `Idle`/`Empty`/`loadingMore` — extend.
- [ ] `BooksListViewModel.init` is empty; `BookListScreen` body is empty.
- [ ] Detail and Favorites screens + ViewModels do not exist yet.
- [ ] **README falsely claims iOS/JS/Wasm CI and `iosMain`/`jvmMain` targets that don't exist** — correct it (only Android is wired up).
- [ ] No Ktor / Room / Coil dependencies in `libs.versions.toml` yet.

---

## Key versions

Kotlin 2.4.0 · Compose Multiplatform 1.11.1 · AGP 9.2.1 · Koin 4.2.2 · Navigation3 1.1.1 (JetBrains) · Material3 Adaptive 1.3.0-beta02 · Mokkery 3.4.1 · Turbine 1.2.1 · **minSdk 24** · compileSdk 37. *(Ktor, Room, Coil to be added.)*

---

## CI

`.github/workflows/` (each step PR must pass `pr-checks` before merge to the green `main` trunk):
- **`pr-checks`** — 4 parallel jobs: `actionlint`, Gradle wrapper validation, `shared-quality` (`detektAll ktlintAll :shared:allTests`), `target-verification` (`:androidApp:lintDebug :androidApp:assembleDebug`). **Only the Android target is verified** — the matrix has a commented placeholder for iOS; there is no JS/Wasm/iOS verification despite older README prose (now corrected).
- **`ci-main`** — Kover coverage (`koverXmlReport koverHtmlReport`) on `main`/`master`/`develop`.
- **`security`** — Gitleaks secret scan + dependency review on PRs.
- **Dependabot** — weekly Gradle + GitHub Actions update PRs (ignored during this sprint).

detekt + ktlint via `build-logic` convention plugins; **lefthook** for local `pre-commit`/`pre-push` hooks.
