# openBooks

A book discovery app: the user searches the OpenLibrary catalog, opens a book to read more, and saves books as favorites that persist across sessions.

## Language

**Book**:
A single title the user can discover, open, and favorite. In OpenLibrary terms this is a **Work** (not an Edition). Identified by its **Work key**.
_Avoid_: Edition, Volume, Title (as a noun)

**Work key**:
The stable OpenLibrary identifier for a Book (e.g. `OL45804W`). The id used for navigation to detail and as the primary key for a Favorite.
_Avoid_: id, isbn (an ISBN identifies an Edition, not a Work)

**Cover**:
The image for a Book, derived from OpenLibrary's `cover_i` id. May be absent.

**Favorite**:
A Book the user has saved. Stored locally and survives app restarts. A Book is either a Favorite or not — there is no partial state.

**Search query**:
The text the user types to retrieve Books from the remote catalog. When the query is empty, no Books are shown (empty initial state).

## Relationships

- A **Search query** returns zero or more **Books**
- A **Book** is identified by exactly one **Work key**
- A **Favorite** is a **Book** the user saved, keyed by its **Work key**
- A **Book** has zero or one **Cover**

## Example dialogue

> **Dev:** "When the user favorites a Book from the search list, what do we store?"
> **Product:** "Enough to render the Favorites screen offline — the Work key, title, author, and cover. We don't re-fetch from the network to show favorites."
> **Dev:** "And the full description?"
> **Product:** "That's only on the detail screen, fetched live from the Work. Favorites don't need it."

## Flagged ambiguities

- "Book" vs "Work" vs "Edition" — resolved: in this app a **Book** always means an OpenLibrary **Work**, identified by its **Work key**. Editions/ISBNs are out of scope.
