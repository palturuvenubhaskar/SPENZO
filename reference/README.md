# Spenzo — Expense Tracker (Android, Kotlin + Jetpack Compose)

Design reference: light-gray background (#F4F4F6), white rounded cards, dark
navy balance card (#1B1C22), green/orange accents, pill-style bottom nav.
NO emojis — all categories use Material vector icons.

## Features implemented
- Add income (allowance) & expenses: name, amount, category, date, note
- Edit any previously added transaction (prefilled form)
- Delete any transaction (with confirmation dialog)
- Dashboard: balance, month income/expense, recent transactions
- History: searchable/filterable list with Edit / Delete actions
- Graphs: per-day totals for a month, per-month totals for a year, category donut
- Analysis: budgets per category, savings rate, avg daily spend, top categories,
  month-over-month comparison
- Bottom navigation: Home / Graphs / Analysis / History (pill active indicator)

## 1. Project setup
New project in Android Studio: "Empty Activity (Compose)", name `Spenzo`,
package `com.spenzo`, minSdk 26.

## 2. app/build.gradle.kts (add inside dependencies)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

## 3. project-level build.gradle.kts (plugins block)
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false

## 4. app-level plugins (top of app/build.gradle.kts)
    id("com.google.devtools.ksp")

## File map (package com.spenzo)
    ui/theme/Color.kt, Theme.kt          -> design system from reference image
    data/Transaction.kt                  -> Room entity + category enum (icons)
    data/AppDatabase.kt                  -> Room DB + DAO
    TransactionViewModel.kt              -> state + add/update/delete + queries
    MainActivity.kt                      -> nav host + pill bottom bar
    ui/HomeScreen.kt                     -> dashboard (reference-image layout)
    ui/AddEditTransactionScreen.kt       -> add / edit form, date picker
    ui/HistoryScreen.kt                  -> full list, search, edit/delete
    ui/GraphsScreen.kt                   -> day/month bars + category donut (Canvas)
    ui/AnalysisScreen.kt                 -> budgets, savings rate, insights
