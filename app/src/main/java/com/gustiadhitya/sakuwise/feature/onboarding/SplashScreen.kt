package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.icons.LogoDaun
import com.gustiadhitya.sakuwise.core.designsystem.icons.Wordmark
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val sw = SwTheme.colors
    var showMark by remember { mutableStateOf(false) }
    var showWord by remember { mutableStateOf(false) }
    var showTag by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showMark = true
        delay(220); showWord = true
        delay(220); showTag = true
        delay(1160); onDone()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(sw.bg),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = showMark,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
            ) {
                LogoDaun(sizeDp = 120)
            }
            Spacer(Modifier.height(22.dp))
            AnimatedVisibility(
                visible = showWord,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
            ) {
                Wordmark(sizeSp = 36)
            }
            Spacer(Modifier.height(22.dp))
            AnimatedVisibility(
                visible = showTag,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
            ) {
                Text(
                    stringResource(R.string.splash_tagline),
                    color = sw.inkMuted,
                    style = SwType.Body.copy(fontSize = 13.sp),
                )
            }
        }
    }
}
