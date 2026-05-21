package com.gustiadhitya.sakuwise.feature.settings.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.model.AccountType
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DepositRepository
import com.gustiadhitya.sakuwise.core.domain.repository.GoldRepository
import com.gustiadhitya.sakuwise.core.domain.repository.LandRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ComputeNetWorthUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ExportPdfUseCase — V1.1 Export PDF Report (M49).
 *
 * Generates a multi-page A4 PDF summarizing the user's financial state for a
 * given period, then returns a `content://` URI from the bundled FileProvider
 * so the screen can hand it off to `ACTION_VIEW` / `ACTION_SEND` intents.
 *
 * Uses the platform `android.graphics.pdf.PdfDocument` — no third-party PDF
 * dependency. Pure text rendering with [Paint]; no inline images (logo is
 * the "S" wordmark text; punted for V1.2 if a real embedded PNG is wanted).
 *
 * A4 = 595 × 842 points. Margin 36pt (≈12mm). Multi-page support: when the
 * cursor advances past `bottomLimit`, the use case finishes the current page
 * and starts a fresh one with the standard footer.
 */
class ExportPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    private val goldRepo: GoldRepository,
    private val landRepo: LandRepository,
    private val depositRepo: DepositRepository,
    private val debtRepo: DebtRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val computeNetWorth: ComputeNetWorthUseCase,
) {

    suspend operator fun invoke(
        periodStart: LocalDate,
        periodEnd: LocalDate,
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            // ── Gather data ───────────────────────────────────────
            val prefs = prefsRepo.prefs.first()
            val accounts = accountRepo.observeActive().first()
            val accountBalances = accounts.associate { acc ->
                acc.id to accountRepo.observeBalance(acc.id).first()
            }
            val accountNameById = accounts.associate { it.id to it.name }

            val netWorth = computeNetWorth().first()

            val income = transactionRepo.observeIncomeBetween(periodStart, periodEnd).first()
            val expense = transactionRepo.observeExpenseBetween(periodStart, periodEnd).first()
            val topCats = transactionRepo
                .observeTopExpenseCategories(periodStart, periodEnd, limit = 8)
                .first()
            val recentTxns = transactionRepo.observeRecent(limit = 20).first()

            // ── Render the document ───────────────────────────────
            val pdf = PdfDocument()
            try {
                val ctx = RenderContext(
                    pdf = pdf,
                    nickname = prefs.userNickname.ifBlank { "Teman" },
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                )
                ctx.beginPage()
                renderHeader(ctx)
                renderNetWorth(ctx, netWorth)
                renderAccounts(ctx, accounts, accountBalances)
                renderIncomeExpense(ctx, income, expense)
                renderTopCategories(ctx, topCats)
                renderRecentTransactions(ctx, recentTxns, accountNameById)
                ctx.finishPage()

                // ── Write to cacheDir/exports/ ────────────────────
                val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val stamp = DateTimeFormatter
                    .ofPattern("yyyyMMdd")
                    .format(LocalDate.now())
                val outFile = File(exportsDir, "sakuwise-report-$stamp.pdf")
                FileOutputStream(outFile).use { pdf.writeTo(it) }

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outFile,
                )
            } finally {
                pdf.close()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────

    /** Mutable render cursor shared across section helpers. */
    private class RenderContext(
        val pdf: PdfDocument,
        val nickname: String,
        val periodStart: LocalDate,
        val periodEnd: LocalDate,
    ) {
        var pageNo: Int = 0
        var page: PdfDocument.Page? = null
        var y: Float = 0f

        fun beginPage() {
            pageNo += 1
            val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create()
            page = pdf.startPage(info)
            y = MARGIN
        }

        fun finishPage() {
            val p = page ?: return
            // Inline footer — drawn here so RenderContext stays self-contained.
            val canvas = p.canvas
            val footY = PAGE_H - MARGIN + 14f
            val footerPaint = Paint().apply {
                isAntiAlias = true; textSize = 8f
                typeface = Typeface.DEFAULT; color = INK_SUBTLE
            }
            val left = "Diekspor pada ${LocalDate.now().toAbsoluteId()}"
            canvas.drawText(left, MARGIN, footY, footerPaint)
            val right = "Halaman $pageNo"
            canvas.drawText(
                right,
                PAGE_W - MARGIN - footerPaint.measureText(right),
                footY, footerPaint,
            )
            pdf.finishPage(p)
            page = null
        }

        /** Ensure at least [needed] pts of vertical room; otherwise paginate. */
        fun ensureRoom(needed: Float) {
            if (y + needed > BOTTOM_LIMIT) {
                finishPage()
                beginPage()
            }
        }
    }

    private fun renderHeader(ctx: RenderContext) {
        val page = ctx.page ?: return
        val canvas = page.canvas

        // Wordmark "Sakuwise" (top-left). The "S" gets the brand green tint.
        val brand = "Sakuwise"
        val sx = MARGIN
        var x = sx
        // Squircle-ish brand mark: a small filled rounded square as the "logo".
        val markSize = 22f
        canvas.drawRoundRect(
            x, ctx.y, x + markSize, ctx.y + markSize,
            6f, 6f, fillPaint(BRAND_GREEN),
        )
        val markLetter = "S"
        val markLetterPaint = textPaint(11f, Typeface.DEFAULT_BOLD, Color.WHITE)
        val markLetterWidth = markLetterPaint.measureText(markLetter)
        canvas.drawText(
            markLetter,
            x + (markSize - markLetterWidth) / 2f,
            ctx.y + markSize / 2f + 4f,
            markLetterPaint,
        )
        x += markSize + 8f

        val wordPaint = textPaint(20f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), INK)
        canvas.drawText(brand, x, ctx.y + 17f, wordPaint)

        // Right-aligned report meta
        val labelPaint = textPaint(8f, Typeface.DEFAULT_BOLD, INK_SUBTLE)
        val periodLabel = "LAPORAN " +
            "${ctx.periodStart.toAbsoluteId()} — ${ctx.periodEnd.toAbsoluteId()}"
        val rightX = PAGE_W - MARGIN
        canvas.drawText(
            "LAPORAN KEUANGAN",
            rightX - labelPaint.measureText("LAPORAN KEUANGAN"),
            ctx.y + 6f,
            labelPaint,
        )
        val periodPaint = textPaint(10f, Typeface.DEFAULT_BOLD, INK)
        canvas.drawText(
            periodLabel.removePrefix("LAPORAN "),
            rightX - periodPaint.measureText(periodLabel.removePrefix("LAPORAN ")),
            ctx.y + 19f,
            periodPaint,
        )
        val ownerPaint = textPaint(9f, Typeface.DEFAULT, INK_MUTED)
        val ownerLine = "Untuk: ${ctx.nickname}"
        canvas.drawText(
            ownerLine,
            rightX - ownerPaint.measureText(ownerLine),
            ctx.y + 32f,
            ownerPaint,
        )

        ctx.y += 44f
        // Brand rule
        canvas.drawRect(
            MARGIN, ctx.y, PAGE_W - MARGIN, ctx.y + 1.5f,
            fillPaint(BRAND_GREEN),
        )
        ctx.y += 14f
    }

    private fun renderNetWorth(
        ctx: RenderContext,
        nw: ComputeNetWorthUseCase.NetWorth,
    ) {
        ctx.ensureRoom(110f)
        sectionTitle(ctx, "Net Worth")

        val page = ctx.page!!
        val canvas = page.canvas
        // Big "Total" number on a brand-green pill
        val pillH = 56f
        canvas.drawRoundRect(
            MARGIN, ctx.y, PAGE_W - MARGIN, ctx.y + pillH,
            8f, 8f, fillPaint(BRAND_GREEN),
        )
        val labelPaint = textPaint(8f, Typeface.DEFAULT_BOLD, Color.WHITE)
        canvas.drawText("TOTAL NET WORTH", MARGIN + 12f, ctx.y + 14f, labelPaint)
        val totalPaint = textPaint(22f, Typeface.DEFAULT_BOLD, Color.WHITE)
        canvas.drawText(
            nw.total.toRupiah(),
            MARGIN + 12f, ctx.y + 40f,
            totalPaint,
        )
        ctx.y += pillH + 8f

        // Breakdown rows
        val rows = listOf(
            "Akun" to nw.accountsTotal,
            "Emas" to nw.goldTotal,
            "Properti / Tanah" to nw.landTotal,
            "Deposito" to nw.depositTotal,
            "− Hutang" to -nw.debtsTotal,
        )
        for ((label, value) in rows) {
            ctx.ensureRoom(16f)
            keyValueRow(ctx, label, value.toRupiah())
        }
        ctx.y += 10f
    }

    private fun renderAccounts(
        ctx: RenderContext,
        accounts: List<com.gustiadhitya.sakuwise.core.domain.model.Account>,
        balances: Map<String, Long>,
    ) {
        ctx.ensureRoom(40f)
        sectionTitle(ctx, "Akun")
        if (accounts.isEmpty()) {
            mutedLine(ctx, context.getString(com.gustiadhitya.sakuwise.R.string.export_pdf_no_accounts))
            return
        }
        // Header row
        tableHeader(ctx, listOf("Nama" to 0.45f, "Tipe" to 0.25f, "Saldo" to 0.30f))
        for (acc in accounts) {
            ctx.ensureRoom(18f)
            tableRow(
                ctx,
                listOf(
                    acc.name to 0.45f,
                    typeLabel(acc.type) to 0.25f,
                    (balances[acc.id] ?: 0L).toRupiah() to 0.30f,
                ),
                rightAlignLast = true,
            )
        }
        ctx.y += 10f
    }

    private fun renderIncomeExpense(ctx: RenderContext, income: Long, expense: Long) {
        ctx.ensureRoom(80f)
        sectionTitle(ctx, context.getString(com.gustiadhitya.sakuwise.R.string.export_pdf_income_vs_expense))
        val page = ctx.page!!
        val canvas = page.canvas
        val cellW = (PAGE_W - 2 * MARGIN - 8f) / 2f
        val cellH = 50f

        // Income cell
        canvas.drawRoundRect(
            MARGIN, ctx.y, MARGIN + cellW, ctx.y + cellH,
            6f, 6f, fillPaint(SOFT_BG),
        )
        canvas.drawText("PEMASUKAN", MARGIN + 10f, ctx.y + 14f,
            textPaint(8f, Typeface.DEFAULT_BOLD, INK_SUBTLE))
        canvas.drawText(income.toRupiah(), MARGIN + 10f, ctx.y + 38f,
            textPaint(16f, Typeface.DEFAULT_BOLD, BRAND_GREEN))

        // Expense cell
        val xExp = MARGIN + cellW + 8f
        canvas.drawRoundRect(
            xExp, ctx.y, xExp + cellW, ctx.y + cellH,
            6f, 6f, fillPaint(SOFT_BG),
        )
        canvas.drawText("PENGELUARAN", xExp + 10f, ctx.y + 14f,
            textPaint(8f, Typeface.DEFAULT_BOLD, INK_SUBTLE))
        canvas.drawText(expense.toRupiah(), xExp + 10f, ctx.y + 38f,
            textPaint(16f, Typeface.DEFAULT_BOLD, DANGER_RED))

        ctx.y += cellH + 8f

        val delta = income - expense
        val deltaLabel = if (delta >= 0) "Surplus" else "Defisit"
        val deltaColor = if (delta >= 0) BRAND_GREEN else DANGER_RED
        keyValueRow(ctx, deltaLabel, delta.toRupiah(), valueColor = deltaColor, bold = true)
        ctx.y += 10f
    }

    private fun renderTopCategories(
        ctx: RenderContext,
        cats: List<com.gustiadhitya.sakuwise.core.domain.repository.TopExpenseCategory>,
    ) {
        ctx.ensureRoom(40f)
        sectionTitle(ctx, "Top Pengeluaran")
        if (cats.isEmpty()) {
            mutedLine(ctx, "Tidak ada pengeluaran di periode ini.")
            return
        }
        val max = cats.maxOf { it.total }.coerceAtLeast(1L)
        for (cat in cats) {
            ctx.ensureRoom(22f)
            val page = ctx.page!!
            val canvas = page.canvas
            // Label
            canvas.drawText(
                cat.name, MARGIN, ctx.y + 11f,
                textPaint(10f, Typeface.DEFAULT, INK),
            )
            // Amount (right)
            val amountStr = cat.total.toRupiah()
            val amountPaint = textPaint(10f, Typeface.DEFAULT_BOLD, INK)
            canvas.drawText(
                amountStr,
                PAGE_W - MARGIN - amountPaint.measureText(amountStr),
                ctx.y + 11f, amountPaint,
            )
            // Bar
            val barY = ctx.y + 14f
            val barH = 4f
            val barMaxW = PAGE_W - 2 * MARGIN
            canvas.drawRoundRect(
                MARGIN, barY, MARGIN + barMaxW, barY + barH,
                2f, 2f, fillPaint(TRACK),
            )
            val ratio = cat.total.toDouble() / max.toDouble()
            canvas.drawRoundRect(
                MARGIN, barY,
                MARGIN + (barMaxW * ratio).toFloat(), barY + barH,
                2f, 2f, fillPaint(BRAND_GREEN),
            )
            ctx.y += 22f
        }
        ctx.y += 6f
    }

    private fun renderRecentTransactions(
        ctx: RenderContext,
        txns: List<Transaction>,
        accountNameById: Map<String, String>,
    ) {
        ctx.ensureRoom(40f)
        sectionTitle(ctx, "Transaksi Terakhir (${txns.size})")
        if (txns.isEmpty()) {
            mutedLine(ctx, context.getString(com.gustiadhitya.sakuwise.R.string.export_pdf_no_transactions))
            return
        }
        tableHeader(
            ctx,
            listOf(
                "Tanggal" to 0.18f,
                "Tipe" to 0.14f,
                "Catatan / Akun" to 0.48f,
                "Jumlah" to 0.20f,
            ),
        )
        for (t in txns) {
            ctx.ensureRoom(18f)
            val date = t.date.toAbsoluteId()
            val type = typeShort(t.type)
            val accName = accountNameById[t.sourceAccountId] ?: "—"
            val noteOrAcct = (t.note?.take(40) ?: accName)
            val signed = when (t.type) {
                TxnType.Expense -> -t.amount
                else -> t.amount
            }
            tableRow(
                ctx,
                listOf(
                    date to 0.18f,
                    type to 0.14f,
                    noteOrAcct to 0.48f,
                    signed.toRupiah() to 0.20f,
                ),
                rightAlignLast = true,
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Drawing primitives
    // ─────────────────────────────────────────────────────────────

    private fun sectionTitle(ctx: RenderContext, text: String) {
        ctx.ensureRoom(22f)
        val page = ctx.page!!
        val canvas = page.canvas
        canvas.drawText(
            text.uppercase(),
            MARGIN, ctx.y + 9f,
            textPaint(9f, Typeface.DEFAULT_BOLD, BRAND_GREEN),
        )
        // Underline rule
        canvas.drawRect(
            MARGIN, ctx.y + 13f, PAGE_W - MARGIN, ctx.y + 13.6f,
            fillPaint(BORDER),
        )
        ctx.y += 20f
    }

    private fun keyValueRow(
        ctx: RenderContext,
        key: String,
        value: String,
        valueColor: Int = INK,
        bold: Boolean = false,
    ) {
        val page = ctx.page!!
        val canvas = page.canvas
        canvas.drawText(
            key, MARGIN, ctx.y + 10f,
            textPaint(10f, Typeface.DEFAULT, INK_MUTED),
        )
        val typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        val vPaint = textPaint(10f, typeface, valueColor)
        canvas.drawText(
            value,
            PAGE_W - MARGIN - vPaint.measureText(value),
            ctx.y + 10f, vPaint,
        )
        ctx.y += 14f
    }

    private fun mutedLine(ctx: RenderContext, text: String) {
        ctx.ensureRoom(14f)
        val page = ctx.page!!
        val canvas = page.canvas
        canvas.drawText(
            text, MARGIN, ctx.y + 10f,
            textPaint(9f, Typeface.DEFAULT, INK_SUBTLE),
        )
        ctx.y += 18f
    }

    private fun tableHeader(ctx: RenderContext, cols: List<Pair<String, Float>>) {
        ctx.ensureRoom(16f)
        val page = ctx.page!!
        val canvas = page.canvas
        val total = PAGE_W - 2 * MARGIN
        var x = MARGIN
        for ((label, weight) in cols) {
            val w = total * weight
            canvas.drawText(
                label.uppercase(), x + 2f, ctx.y + 9f,
                textPaint(7.5f, Typeface.DEFAULT_BOLD, INK_SUBTLE),
            )
            x += w
        }
        ctx.y += 12f
        // Header separator
        canvas.drawRect(
            MARGIN, ctx.y, PAGE_W - MARGIN, ctx.y + 0.6f,
            fillPaint(BORDER),
        )
        ctx.y += 4f
    }

    private fun tableRow(
        ctx: RenderContext,
        cells: List<Pair<String, Float>>,
        rightAlignLast: Boolean,
    ) {
        val page = ctx.page!!
        val canvas = page.canvas
        val total = PAGE_W - 2 * MARGIN
        var x = MARGIN
        val lastIdx = cells.lastIndex
        cells.forEachIndexed { i, (text, weight) ->
            val w = total * weight
            val paint = textPaint(9.5f, Typeface.DEFAULT, INK)
            val clipped = ellipsize(text, paint, w - 4f)
            if (rightAlignLast && i == lastIdx) {
                val tw = paint.measureText(clipped)
                canvas.drawText(clipped, x + w - tw - 2f, ctx.y + 10f, paint)
            } else {
                canvas.drawText(clipped, x + 2f, ctx.y + 10f, paint)
            }
            x += w
        }
        ctx.y += 14f
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private fun textPaint(sizePt: Float, typeface: Typeface?, color: Int): Paint = Paint().apply {
        isAntiAlias = true
        textSize = sizePt
        this.typeface = typeface ?: Typeface.DEFAULT
        this.color = color
    }

    private fun fillPaint(color: Int): Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.color = color
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 1 && paint.measureText(text.substring(0, end) + "…") > maxWidth) {
            end--
        }
        return text.substring(0, end) + "…"
    }

    private fun typeLabel(type: AccountType): String = when (type) {
        AccountType.Cash -> "Tunai"
        AccountType.Bank -> "Bank"
        AccountType.EWallet -> "E-Wallet"
        AccountType.Other -> "Lainnya"
    }

    private fun typeShort(type: TxnType): String = when (type) {
        TxnType.Income -> "Masuk"
        TxnType.Expense -> "Keluar"
        TxnType.Transfer -> "Transfer"
        TxnType.DebtInflow -> "Hutang+"
        TxnType.DebtOutflow -> "Bayar"
        TxnType.Reconciliation -> "Recon"
    }

    private companion object {
        // A4 portrait in PostScript points (1pt = 1/72 in).
        const val PAGE_W = 595
        const val PAGE_H = 842
        const val MARGIN = 36f
        const val BOTTOM_LIMIT = (PAGE_H - MARGIN - 20f) // leave room for footer

        // Colors — kept inline so the PDF renders identically regardless of theme.
        const val BRAND_GREEN = 0xFF0F4C3A.toInt()
        const val INK = 0xFF1A2520.toInt()
        const val INK_MUTED = 0xFF5C6963.toInt()
        const val INK_SUBTLE = 0xFF8B948F.toInt()
        const val BORDER = 0xFFE8E0CC.toInt()
        const val SOFT_BG = 0xFFFAF7F0.toInt()
        const val TRACK = 0xFFE8E0CC.toInt()
        const val DANGER_RED = 0xFFB23A2E.toInt()
    }
}
