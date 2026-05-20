package com.gustiadhitya.sakuwise.feature.transaction.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OcrStage {
    data object Idle : OcrStage
    data object Processing : OcrStage
    data class Ready(val draft: ReceiptDraft) : OcrStage
    data class Failure(val message: String) : OcrStage
}

@HiltViewModel
class OcrCaptureViewModel @Inject constructor(
    private val ocr: OcrService,
) : ViewModel() {
    private val _stage = MutableStateFlow<OcrStage>(OcrStage.Idle)
    val stage: StateFlow<OcrStage> = _stage

    fun process(bitmap: Bitmap) {
        _stage.value = OcrStage.Processing
        viewModelScope.launch {
            runCatching { ocr.extractReceipt(bitmap) }
                .onSuccess { _stage.value = OcrStage.Ready(it) }
                .onFailure {
                    _stage.value = OcrStage.Failure(it.message ?: "OCR gagal — coba foto ulang.")
                }
        }
    }

    fun reset() { _stage.value = OcrStage.Idle }
}

/**
 * OcrCaptureScreen — uses the system camera (TakePicturePreview) to grab a
 * Bitmap, runs ML Kit text recognition + Indonesian receipt parser, then
 * hands the resulting draft back via [onComplete] for prefill into the
 * Expense form.
 *
 * No CameraX dependency required — the system intent already handles preview,
 * focus, and capture. Result is an in-memory thumbnail Bitmap (~1MP) which is
 * adequate for receipt OCR.
 */
@Composable
fun OcrCaptureScreen(
    onClose: () -> Unit,
    onComplete: (ReceiptDraft) -> Unit,
    viewModel: OcrCaptureViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val ctx = LocalContext.current
    val stage by viewModel.stage.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap -> if (bitmap != null) viewModel.process(bitmap) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) cameraLauncher.launch(null) }

    LaunchedEffect(Unit) {
        if (stage is OcrStage.Idle) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) cameraLauncher.launch(null)
            else permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(sw.bg)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = SwSpace.pageH, top = 6.dp, bottom = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sw.surface)
                    .padding(2.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack, "Kembali",
                    tint = sw.ink,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sw.surface),
                )
            }
            Text("Scan Struk", color = sw.ink,
                style = SwType.H1.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp).weight(1f))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SwSpace.pageH),
        ) {
            when (val s = stage) {
                is OcrStage.Idle -> IdleHero(sw.primaryContainer, sw.onPrimaryContainer)
                is OcrStage.Processing -> ProcessingHero()
                is OcrStage.Ready -> ReadyBody(
                    draft = s.draft,
                    onUse = { onComplete(s.draft) },
                    onRetake = {
                        viewModel.reset()
                        cameraLauncher.launch(null)
                    },
                )
                is OcrStage.Failure -> FailureBody(
                    message = s.message,
                    onRetake = {
                        viewModel.reset()
                        cameraLauncher.launch(null)
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
            SwButton(text = "Tutup", variant = SwButtonVariant.Ghost, onClick = onClose)
        }
    }
}

@Composable
private fun IdleHero(bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CameraAlt, null, tint = fg, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Menyiapkan kamera…", color = fg, style = SwType.Body.copy(fontSize = 14.sp))
        }
    }
}

@Composable
private fun ProcessingHero() {
    val sw = SwTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(sw.surface),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = sw.primary)
            Spacer(Modifier.height(12.dp))
            Text("Membaca struk…", color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 14.sp))
        }
    }
}

@Composable
private fun ReadyBody(
    draft: ReceiptDraft,
    onUse: () -> Unit,
    onRetake: () -> Unit,
) {
    val sw = SwTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(sw.successSoft)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Check, null, tint = sw.success, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(10.dp))
            Column {
                Text("Struk berhasil dibaca", color = sw.success,
                    style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                Text("Confidence: ${draft.confidence.name}",
                    color = sw.ink,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp))
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    SwCard(padding = PaddingValues(0.dp)) {
        Column {
            DraftRow("Merchant", draft.merchant ?: "—")
            DraftRow("Tanggal", draft.date?.toString() ?: "—")
            DraftRow("Total",
                draft.totalAmount?.let { "Rp ${it.toRupiahShort(prefix = "")}" } ?: "—")
        }
    }
    Spacer(Modifier.height(16.dp))
    SwButton(text = "Isi ke Form Pengeluaran", onClick = onUse)
    Spacer(Modifier.height(8.dp))
    SwButton(text = "Foto Ulang", variant = SwButtonVariant.Outline, onClick = onRetake)
}

@Composable
private fun FailureBody(message: String, onRetake: () -> Unit) {
    val sw = SwTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(sw.dangerSoft)
            .padding(20.dp),
    ) {
        Column {
            Icon(Icons.Outlined.WarningAmber, null, tint = sw.danger,
                modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text("Gagal membaca struk", color = sw.danger,
                style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
            Text(message, color = sw.ink,
                style = SwType.LabelSmall.copy(fontSize = 12.sp))
        }
    }
    Spacer(Modifier.height(16.dp))
    SwButton(text = "Foto Ulang", onClick = onRetake)
}

@Composable
private fun DraftRow(label: String, value: String) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(label, color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.weight(1f))
        Text(value, color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
    }
}
