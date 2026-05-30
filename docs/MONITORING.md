# Crash / ANR Monitoring (v1.0.4)

> Decision: **Android Vitals (Google Play Console)** is the sole source of
> crash and ANR data. **No telemetry/analytics/crash SDK is bundled.**

## Why no SDK
Sakuwise is local-first with a hard no-telemetry promise (see README §Privacy).
Firebase Crashlytics / Sentry / etc. would all ship a client that phones home,
breaking that promise. Android Vitals gives crash & ANR rates, stack traces,
and affected-device breakdowns for free, collected by the OS/Play Store with no
code in our app and no per-user data flowing to us.

## What this means in the codebase
- **No** `firebase-*`, `crashlytics`, `analytics`, `sentry`, or `google-services`
  dependencies. The dead `google-services` plugin + `googleServices` version
  were removed from the version catalog in v1.0.4. The only Google dependency is
  `play-services-auth`, used solely for Google Drive backup sign-in (opt-in).
- A leftover local `app/google-services.json` is **gitignored and never
  committed** — the Drive flow does not need it (see `core/cloud/README.md`).
- Exceptions must not be swallowed silently where a crash/ANR signal matters.
  The only intentional empty catch is date-format probing in
  `TransactionCsvParser.parseDate` (it tries several formats and returns null on
  miss — expected control flow, not a swallowed crash).

## How to read crash data
Play Console → Quality → **Android vitals** → Crashes & ANRs. Filter by app
version (e.g. 1.0.4 / versionCode 5) after release to catch regressions in the
external-user rollout.

## If we ever need richer diagnostics
Prefer an on-device, no-network option (e.g. write a local crash log the user
can attach to a support email) over any phone-home SDK, to preserve the
no-telemetry guarantee.
