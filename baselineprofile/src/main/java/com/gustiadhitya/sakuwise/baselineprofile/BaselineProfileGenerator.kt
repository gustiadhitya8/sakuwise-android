package com.gustiadhitya.sakuwise.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile for the app's startup path (Item 4, v1.0.4).
 *
 * Run: `./gradlew :app:generateBaselineProfile` (uses the connected emulator).
 * The resulting profile is bundled into the release variant by the
 * androidx.baselineprofile plugin, improving cold-start on entry-level devices.
 *
 * The plugin profiles the `nonMinifiedRelease` variant of :app (release
 * applicationId, no .debug suffix). The journey is intentionally minimal —
 * launch to first frame — because that's the highest-value path; the app gates
 * behind a PIN lock so we don't drive deep navigation here.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = "com.gustiadhitya.sakuwise",
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
