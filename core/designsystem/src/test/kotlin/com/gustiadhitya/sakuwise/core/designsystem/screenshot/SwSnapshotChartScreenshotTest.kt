package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SnapshotDataPoint
import com.gustiadhitya.sakuwise.core.designsystem.component.SnapshotPeriod
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSnapshotChart
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwSnapshotChartScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swSnapshotChart_3m_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SwSnapshotChart(
                        dataPoints = ascendingData(),
                        selectedPeriod = SnapshotPeriod.THREE_MONTHS,
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                    )
                }
            }
        }
    }

    @Test
    fun swSnapshotChart_6m_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SwSnapshotChart(
                        dataPoints = volatileData(),
                        selectedPeriod = SnapshotPeriod.SIX_MONTHS,
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                    )
                }
            }
        }
    }

    @Test
    fun swSnapshotChart_flat_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SwSnapshotChart(
                        dataPoints = flatData(),
                        selectedPeriod = SnapshotPeriod.ONE_YEAR,
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                    )
                }
            }
        }
    }

    private fun ascendingData(): List<SnapshotDataPoint> {
        val day = 86_400_000L
        val base = 1_700_000_000_000L
        return listOf(
            SnapshotDataPoint(base - 90 * day, 8_000_000),
            SnapshotDataPoint(base - 75 * day, 8_800_000),
            SnapshotDataPoint(base - 60 * day, 9_200_000),
            SnapshotDataPoint(base - 45 * day, 9_800_000),
            SnapshotDataPoint(base - 30 * day, 10_500_000),
            SnapshotDataPoint(base - 15 * day, 11_200_000),
            SnapshotDataPoint(base,             12_000_000),
        )
    }

    private fun volatileData(): List<SnapshotDataPoint> {
        val day = 86_400_000L
        val base = 1_700_000_000_000L
        return listOf(
            SnapshotDataPoint(base - 150 * day, 12_000_000),
            SnapshotDataPoint(base - 120 * day, 7_500_000),
            SnapshotDataPoint(base - 90 * day,  14_000_000),
            SnapshotDataPoint(base - 60 * day,  9_000_000),
            SnapshotDataPoint(base - 30 * day,  11_500_000),
            SnapshotDataPoint(base,              10_000_000),
        )
    }

    private fun flatData(): List<SnapshotDataPoint> {
        val day = 86_400_000L
        val base = 1_700_000_000_000L
        return listOf(
            SnapshotDataPoint(base - 120 * day, 10_000_000),
            SnapshotDataPoint(base - 90 * day,  10_100_000),
            SnapshotDataPoint(base - 60 * day,  9_900_000),
            SnapshotDataPoint(base - 30 * day,  10_050_000),
            SnapshotDataPoint(base,             10_000_000),
        )
    }
}
