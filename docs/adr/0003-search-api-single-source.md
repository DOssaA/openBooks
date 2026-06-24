# OpenLibrary Search API as the single list source; empty initial screen

The book list, remote search, and pagination are all served by **one endpoint** — OpenLibrary's Search API (`/search.json?q=&page=&limit=`) — and the detail screen fetches `/works/{key}.json` for the full description. We deliberately do **not** populate the list on launch: with no query the screen shows an **empty state** with a search bar, and results appear only once the user types.

Rationale: with ~1 day of build time, one integration that satisfies list + search + pagination simultaneously is the right economy, and the blank-query / in-flight / zero-results / failure flow exercises all three required UI states (empty, loading, error) through a single screen. Trade-off accepted: the home screen is empty until the user interacts, rather than showing a seeded/trending list (which would add an integration for no new requirement). Subjects/Trending endpoints were considered and rejected for this reason.
