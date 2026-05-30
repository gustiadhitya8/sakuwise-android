# QA — Empty / Error States (Item 6, v1.0.4)

Tested on emulator-5554 (API 35) with a freshly wiped install, walking the
real first-run path as a brand-new user. Goal: no crashes on empty data, and
sensible/localized empty states.

## Screens reviewed (empty data, English locale)
| Screen | Result |
|---|---|
| Onboarding step 1 (language) | OK — clean EN, no crash |
| Onboarding step 2 (nickname + PIN + biometric) | OK |
| Onboarding step 3 (privacy notice) | OK |
| Onboarding step 4 (first account, Rp 0) | OK |
| Lock screen (PIN unlock) | OK |
| Dashboard (no transactions) | OK — "No transactions yet…", "No backup yet" |
| Plan (no plan) | OK — "No plan for this month yet" + Create/Template CTAs |
| Assets (1 account Rp 0, no gold/land/deposit/debt) | OK — empty class cards, trend-chart empty state |
| Me / Settings | OK |

## Static crash-safety review (empty collections)
All collection access in chart/aggregate UI is guarded:
- TransactionHistoryScreen category chart → `if (categorySummary.isEmpty())` else branch
- TransactionHistoryScreen daily-expense chart x-axis → `if (n >= 2)`
- AssetsHubScreen net-worth trend → `if (seriesAll.isNotEmpty())`
- TransactionCsvParser → `if (lines.isEmpty()) return`
No unguarded `.first()/.last()/[i]` on user-data collections in UI.

## Error paths (code-reviewed)
- CSV import: malformed rows → `ParseResult.errors` surfaced as localized text;
  bad header → localized "invalid header" message; no crash.
- Restore wrong PIN → `BadPinException` → "PIN salah atau file rusak." message.

## Issues found & fixed
1. Dashboard "All Transaction History" + "↑ … in · … out" showed Indonesian —
   root cause was a stale debug install; fix already in code/resources (verified
   English after rebuild).
2. Assets class cards showed "% dari total" in EN → new key
   `assets_pct_of_total_format`.
3. Settings rows "Export CSV/XLSX" + "Import CSV" subtitles were hardcoded
   Indonesian → new `settings_export_csv*` / `settings_import_csv*` keys.

After fixes: app-wide grep finds no Indonesian UI literals; ID/EN resource key
sets balanced (880 each).
