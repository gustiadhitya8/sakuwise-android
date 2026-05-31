package com.gustiadhitya.sakuwise.feature.settings.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.gustiadhitya.sakuwise.R
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
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
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
private data class PlanItemRow(
    val categoryName: String,
    val itemName: String,
    val planAmount: Long,
    val actualAmount: Long,
)

class ExportPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    private val planRepo: PlanRepository,
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
            // Build per-plan-item actual amounts for the period
            val periodTxns = transactionRepo.observeBetween(periodStart, periodEnd).first()
            val actualByPlanItem = periodTxns
                .filter { it.type == TxnType.Expense && it.planItemId != null }
                .groupBy { it.planItemId!! }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }

            // Collect plan item rows — prefer plan active at period end, fall back to most recent
            val planItemRows = mutableListOf<PlanItemRow>()
            val activePlan = planRepo.observeForDate(periodEnd).first()
                ?: planRepo.observeAll().first().maxByOrNull { it.end }
            if (activePlan != null) {
                val allocations = planRepo.observeAllocations(activePlan.id).first()
                for (alloc in allocations) {
                    val categories = planRepo.observeCategories(alloc.id).first()
                    for (cat in categories) {
                        val items = planRepo.observePlanItems(cat.id).first()
                        for (pi in items) {
                            planItemRows.add(
                                PlanItemRow(
                                    categoryName = cat.name,
                                    itemName     = pi.name,
                                    planAmount   = pi.plannedAmount,
                                    actualAmount = actualByPlanItem[pi.id] ?: 0L,
                                ),
                            )
                        }
                    }
                }
            }

            // ── Render the document ───────────────────────────────
            val pdf = PdfDocument()
            try {
                val ctx = RenderContext(
                    pdf = pdf,
                    nickname = prefs.userNickname.ifBlank { context.getString(R.string.default_nickname) },
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                    exportedAtFormat = context.getString(R.string.export_pdf_exported_at_format, "%1\$s"),
                    pageLabelFormat = context.getString(R.string.export_pdf_page_label_format, "%1\$d"),
                )
                ctx.beginPage()
                renderHeader(ctx)
                renderNetWorth(ctx, netWorth)
                renderAccounts(ctx, accounts, accountBalances)
                renderIncomeExpense(ctx, income, expense)
                renderTopCategories(ctx, topCats)
                renderPlanItemTable(ctx, planItemRows)
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
        val exportedAtFormat: String,
        val pageLabelFormat: String,
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
            val left = String.format(exportedAtFormat, LocalDate.now().toAbsoluteId())
            canvas.drawText(left, MARGIN, footY, footerPaint)
            val right = String.format(pageLabelFormat, pageNo)
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
        val periodLabel = "${ctx.periodStart.toAbsoluteId()} — ${ctx.periodEnd.toAbsoluteId()}"
        val rightX = PAGE_W - MARGIN
        val reportTitle = context.getString(R.string.export_pdf_report_title)
        canvas.drawText(
            reportTitle,
            rightX - labelPaint.measureText(reportTitle),
            ctx.y + 6f,
            labelPaint,
        )
        val periodPaint = textPaint(10f, Typeface.DEFAULT_BOLD, INK)
        canvas.drawText(
            periodLabel,
            rightX - periodPaint.measureText(periodLabel),
            ctx.y + 19f,
            periodPaint,
        )
        val ownerPaint = textPaint(9f, Typeface.DEFAULT, INK_MUTED)
        val ownerLine = context.getString(R.string.export_pdf_for_format, ctx.nickname)
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
        sectionTitle(ctx, context.getString(R.string.export_pdf_net_worth))

        val page = ctx.page!!
        val canvas = page.canvas
        // Big "Total" number on a brand-green pill
        val pillH = 56f
        canvas.drawRoundRect(
            MARGIN, ctx.y, PAGE_W - MARGIN, ctx.y + pillH,
            8f, 8f, fillPaint(BRAND_GREEN),
        )
        val labelPaint = textPaint(8f, Typeface.DEFAULT_BOLD, Color.WHITE)
        canvas.drawText(context.getString(R.string.export_pdf_total_net_worth), MARGIN + 12f, ctx.y + 14f, labelPaint)
        val totalPaint = textPaint(22f, Typeface.DEFAULT_BOLD, Color.WHITE)
        canvas.drawText(
            nw.total.toRupiah(),
            MARGIN + 12f, ctx.y + 40f,
            totalPaint,
        )
        ctx.y += pillH + 8f

        // Breakdown rows
        val rows = listOf(
            context.getString(R.string.export_pdf_accounts) to nw.accountsTotal,
            context.getString(R.string.export_pdf_gold) to nw.goldTotal,
            context.getString(R.string.export_pdf_land) to nw.landTotal,
            context.getString(R.string.export_pdf_deposit) to nw.depositTotal,
            context.getString(R.string.export_pdf_minus_debt) to -nw.debtsTotal,
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
        sectionTitle(ctx, context.getString(R.string.export_pdf_accounts))
        if (accounts.isEmpty()) {
            mutedLine(ctx, context.getString(com.gustiadhitya.sakuwise.R.string.export_pdf_no_accounts))
            return
        }
        // Header row
        tableHeader(ctx, listOf(
            context.getString(R.string.export_pdf_name) to 0.45f,
            context.getString(R.string.export_pdf_type) to 0.25f,
            context.getString(R.string.export_pdf_balance) to 0.30f,
        ))
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
        canvas.drawText(context.getString(R.string.export_pdf_income), MARGIN + 10f, ctx.y + 14f,
            textPaint(8f, Typeface.DEFAULT_BOLD, INK_SUBTLE))
        canvas.drawText(income.toRupiah(), MARGIN + 10f, ctx.y + 38f,
            textPaint(16f, Typeface.DEFAULT_BOLD, BRAND_GREEN))

        // Expense cell
        val xExp = MARGIN + cellW + 8f
        canvas.drawRoundRect(
            xExp, ctx.y, xExp + cellW, ctx.y + cellH,
            6f, 6f, fillPaint(SOFT_BG),
        )
        canvas.drawText(context.getString(R.string.export_pdf_expense), xExp + 10f, ctx.y + 14f,
            textPaint(8f, Typeface.DEFAULT_BOLD, INK_SUBTLE))
        canvas.drawText(expense.toRupiah(), xExp + 10f, ctx.y + 38f,
            textPaint(16f, Typeface.DEFAULT_BOLD, DANGER_RED))

        ctx.y += cellH + 8f

        val delta = income - expense
        val deltaLabel = if (delta >= 0) context.getString(R.string.export_pdf_surplus) else context.getString(R.string.export_pdf_deficit)
        val deltaColor = if (delta >= 0) BRAND_GREEN else DANGER_RED
        keyValueRow(ctx, deltaLabel, delta.toRupiah(), valueColor = deltaColor, bold = true)
        ctx.y += 10f
    }

    private fun renderTopCategories(
        ctx: RenderContext,
        cats: List<com.gustiadhitya.sakuwise.core.domain.repository.TopExpenseCategory>,
    ) {
        ctx.ensureRoom(40f)
        sectionTitle(ctx, context.getString(R.string.export_pdf_top_expenses))
        if (cats.isEmpty()) {
            mutedLine(ctx, context.getString(R.string.export_pdf_no_expenses))
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

    private fun renderPlanItemTable(
        ctx: RenderContext,
        rows: List<PlanItemRow>,
    ) {
        ctx.ensureRoom(40f)
        sectionTitle(ctx, context.getString(R.string.export_pdf_plan_vs_actual))
        if (rows.isEmpty()) {
            mutedLine(ctx, context.getString(R.string.export_pdf_no_plan_items))
            return
        }

        val tableW = PAGE_W - 2 * MARGIN
        // Column widths: item name gets the most space; amounts + pct share the right side
        val cItem  = tableW * 0.40f
        val cPlan  = tableW * 0.22f
        val cAct   = tableW * 0.22f
        val cPct   = tableW * 0.16f

        // ── Table header (brand green pill) ──────────────────────
        ctx.ensureRoom(24f)
        ctx.page!!.canvas.let { c ->
            c.drawRoundRect(MARGIN, ctx.y, MARGIN + tableW, ctx.y + 24f, 5f, 5f, fillPaint(BRAND_GREEN))
            val hp = textPaint(8f, Typeface.DEFAULT_BOLD, Color.WHITE)
            c.drawText(context.getString(R.string.export_pdf_item_category), MARGIN + 8f, ctx.y + 16f, hp)
            val rLabel = context.getString(R.string.export_pdf_planned)
            c.drawText(rLabel, MARGIN + cItem + cPlan - hp.measureText(rLabel) - 4f, ctx.y + 16f, hp)
            val aLabel = context.getString(R.string.export_pdf_actual)
            c.drawText(aLabel, MARGIN + cItem + cPlan + cAct - hp.measureText(aLabel) - 4f, ctx.y + 16f, hp)
            val pLabel = "%"
            c.drawText(pLabel, MARGIN + tableW - hp.measureText(pLabel) - 6f, ctx.y + 16f, hp)
        }
        ctx.y += 24f

        // ── Rows grouped by category ──────────────────────────────
        val grouped = rows.groupBy { it.categoryName }
        var rowIdx = 0
        var totalPlan   = 0L
        var totalActual = 0L

        for ((catName, catItems) in grouped) {
            // Category header row — light green tint
            ctx.ensureRoom(20f)
            ctx.page!!.canvas.let { c ->
                c.drawRect(MARGIN, ctx.y, MARGIN + tableW, ctx.y + 20f, fillPaint(CATEGORY_BG))
                c.drawText(
                    "▸ $catName",
                    MARGIN + 8f, ctx.y + 14f,
                    textPaint(8.5f, Typeface.DEFAULT_BOLD, BRAND_GREEN),
                )
            }
            ctx.y += 20f

            for (row in catItems) {
                val rowH = 17f
                ctx.ensureRoom(rowH)
                val rowBg = if (rowIdx % 2 == 0) Color.WHITE else SOFT_BG
                val pctVal = if (row.planAmount > 0L) row.actualAmount * 100L / row.planAmount else null
                val pctStr = pctVal?.let { "$it%" } ?: "—"
                val pctColor = when {
                    pctVal == null -> INK_SUBTLE
                    pctVal > 100   -> DANGER_RED
                    else           -> BRAND_GREEN
                }
                ctx.page!!.canvas.let { c ->
                    c.drawRect(MARGIN, ctx.y, MARGIN + tableW, ctx.y + rowH, fillPaint(rowBg))
                    val ip = textPaint(9f, Typeface.DEFAULT, INK)
                    c.drawText(
                        ellipsize(row.itemName, ip, cItem - 14f),
                        MARGIN + 14f, ctx.y + 12f, ip,
                    )
                    val planStr = row.planAmount.toRupiah()
                    val pp = textPaint(8.5f, Typeface.DEFAULT, INK_MUTED)
                    c.drawText(planStr, MARGIN + cItem + cPlan - pp.measureText(planStr) - 4f, ctx.y + 12f, pp)
                    val actStr = row.actualAmount.toRupiah()
                    val ap = textPaint(8.5f, Typeface.DEFAULT_BOLD, INK)
                    c.drawText(actStr, MARGIN + cItem + cPlan + cAct - ap.measureText(actStr) - 4f, ctx.y + 12f, ap)
                    val ppp = textPaint(8.5f, Typeface.DEFAULT_BOLD, pctColor)
                    c.drawText(pctStr, MARGIN + tableW - ppp.measureText(pctStr) - 6f, ctx.y + 12f, ppp)
                }
                ctx.y += rowH
                rowIdx++
                totalPlan   += row.planAmount
                totalActual += row.actualAmount
            }
            // Thin divider between category groups
            ctx.page!!.canvas.drawRect(MARGIN, ctx.y, MARGIN + tableW, ctx.y + 0.6f, fillPaint(BORDER))
        }

        // ── Totals row ────────────────────────────────────────────
        ctx.ensureRoom(22f)
        val totPctVal = if (totalPlan > 0L) totalActual * 100L / totalPlan else null
        val totPctStr = totPctVal?.let { "$it%" } ?: "—"
        val totPctColor = if ((totPctVal ?: 0L) > 100L) DANGER_RED else BRAND_GREEN
        ctx.page!!.canvas.let { c ->
            c.drawRoundRect(MARGIN, ctx.y, MARGIN + tableW, ctx.y + 22f, 0f, 0f, fillPaint(TOTAL_BG))
            val tp = textPaint(9f, Typeface.DEFAULT_BOLD, INK)
            c.drawText(context.getString(R.string.export_pdf_total), MARGIN + 8f, ctx.y + 15f, tp)
            val psStr = totalPlan.toRupiah()
            c.drawText(psStr, MARGIN + cItem + cPlan - tp.measureText(psStr) - 4f, ctx.y + 15f, tp)
            val actColor = if (totalActual > totalPlan) DANGER_RED else BRAND_GREEN
            val ap = textPaint(9f, Typeface.DEFAULT_BOLD, actColor)
            val asStr = totalActual.toRupiah()
            c.drawText(asStr, MARGIN + cItem + cPlan + cAct - ap.measureText(asStr) - 4f, ctx.y + 15f, ap)
            val ppp = textPaint(9f, Typeface.DEFAULT_BOLD, totPctColor)
            c.drawText(totPctStr, MARGIN + tableW - ppp.measureText(totPctStr) - 6f, ctx.y + 15f, ppp)
        }
        ctx.y += 22f
        ctx.page!!.canvas.drawRect(MARGIN, ctx.y, MARGIN + tableW, ctx.y + 1f, fillPaint(BORDER))
        ctx.y += 12f
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

    private fun typeLabel(type: AccountType): String = context.getString(
        when (type) {
            AccountType.Cash -> R.string.export_pdf_acctype_cash
            AccountType.Bank -> R.string.export_pdf_acctype_bank
            AccountType.EWallet -> R.string.export_pdf_acctype_ewallet
            AccountType.Other -> R.string.export_pdf_acctype_other
        },
    )

    private fun typeShort(type: TxnType): String = context.getString(
        when (type) {
            TxnType.Income -> R.string.export_pdf_txntype_income
            TxnType.Expense -> R.string.export_pdf_txntype_expense
            TxnType.Transfer -> R.string.export_pdf_txntype_transfer
            TxnType.DebtInflow -> R.string.export_pdf_txntype_debt
            TxnType.DebtOutflow -> R.string.export_pdf_txntype_payment
            TxnType.Reconciliation -> R.string.export_pdf_txntype_recon
        },
    )

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
        const val CATEGORY_BG = 0xFFDCEDE8.toInt() // Light teal-green for category group rows
        const val TOTAL_BG = 0xFFE4EDEA.toInt()    // Slightly darker tint for totals row
    }
}
