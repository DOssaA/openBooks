# Compose Multiplatform with a single Android target

The challenge asks for a native Android app; Compose Multiplatform is only an optional bonus. We adopt CMP and place all UI and architecture in `commonMain`, but wire up **only the Android target** for this delivery — there is not enough time in the 3-day window to build and test iOS/web app shells, and the brief values finishing the graded Android features over breadth.

The `commonMain`-pure structure (UI, ViewModels, domain, data all platform-agnostic; Koin's `platformModule` as the only `expect`/`actual` seam) means adding an iOS or web target later is a small, additive step rather than a rewrite. The README must state this honestly and must not claim iOS/JS/Wasm CI or targets that do not exist yet.
