package com.gustiadhitya.sakuwise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.gustiadhitya.sakuwise.core.designsystem.component.FieldButton
import com.gustiadhitya.sakuwise.core.designsystem.component.RupiahText
import com.gustiadhitya.sakuwise.core.designsystem.component.SwAmount
import com.gustiadhitya.sakuwise.core.designsystem.component.SwAmountDisplay
import com.gustiadhitya.sakuwise.core.designsystem.component.SwBar
import com.gustiadhitya.sakuwise.core.designsystem.component.SwChip
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSheet
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTopBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.CaptionStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.H1Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M5bGalleryScreen() {
    val chips = listOf("Semua", "Needs", "Wants", "Investment")
    var selectedChip by remember { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            SwTopBar(title = "M5b Components", subtitle = "Compound Components")

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                GallerySection("SwChip")
                Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                    chips.forEachIndexed { index, label ->
                        SwChip(
                            text = label,
                            selected = index == selectedChip,
                            onClick = { selectedChip = index },
                        )
                    }
                }

                GallerySection("SwBar")
                listOf(
                    0f to "0%",
                    0.67f to "67% — Needs",
                    0.81f to "81% — Investment",
                    1.0f to "100% — full",
                    1.2f to "120% — over budget",
                ).forEach { (progress, label) ->
                    Text(label, style = CaptionStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    SwBar(progress = progress, modifier = Modifier.fillMaxWidth(), label = label)
                }

                GallerySection("RupiahText + SwAmount")
                RupiahText(amount = 4_700_000L, style = H1Style)
                RupiahText(amount = 15_500_000L)
                RupiahText(amount = 5_200_000L, short = true)
                RupiahText(amount = -10_800_000L, short = true)
                SwAmount(
                    amount = 28_000L,
                    display = SwAmountDisplay.Short,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                GallerySection("SwSheet")
                Button(onClick = { showSheet = true }) {
                    Text("Buka Sheet")
                }

                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(bottom = SakuwiseSpacing.xxxl),
                )
            }
        }

        if (showSheet) {
            SwSheet(
                title = "Aksi Plan",
                onDismiss = { showSheet = false },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = SakuwiseSpacing.l),
                    verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                ) {
                    FieldButton(text = "Salin dari bulan lalu", subText = "Ambil item recurring dari Plan April", onClick = { showSheet = false })
                    FieldButton(text = "Terapkan template starter", subText = "Struktur kategori standard Indonesia", onClick = { showSheet = false })
                    FieldButton(text = "Atur persentase untuk plan ini", subText = "Override alokasi 50/30/20", onClick = { showSheet = false })
                }
            }
        }
    }
}

@Composable
private fun GallerySection(title: String) {
    Text(
        text = title,
        style = CaptionStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        modifier = Modifier.padding(top = SakuwiseSpacing.m),
    )
}
