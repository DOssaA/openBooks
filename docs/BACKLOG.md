# openBooks — Delivery Backlog

GitHub-issue-style backlog for finishing the challenge. Each issue = **one pull request** that must pass `pr-checks` CI before merge. Ordered hardest-risk-first. Logic layers (ViewModels, repository, list-screen logic) are built **test-first (TDD)**.

See [agents.md](../agents.md) for architecture, [CONTEXT.md](../CONTEXT.md) for domain language, and [docs/adr/](adr/) for locked decisions.

## Working agreements

**GitHub-PR workflow** — `main` is the protected, always-green trunk:

- One **feature branch per issue**, named `feature/issue-<n>-<slug>` (e.g. `feature/issue-1-search-list`), pushed to GitHub; open a **real PR** into `main`. The PR must pass `pr-checks` before merge; squash-merge to keep history clean.
- Put the issue's acceptance criteria in the PR body so intent is reviewable (solo self-merge is fine).
- `pr-checks` runs four parallel jobs — `actionlint`, Gradle wrapper validation, `shared-quality` (`detektAll ktlintAll :shared:allTests`), and `target-verification` (`:androidApp:lintDebug :androidApp:assembleDebug`). Budget **~5–10 min** wall-clock per PR; work the next branch while CI runs.
- **PR #1 carries the existing scaffolding.** All current work sits uncommitted on `feature/bookList`, so PR #1 ≈ scaffolding + Issue 1 + Issue 2. Go granular only from Issue 3 onward — don't retro-split the scaffolding.
- **Front-load the dependency PRs** (Room Issue 3, Ktor Issue 5): new deps + KSP are the most likely to break a clean-machine `assembleDebug`/`allTests`. Don't stack PRs on an unverified one.

**Other agreements:**

- TDD the logic: write/adjust the failing test first, then implement to green.
- **Dependabot PRs are low priority — ignore them for now.**
- Compose UI tests (`createComposeRule`) are bonus, never a merge blocker.
- `./gradlew detektAll ktlintAll` clean before committing (lefthook enforces locally; GitHub Actions is source of truth).
- Stop at the **cut line** if the day runs short; everything below it is optional bonus.

---

## Issue 1 — Search-driven ViewModel + re-spec tests (empty initial state)
**Labels:** `test` `feature` `priority:1` · **Branch:** `feature/issue-1-search-list` (current) · **Depends on:** —

Replace the discarded auto-load-on-`init` behaviour with a **reactive, search-driven** ViewModel and re-spec its tests. PR #1 also carries the scaffolding + Issue 2 (package swap).

**Status: ✅ done.** VM is reactive; 4 tests green; `detektAll ktlintAll :shared:allTests :androidApp:lintDebug assembleDebug` all pass locally.

**Acceptance criteria**
- [x] Initial state is `Empty` (no network call on `init`).
- [x] Use case takes a **query** (`SearchBooksUseCase`).
- [x] **Reactive VM:** `MutableStateFlow<query>` → `debounce` → `flatMapLatest { searchBooks }` (auto-cancels stale searches) → `map` to `ListState` → `stateIn(WhileSubscribed(5s))`. No manual `Job`.
- [x] Test: empty query → stays `Empty`; non-empty → `Loading → Success(list)`.
- [x] Test: query → `Loading → Error` on failure.
- [x] Test: query with zero results → `Empty`.
- [x] Tests green.

**Emergent fixes folded into PR #1 (not originally listed):**
- **Case bug:** the `androidMain` tree was `com/darioossa/openBooks/` (capital B) with `MainApp.kt` in `package …openBooks.di`; on case-insensitive macOS the compile jar then broke case-sensitive classpath resolution for `…openbooks` imports. Renamed to lowercase `openbooks` everywhere.
- **Koin DI:** project uses the **Koin compiler plugin** (4.2). Its compile-time check (`KOIN-D001`) only tracks definitions written with the plugin DSL (`org.koin.plugin.module.dsl.*`): `single<T>()`, `factory<T>()`, `viewModel<T>()`, `.bind(X::class)` — standard `singleOf`/`factoryOf` are invisible to it. Rewrote `Modules.kt` accordingly; dropped `SearchBooksUseCase`'s dispatcher param (plugin can't construct a `CoroutineDispatcher`). Kept `includes()` so each module passes `Module.verify()` in isolation.
- **Lint:** cleaned scaffolding detekt/ktlint issues (empty stubs, trailing newlines, `@Suppress` + TODO on Issue-5/7 placeholders).

**Note:** `MainApp` is still **not** wired in `androidApp/.../AndroidManifest.xml` (`android:name` missing), so Koin doesn't start on Android yet — wire it when the app first runs (Issue 7).

## Issue 2 — Fix swapped `data/local` ↔ `data/remote` packages
**Labels:** `chore` `priority:1` · **Depends on:** —  *(tracked as a background task already)*

Network source currently lives in `data.local`, DB source in `data.remote`. Correct so `data.remote` = network, `data.local` = database.

