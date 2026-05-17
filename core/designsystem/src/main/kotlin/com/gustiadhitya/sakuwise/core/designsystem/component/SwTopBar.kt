package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.H2Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

// A11Y-004: back button visual area 40dp, hit area expanded to 48dp via padding
private val BackButtonVisualSize = 40.dp
private val BackButtonHitAreaSize = 48.dp
private val BackButtonPadding = (BackButtonHitAreaSize - BackButtonVisualSize) / 2

@Composable
fun SwTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    rightAction: @Composable (() -> Unit)? = null,
    transparent: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (transparent) MaterialTheme.colorScheme.background.copy(alpha = 0f)
                else MaterialTheme.colorScheme.background,
            )
            .padding(start = if (onBack != null) 0.dp else SakuwiseSpacing.l,
                     end = SakuwiseSpacing.l,
                     top = SakuwiseSpacing.xs,
                     bottom = SakuwiseSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            // A11Y-004: hit area 48dp, visual 40dp
            Box(
                modifier = Modifier
                    .size(BackButtonHitAreaSize)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = "Kembali" },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(BackButtonVisualSize)
                        .background(MaterialTheme.colorScheme.background, SakuwiseShapes.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(SakuwiseSpacing.xs))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = H2Style.copy(color = MaterialTheme.colorScheme.onBackground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }

        if (rightAction != null) {
            Spacer(Modifier.width(SakuwiseSpacing.s))
            rightAction()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwTopBarWithBackPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SwTopBar(
                title = "Pengeluaran",
                onBack = {},
                rightAction = {
                    SwButton(
                        text = "Simpan",
                        onClick = {},
                        size = SwButtonSize.Sm,
                        fillWidth = false,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwTopBarNoBackPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SwTopBar(title = "Beranda", subtitle = "Plan Mei · sisa 16 hari")
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwTopBarPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SwTopBar(title = "Pengeluaran", onBack = {})
        }
    }
}
