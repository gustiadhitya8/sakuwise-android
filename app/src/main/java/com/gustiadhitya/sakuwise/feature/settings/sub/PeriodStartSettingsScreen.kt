package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun PeriodStartSettingsScreen(
    onBack: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    mutator: PrefMutatorViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by main.prefs.collectAsState()

    SimpleSettingsScreen(title = stringResource(R.string.period_start_title), onBack = onBack) {
        Text(
            stringResource(R.string.period_start_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth().height(240.dp),
        ) {
            items((1..28).toList()) { day ->
                val active = prefs.planPeriodStartDay == day
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) sw.primary else sw.surface)
                        .border(1.dp, if (active) sw.primary else sw.border, RoundedCornerShape(10.dp))
                        .clickable { mutator.setPeriodStart(day) },
                ) {
                    Text(day.toString(),
                        color = if (active) sw.onPrimary else sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        SwCard {
            Text(
                stringResource(R.string.period_start_note),
                color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 12.sp),
            )
        }
    }
}