**Status: ✅ done.** Sources moved to their correct packages; imports updated; `detektAll ktlintAll :shared:allTests :androidApp:lintDebug assembleDebug` all pass locally.

**Acceptance criteria**
- [x] `BooksRemoteSource`/`BooksRemote` → `data.remote`; `BooksLocalSource`/`BooksLocal` → `data.local`.
- [x] Imports updated in `BooksRepository` and `Modules`.
- [x] Build + tests green.

## Issue 3 — Room-KMP persistence spike (favorites) ⚠ highest time-risk
**Labels:** `feature` `data` `priority:1` · **Depends on:** Issue 2

Stand up Room 2.7+ in KMP. Front-load this — if the wiring fights, find out at hour 1.

**Acceptance criteria**
- [ ] `Favorite` entity (Work key as PK; title, author, coverUrl).
- [ ] DAO: `observeFavorites(): Flow<List<Favorite>>`, `upsert`, `delete`, `isFavorite`.
- [ ] Database + bundled SQLite driver; `expect`/`actual` builder wired into Koin `platformModule` (Android `actual`).
- [ ] Proven to persist across app restart on Android.

## Issue 4 — Reshape `Book` entity
**Labels:** `refactor` `domain` `priority:1` · **Depends on:** Issue 1

Replace `Book(title, description)` with `key`, `title`, `authors`, `coverUrl`, `firstPublishYear`, `description?`.

**Acceptance criteria**
- [x] Entity updated; navigation `BookDetail(id)` uses the Work `key`.
- [x] Existing references/tests compile.

## Issue 5 — Ktor + OpenLibrary Search API remote source
**Labels:** `feature` `data` `priority:1` · **Depends on:** Issues 2, 4

**Acceptance criteria**
- [ ] Ktor client (+ content negotiation / kotlinx-serialization) in `commonMain`.
- [ ] `search(query, page)` against `/search.json?q=&page=&limit=`; DTO → `Book` mapping (`cover_i` → `coverUrl`).
- [ ] `getBook(key)` against `/works/{key}.json` for the description.
- [ ] Repository merges remote results with local favorite state.
- [ ] Mapper/repository unit tests.

## Issue 6 — `BooksListViewModel`: search, paging, states, favorite toggle (TDD)
**Labels:** `feature` `presentation` `priority:1` · **Depends on:** Issues 3, 5

**Acceptance criteria**
- [ ] Debounced query input drives search.
- [ ] State model `Idle | Loading | Success(books, loadingMore, endReached) | Empty | Error`.
- [ ] Manual pagination: load next `page` and append.
- [ ] Toggle favorite from a list item (writes through Room).
- [ ] Tests cover each state transition + paging append + favorite toggle.

## Issue 7 — `BooksListScreen` UI
**Labels:** `feature` `ui` `priority:1` · **Depends on:** Issue 6

**Acceptance criteria**
- [ ] Search bar; `LazyColumn` of thin items (cover via Coil 3, title, author, favorite control).
- [ ] Infinite scroll via `derivedStateOf` over `LazyListState`.
- [ ] Renders Idle / Loading / Empty / Error / Success.
- [ ] `collectAsStateWithLifecycle()`; no business logic in the composable.

## Issue 8 — `FavoritesScreen` + ViewModel
**Labels:** `feature` `priority:2` · **Depends on:** Issue 3

**Acceptance criteria**
- [ ] VM observes `Flow<List<Favorite>>` (plain Flow, **not** paged).
- [ ] Screen lists favorites with remove; handles empty state.
- [ ] Wired into navigation (`FavoritesList`). VM tested.

## Issue 9 — `BookDetailScreen` + ViewModel
**Labels:** `feature` `priority:2` · **Depends on:** Issues 4, 5

**Acceptance criteria**
- [ ] VM fetches the Work by `key`; loading/error/success states.
- [ ] Screen shows cover, title, author(s), year, full description, favorite toggle.
- [ ] Wired into navigation (`BookDetail`). VM tested.

## Issue 10 — Round out tests to 3–5 meaningful ones
**Labels:** `test` `priority:2` · **Depends on:** Issues 6, 8, 9

Ensure the suite covers the critical logic (list VM, detail/favorites VM, repository) — at least the 3–5 the challenge asks for.

## Issue 11 — README: install/run, decisions, trade-offs, not-done + remove false CI claims
**Labels:** `docs` `priority:2` · **Depends on:** all above

**Acceptance criteria**
- [ ] Install/run instructions; technical decisions (link the ADRs); trade-offs; explicit "not done & why".
- [ ] **Delete the iOS/JS/Wasm CI and `iosMain`/`jvmMain` claims** — only the Android target exists.

---

## ── Cut line — bonus only if time remains ──

- **B1** — Paging 3 (replace manual infinite scroll). `priority:bonus`
- **B2** — Compose UI tests with `createComposeRule`. `priority:bonus`
- **B3** — Animations (`AnimatedVisibility`, `animateContentSize`). `priority:bonus`
- **B4** — Dark theme + dynamic color (Material You). `priority:bonus`
