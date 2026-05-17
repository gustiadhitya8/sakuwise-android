package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.BodyLStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.BodyStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.CaptionStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

private val FieldButtonHeight = 52.dp
private val FieldButtonHorizontalPadding = 14.dp

// Read-only field-shaped picker — looks like SwField, behaves like a Button.
// Shows a leading icon, primary text, optional sub-text, and a trailing chevron.
@Composable
fun FieldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    subText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
    leadingIconContentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = CaptionStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(bottom = SakuwiseSpacing.xs),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(FieldButtonHeight)
                .background(MaterialTheme.colorScheme.surface, SakuwiseShapes.md)
                .border(SakuwiseSpacing.borderThin, MaterialTheme.colorScheme.outline, SakuwiseShapes.md)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = FieldButtonHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(leadingIconTint, SakuwiseShapes.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = leadingIconContentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(SakuwiseSpacing.m))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = BodyLStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                )
                if (subText != null) {
                    Text(
                        text = subText,
                        style = BodyStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FieldButtonPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                FieldButton(text = "Kopi/Kafe", label = "Plan Item *", subText = "Makan di Luar · Wants", onClick = {})
                FieldButton(text = "GoPay", label = "Akun *", subText = "Saldo: Rp 280.000", onClick = {})
                FieldButton(text = "15 Mei 2026", label = "Tanggal", subText = "2 hari lalu", onClick = {})
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FieldButtonPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                FieldButton(text = "GoPay", label = "Akun *", subText = "Saldo: Rp 280.000", onClick = {})
            }
        }
    }
}
