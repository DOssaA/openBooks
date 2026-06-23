# openBooks
App for searching books and mark your favorites

This is a Kotlin Multiplatform project targeting Android.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## CI/CD summary

The repository uses GitHub Actions with a low-cost baseline oriented to fast pull request feedback and multiplatform confidence.

- `pr-checks`
  Runs the mandatory branch gate: `actionlint`, Gradle wrapper validation, shared quality gates, Android lint/build, JS/Wasm build verification, and iOS simulator build verification on `macos-latest`.
- `ci-main`
  Runs on main branches and generates coverage reports through `Kover`.
- `security`
  Runs low-cost security automation with secret scanning and dependency review where repository features allow it.
- `dependabot`
  Keeps Gradle and GitHub Actions dependencies moving through scheduled update PRs.

CI/CD decisions behind this setup:

- Keep the default PR path fast enough to be usable every day.
- Validate Android and iOS build health because this is a Kotlin Multiplatform repository.
- Prefer reliable open-source or GitHub-native tooling that does not require paid security add-ons by default.
- Separate PR checks, main-branch reporting, and security automation so failures are easier to reason about and maintain.

Local hook decisions behind this setup:

- `lefthook` is used to bring part of the CI feedback loop closer to the developer machine.
- `pre-commit` stays relatively fast and focuses on formatting plus optional local workflow and secret checks when the required binaries are installed.
- `pre-push` runs the heavier Kotlin, Android, Web, and conditional iOS verification steps.
- GitHub Actions remains the source of truth. Local hooks improve feedback speed but do not replace server-side enforcement.