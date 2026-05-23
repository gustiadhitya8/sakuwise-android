package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AccountSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.Category
import com.gustiadhitya.sakuwise.core.domain.model.Debt
import com.gustiadhitya.sakuwise.core.domain.model.DebtPayment
import com.gustiadhitya.sakuwise.core.domain.model.DepositAsset
import com.gustiadhitya.sakuwise.core.domain.model.DepositSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.GoldAsset
import com.gustiadhitya.sakuwise.core.domain.model.IncomeCategory
import com.gustiadhitya.sakuwise.core.domain.model.LandAsset
import com.gustiadhitya.sakuwise.core.domain.model.LandTaxPayment
import com.gustiadhitya.sakuwise.core.domain.model.Plan
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AccountRepository {
    fun observeActive(): Flow<List<Account>>
    fun observeAll(): Flow<List<Account>>
    fun observeById(id: String): Flow<Account?>
    fun observeBalance(id: String): Flow<Long>
    fun observeTotalBalance(): Flow<Long>
    suspend fun upsert(account: Account)
    suspend fun setArchived(id: String, archived: Boolean)
    suspend fun delete(id: String)
    fun observeSnapshots(accountId: String): Flow<List<AccountSnapshot>>
    suspend fun insertSnapshot(snapshot: AccountSnapshot)
    suspend fun upsertSnapshot(snapshot: AccountSnapshot)
    suspend fun deleteSnapshot(snapshotId: String)
}

interface PlanRepository {
    fun observeAll(): Flow<List<Plan>>
    fun observeForDate(date: LocalDate): Flow<Plan?>
    fun observeById(id: String): Flow<Plan?>
    suspend fun upsert(plan: Plan)

    fun observeAllocations(planId: String): Flow<List<Allocation>>
    suspend fun upsertAllocation(allocation: Allocation)

    fun observeCategories(allocationId: String): Flow<List<Category>>
    suspend fun upsertCategory(category: Category)
    suspend fun deleteCategory(id: String)

    fun observePlanItems(categoryId: String): Flow<List<PlanItem>>
    suspend fun getPlanItem(id: String): PlanItem?
    fun observePlanItem(id: String): Flow<PlanItem?>
    suspend fun upsertPlanItem(item: PlanItem)
    suspend fun deletePlanItem(id: String)
    fun observePlanItemUsed(planItemId: String): Flow<Long>
}

interface TransactionRepository {
    fun observeRecent(limit: Int = 50): Flow<List<Transaction>>
    fun observeForAccount(accountId: String): Flow<List<Transaction>>
    fun observeForPlanItem(planItemId: String): Flow<List<Transaction>>
    suspend fun getById(id: String): Transaction?
    suspend fun upsert(transaction: Transaction)
    suspend fun delete(id: String)
    suspend fun deleteAll()
    fun observeIncomeBetween(start: LocalDate, end: LocalDate): Flow<Long>
    fun observeExpenseBetween(start: LocalDate, end: LocalDate): Flow<Long>
    fun observeIncomeCategories(): Flow<List<IncomeCategory>>
    fun observeTopExpenseCategories(start: LocalDate, end: LocalDate, limit: Int): Flow<List<TopExpenseCategory>>
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
}

data class TopExpenseCategory(val name: String, val total: Long)

interface GoldRepository {
    fun observeAll(): Flow<List<GoldAsset>>
    fun observeById(id: String): Flow<GoldAsset?>
    suspend fun upsert(gold: GoldAsset)
    suspend fun delete(id: String)
}

interface LandRepository {
    fun observeAll(): Flow<List<LandAsset>>
    fun observeById(id: String): Flow<LandAsset?>
    suspend fun upsert(land: LandAsset)
    suspend fun delete(id: String)
    fun observeTaxPayments(landId: String): Flow<List<LandTaxPayment>>
    suspend fun upsertTaxPayment(payment: LandTaxPayment)
    suspend fun deleteTaxPayment(id: String)
}

interface DepositRepository {
    fun observeAll(): Flow<List<DepositAsset>>
    fun observeById(id: String): Flow<DepositAsset?>
    suspend fun upsert(deposit: DepositAsset)
    suspend fun delete(id: String)
    fun observeSnapshots(depositId: String): Flow<List<DepositSnapshot>>
    fun observeLatestSnapshot(depositId: String): Flow<DepositSnapshot?>
    suspend fun upsertSnapshot(snapshot: DepositSnapshot)
}

interface DebtRepository {
    fun observeAll(): Flow<List<Debt>>
    fun observeById(id: String): Flow<Debt?>
    suspend fun upsert(debt: Debt)
    fun observePayments(debtId: String): Flow<List<DebtPayment>>
    fun observePaidTotal(debtId: String): Flow<Long>
    suspend fun upsertPayment(payment: DebtPayment)
    suspend fun deletePayment(id: String)
}
