package com.gustiadhitya.sakuwise.feature.onboarding

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustiadhitya.sakuwise.core.designsystem.brand.DaunMark
import com.gustiadhitya.sakuwise.core.designsystem.brand.Wordmark
import com.gustiadhitya.sakuwise.core.designsystem.theme.FigtreeFontFamily
import com.gustiadhitya.sakuwise.core.designsystem.theme.LocalReduceMotion
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseAnimation
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import kotlinx.coroutines.delay

private val SplashMarkSize = 120.dp
private val SplashWordmarkFontSize = 36.sp
private val SplashSlideOffset = 20f  // dp, starting Y offset for fade-up animation

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val reduceMotion = LocalReduceMotion.current

    var showMark by remember { mutableStateOf(reduceMotion) }
    var showWordmark by remember { mutableStateOf(reduceMotion) }
    var showTagline by remember { mutableStateOf(reduceMotion) }
    var animationDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            delay(1600)
        } else {
            showMark = true
            delay(220)
            showWordmark = true
            delay(220)
            showTagline = true
            delay(1160)  // total: 0 + 220 + 220 + 1160 = 1600ms
        }
        animationDone = true
    }

    LaunchedEffect(animationDone, destination) {
        if (!animationDone) return@LaunchedEffect
        when (destination) {
            SplashDestination.Onboarding -> onNavigateToOnboarding()
            SplashDestination.Home -> onNavigateToHome()
            SplashDestination.None -> Unit
        }
    }

    val markAlpha by animateFloatAsState(
        targetValue = if (showMark) 1f else 0f,
        animationSpec = SakuwiseAnimation.splashFade,
        label = "markAlpha",
    )
    val markSlide by animateFloatAsState(
        targetValue = if (showMark) 0f else SplashSlideOffset,
        animationSpec = SakuwiseAnimation.splashFade,
        label = "markSlide",
    )
    val wordmarkAlpha by animateFloatAsState(
        targetValue = if (showWordmark) 1f else 0f,
        animationSpec = SakuwiseAnimation.splashFade,
        label = "wordmarkAlpha",
    )
    val wordmarkSlide by animateFloatAsState(
        targetValue = if (showWordmark) 0f else SplashSlideOffset,
        animationSpec = SakuwiseAnimation.splashFade,
        label = "wordmarkSlide",
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = SakuwiseAnimation.splashFade,
        label = "taglineAlpha",
    )
    val taglineSlide by animateFloatAsState(
        targetValue = if (showTagline) 0f else SplashSlideOffset,
        animationSpec = SakuwiseAnimation.splashFade,
        label = "taglineSlide",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DaunMark(
                size = SplashMarkSize,
                modifier = Modifier
                    .alpha(markAlpha)
                    .offset(y = markSlide.dp),
            )
            Spacer(Modifier.height(SakuwiseSpacing.xxl))
            Wordmark(
                fontSize = SplashWordmarkFontSize,
                modifier = Modifier
                    .alpha(wordmarkAlpha)
                    .offset(y = wordmarkSlide.dp),
            )
            Spacer(Modifier.height(SakuwiseSpacing.s))
            Text(
                text = "Rencanakan. Catat. Tenang.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FigtreeFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .alpha(taglineAlpha)
                    .offset(y = taglineSlide.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashPreviewLight() {
    SakuwiseTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        SplashScreen()
    }
}
