# ReadYou — Claude Code Context

## Project Overview
ReadYou is an Android RSS reader built with Jetpack Compose and Material You (Material 3). It supports multiple RSS/Atom backend sources and presents content in a clean, adaptive layout.

- **Package:** `me.ash.reader`
- **Min SDK:** 26 | **Target SDK:** 34 | **Compile SDK:** 36
- **Version:** 0.16.1 (code 46)
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 Adaptive

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Install on connected device
./gradlew installDebug
```

Requires JDK 17. If `JAVA_HOME` isn't set, point it at the bundled JBR inside Android Studio:
```bash
export JAVA_HOME=/path/to/Android\ Studio.app/Contents/jbr/Contents/Home
```

## Architecture

**Pattern:** MVVM + Hilt DI + Room + DataStore

```
app/src/main/java/me/ash/reader/
├── infrastructure/
│   ├── preference/       # All user preferences (DataStore-backed sealed classes)
│   ├── rss/              # RSS sync service, parsers, repositories
│   └── db/               # Room database, DAOs, entities
├── ui/
│   ├── page/
│   │   ├── nav3/         # AppEntry.kt — top-level nav scaffold (Nav3 + M3 Adaptive)
│   │   ├── adaptive/     # ArticleListReadingPage — two/one pane scaffold
│   │   ├── home/
│   │   │   ├── feeds/    # FeedsPage — subscription list
│   │   │   ├── flow/     # FlowPage — article list
│   │   │   └── reading/  # ReadingPage — article reader
│   │   └── settings/     # All settings pages
│   ├── component/        # Shared UI components
│   └── ext/              # DataStore keys, Compose extensions
```

## Preferences System

All preferences follow a consistent pattern:
1. **Sealed class** in `infrastructure/preference/` — extends `Preference`, has `ON`/`OFF` or enum variants, `put()`, `fromPreferences()`, `default`, `LocalXxx` CompositionLocal
2. **DataStore key** registered in `ui/ext/DataStoreExt.kt` — in both `keyList` and the deprecated `DataStoreKey.keys` map
3. **`Settings.kt`** — add field with default
4. **`Preference.kt` `toSettings()`** — map DataStore → Settings
5. **`SettingsProvider.kt` `ProvidesSettings()`** — expose via CompositionLocal

Use `FlowSingleColumnPreference.kt` as a reference implementation for boolean prefs.

## Adaptive Layout

The app uses **Material 3 Adaptive** (`NavigableListDetailPaneScaffold`) for tablet two-pane support.

Key files:
- `AppEntry.kt` — creates the `ListDetailPaneScaffoldNavigator`, computes `scaffoldDirective`
- `ArticleListReadingPage.kt` — derives `isTwoPane` from `navigator.scaffoldValue`

**Single-column toggle** (`FlowSingleColumnPreference`): forces `maxHorizontalPartitions = 1` on the directive. A `LaunchedEffect` in `AppEntry.kt` watches the preference via `snapshotFlow` and calls `navigator.navigateTo(List)` on change — this is required because the navigator only recomputes `scaffoldValue` on navigation events, not on directive-only changes.

## Key Libraries

| Library | Purpose |
|---------|---------|
| Hilt | Dependency injection |
| Room | Local database |
| DataStore | Preference persistence |
| Nav3 (`androidx.navigation3`) | Navigation back stack |
| Material3 Adaptive | Two-pane / adaptive layout |
| Coil | Image loading |
| OkHttp + Rome | RSS fetching & parsing |
| Timber | Logging |

## Development Branch

Active feature work: `claude/rss-reader-layout-research-irv2Q`

Branch naming convention: `claude/<description>-<sessionId>`

Push with: `git push -u origin <branch-name>`
