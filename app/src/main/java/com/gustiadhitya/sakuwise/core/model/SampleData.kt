package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

/**
 * Verbatim port of proto/data.jsx so the dashboard reads exactly like the
 * prototype while we don't yet have a real data layer. Don't drift this file
 * out of sync with the prototype unless the prototype itself changes.
 */
object SampleData {
    val incomeMonth: Long = 15_500_000L

    val accounts = listOf(
        Account("mandiri", "Mandiri", AccountType.Bank, 12_450_000L),
        Account("bca", "BCA", AccountType.Bank, 8_200_000L),
        Account("gopay", "GoPay", AccountType.EWallet, 280_000L),
        Account("tunai", "Tunai", AccountType.Cash, 540_000L),
    )

    val allocations = listOf(
        Allocation(AllocationId.Needs,  "Needs",      50, plan = 7_750_000L, used = 5_180_000L),
        Allocation(AllocationId.Wants,  "Wants",      30, plan = 4_650_000L, used = 3_120_000L),
        Allocation(AllocationId.Invest, "Investment", 20, plan = 3_100_000L, used = 2_500_000L),
    )

    val topCategories = listOf(
        TopCategory("Sewa/Cicilan",     2_500_000L, ColorTone.Primary),
        TopCategory("Makan Harian",       980_000L, ColorTone.Accent),
        TopCategory("Tabungan Reguler", 1_000_000L, ColorTone.Info),
        TopCategory("Kopi/Kafe",          580_000L, ColorTone.Warning),
        TopCategory("Restoran",           510_000L, ColorTone.Danger),
    )

    // Today is locked to the prototype's reference date (2026-05-15).
    private val today: LocalDate = LocalDate.of(2026, 5, 15)

    val transactions: List<Transaction> = listOf(
        Transaction("t1", TxnType.Expense,    28_000L, today,                  "Kopi/Kafe",      "Kopi Kenangan",     "gopay"),
        Transaction("t2", TxnType.Expense,    35_000L, today,                  "Makan Harian",   "Warteg Bahari",     "tunai"),
        Transaction("t3", TxnType.Expense,   285_000L, today.minusDays(1),     "Listrik",        "PLN",               "mandiri"),
        Transaction("t4", TxnType.Expense,   150_000L, today.minusDays(1),     "BBM",            "Pertamina",         "mandiri"),
        Transaction("t5", TxnType.Income, 15_000_000L, today.minusDays(2),     "Gaji Pokok",     "PT. Sumber Karya",  "mandiri"),
        Transaction("t6", TxnType.Expense,   220_000L, today.minusDays(3),     "Restoran",       "Sushi Tei",         "bca"),
        Transaction("t7", TxnType.Transfer,2_000_000L, today.minusDays(3),     "Transfer",       "BCA → Mandiri",     "bca"),
        Transaction("t8", TxnType.Expense,   200_000L, today.minusDays(4),     "Streaming",      "Netflix",           "bca"),
        Transaction("t9", TxnType.Expense,   350_000L, today.minusDays(5),     "Skincare",       "Watsons",           "gopay"),
        Transaction("t10",TxnType.Expense,   165_000L, today.minusDays(6),     "Air PAM",        "PDAM",              "mandiri"),
    )

    val period = PeriodInfo(label = "Plan Mei", daysLeft = 16)
}
