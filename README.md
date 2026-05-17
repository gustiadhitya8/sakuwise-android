# Sakuwise

Aplikasi Android *local-first* untuk personal money tracking, targeted pengguna Indonesia.

- **IDR-only** — format Rupiah native
- **Bahasa Indonesia** primary
- **Local-first** — data tersimpan lokal, terenkripsi
- **No internet permission** — privasi terjaga

## Tech Stack

- Kotlin 2.0+ · Jetpack Compose · Material 3
- Room + SQLCipher · Hilt · WorkManager · ML Kit

## Build

```bash
./gradlew assembleDebug
```

## Architecture

Clean Architecture multi-module (18 Gradle modules: 1 app + 10 core + 7 feature).

```
:app
:core:common        :core:model       :core:domain
:core:data          :core:database    :core:datastore
:core:crypto        :core:designsystem :core:ui :core:testing
:feature:onboarding :feature:dashboard :feature:plan
:feature:transaction :feature:asset    :feature:settings :feature:donation
```

## Status

V1.0.0 — in development.
