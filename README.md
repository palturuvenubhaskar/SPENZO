# Spenzo — Expense Tracker (Android, Kotlin + Jetpack Compose)

A modern, privacy-first expense tracker built with Jetpack Compose and Material 3. No cloud, no tracking, no ads — your data stays on your device.

## Screenshots

| Home | Graphs | Analysis | History |
|------|--------|----------|---------|
| Balance card, monthly delta, quick actions | Daily/monthly bar charts, category donut | Budgets, savings rate, avg/day | Searchable list, date grouping |

## Features

- **Transactions**: Add income & expenses with name, amount, category, date, note
- **Edit/Delete**: Tap any transaction to edit; long-press or overflow menu to delete
- **Home Dashboard**: Balance card with month-over-month delta, income/spent/saved stats, recent transactions
- **Graphs**: Daily spending bars (tap for value), monthly totals (year view), category donut chart
- **Analysis**: Per-category monthly budgets, savings rate, average daily spend, top categories
- **History**: Full list grouped by day ("Today", "Yesterday", dates), search by name/note/category, filter by type
- **CSV Export**: Share all transactions via system share sheet (FileProvider, no external storage permission)
- **Themes**: Light / Dark / System default with OLED-friendly dark palette
- **Currency**: 30+ currencies with proper symbols and formatting
- **Privacy**: Room database + SharedPreferences only; auto-backup eligible; no network calls

## Tech Stack

| Layer | Choice |
|-------|--------|
| UI | Jetpack Compose (Material 3), single-Activity, Navigation Compose |
| DB | Room (KSP), schema exported for safe migrations |
| State | `StateFlow` + `ViewModel` (shared instance across tabs) |
| DI | Manual — no Hilt/Koin (flat package, ~12 files) |
| Concurrency | Coroutines + Flow |
| Build | Gradle KTS, R8 minify/shrink enabled, dynamic `versionCode`/`versionName` from git |

## Architecture

```
com.spenzo
├── data/
│   ├── Money.kt          # cents-based formatting/parsing (no float drift)
│   ├── Dates.kt          # ISO storage keys (Locale.ROOT), display formatters
│   ├── BudgetStore.kt    # per-month budgets (yyyy-MM|Category) via SharedPreferences
│   ├── CsvExporter.kt    # CSV write + FileProvider share
│   ├── Transaction.kt    # entity + TxType/Category enums (Material icons)
│   └── AppDatabase.kt    # Room DB + DAO (Flow<List<Tx>>)
├── ui/
│   ├── theme/            # SpColors, SpTheme, Space, Radius, Color.kt
│   ├── components/       # SpCard, BarChart, TransactionRow, SegmentedToggle, etc.
│   ├── HomeScreen.kt
│   ├── GraphsScreen.kt
│   ├── AnalysisScreen.kt
│   ├── HistoryScreen.kt
│   ├── AddEditTransactionScreen.kt
│   └── SettingsScreen.kt
├── TransactionViewModel.kt  # single shared instance
├── SpApplication.kt
└── MainActivity.kt
```

## Requirements

- Android Studio **Ladybug** or newer
- JDK **17** (configured in `build.gradle.kts`)
- Android SDK **35** (compile/target)
- Min SDK **26** (Android 8.0)

## Build

```bash
# Debug
./gradlew assembleDebug

# Release (unsigned — sign before Play Store)
./gradlew assembleRelease

# Tests
./gradlew testDebugUnitTest

# Lint
./gradlew lintDebug
```

## Key Implementation Details

### Money handling
- Amounts persisted as `Double` (Room column unchanged), but **all math goes through integer cents**
- `Money.cents()`, `Money.sum()`, `Money.format()` guarantee no floating-point drift
- Sign always before symbol: `-$12.00` (never `$-12.00`)

### Date storage
- Month/day keys are **ISO `yyyy-MM` / `yyyy-MM-dd`** using `Locale.ROOT`
- Thai Buddhist / other calendars cannot break `LIKE 'yyyy-MM%'` queries
- `Dates.daysElapsed()` caps at today for current month (correct average daily spend)

### Budgets
- Keyed by `yyyy-MM|Category` — truly monthly, not global
- Reactive `StateFlow` so Analysis screen updates instantly
- Legacy global budgets migrated once on first launch

### Shared ViewModel
- One `TransactionViewModel` created in `MainActivity`, passed to all screens
- Month selected in Graphs = month shown in Analysis = month in Home balance delta

### Charts
- `BarChart` drawn with Compose `Canvas` + `TextMeasurer` — labels scale with font size, never overlap
- Tallest bar **always labeled** (only scale on the chart)
- 31 daily bars render cleanly; x-axis labels auto-clamped

### CSV Export
- Written to app cache dir, shared via `FileProvider`
- UTF-8 BOM so Excel opens notes correctly
- No `WRITE_EXTERNAL_STORAGE` permission needed

## Project Structure

```
SPENZO/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/spenzo/...
│   │   │   ├── res/ (values, values-night, xml, mipmap)
│   │   │   └── AndroidManifest.xml
│   │   └── test/java/com/spenzo/ (MoneyTest, DatesTest)
│   └── schemas/com.spenzo.data.AppDatabase/1.json  # Room schema export
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## License

MIT — do whatever you want, but keep the license notice.

---

*Built as a complete rewrite from a Clean Architecture + Hilt template into a flat, pragmatic codebase. The old `com.example.spenzo` package (30+ files) was removed entirely.*