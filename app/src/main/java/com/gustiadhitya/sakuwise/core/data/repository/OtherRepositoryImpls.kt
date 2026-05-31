package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.DebtDao
import com.gustiadhitya.sakuwise.core.database.dao.DepositDao
import com.gustiadhitya.sakuwise.core.database.dao.GoldDao
import com.gustiadhitya.sakuwise.core.database.dao.LandDao
import com.gustiadhitya.sakuwise.core.database.dao.TransactionDao
import com.gustiadhitya.sakuwise.core.domain.model.Debt
import com.gustiadhitya.sakuwise.core.domain.model.DebtPayment
import com.gustiadhitya.sakuwise.core.domain.model.DepositAsset
import com.gustiadhitya.sakuwise.core.domain.model.DepositSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.GoldAsset
import com.gustiadhitya.sakuwise.core.domain.model.IncomeCategory
import com.gustiadhitya.sakuwise.core.domain.model.LandAsset
import com.gustiadhitya.sakuwise.core.domain.model.LandTaxPayment
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DepositRepository
import com.gustiadhitya.sakuwise.core.domain.repository.GoldRepository
import com.gustiadhitya.sakuwise.core.domain.repository.LandRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TopExpenseCategory
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
) : TransactionRepository {
    override fun observeRecent(limit: Int) = dao.observeRecent(limit).map { l -> l.map { it.toDomain() } }
    override fun observeForAccount(accountId: String) =
        dao.observeForAccount(accountId).map { l -> l.map { it.toDomain() } }
    override fun observeForPlanItem(planItemId: String) =
        dao.observeForPlanItem(planItemId).map { l -> l.map { it.toDomain() } }
    override suspend fun getById(id: String): Transaction? = dao.getById(id)?.toDomain()
    override suspend fun upsert(transaction: Transaction) = dao.upsert(transaction.toEntity())
    override suspend fun upsertTransferWithFee(transfer: Transaction, fee: Transaction?) =
        dao.upsertTransferWithFee(transfer.toEntity(), fee?.toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
    override suspend fun deleteAll() = dao.deleteAll()
    override fun observeIncomeBetween(start: LocalDate, end: LocalDate) =
        dao.observeIncomeBetween(start.toEpochDay(), end.toEpochDay())
    override fun observeExpenseBetween(start: LocalDate, end: LocalDate) =
        dao.observeExpenseBetween(start.toEpochDay(), end.toEpochDay())
    override fun observeIncomeCategories(): Flow<List<IncomeCategory>> =
        dao.observeIncomeCategories().map { l -> l.map { it.toDomain() } }
    override fun observeTopExpenseCategories(start: LocalDate, end: LocalDate, limit: Int) =
        dao.observeTopExpenseCategories(start.toEpochDay(), end.toEpochDay(), limit).map { rows ->
            rows.map { TopExpenseCategory(name = it.categoryName, total = it.total) }
        }
    override fun observeBetween(start: LocalDate, end: LocalDate) =
        dao.observeBetween(start.toEpochDay(), end.toEpochDay()).map { l -> l.map { it.toDomain() } }
}

class GoldRepositoryImpl @Inject constructor(private val dao: GoldDao) : GoldRepository {
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override fun observeById(id: String) = dao.observeById(id).map { it?.toDomain() }
    override suspend fun upsert(gold: GoldAsset) = dao.upsert(gold.toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
}

class LandRepositoryImpl @Inject constructor(private val dao: LandDao) : LandRepository {
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override fun observeById(id: String) = dao.observeById(id).map { it?.toDomain() }
    override suspend fun upsert(land: LandAsset) = dao.upsert(land.toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
    override fun observeTaxPayments(landId: String) =
        dao.observeTaxPayments(landId).map { l -> l.map { it.toDomain() } }
    override suspend fun upsertTaxPayment(payment: LandTaxPayment) = dao.upsertTaxPayment(payment.toEntity())
    override suspend fun deleteTaxPayment(id: String) = dao.deleteTaxPayment(id)
}

class DepositRepositoryImpl @Inject constructor(private val dao: DepositDao) : DepositRepository {
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override fun observeById(id: String) = dao.observeById(id).map { it?.toDomain() }
    override suspend fun upsert(deposit: DepositAsset) = dao.upsert(deposit.toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
    override fun observeSnapshots(depositId: String) =
        dao.observeSnapshots(depositId).map { l -> l.map { it.toDomain() } }
    override fun observeLatestSnapshot(depositId: String) =
        dao.observeLatestSnapshot(depositId).map { it?.toDomain() }
    override suspend fun upsertSnapshot(snapshot: DepositSnapshot) = dao.upsertSnapshot(snapshot.toEntity())
}

class DebtRepositoryImpl @Inject constructor(private val dao: DebtDao) : DebtRepository {
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override fun observeById(id: String) = dao.observeById(id).map { it?.toDomain() }
    override suspend fun upsert(debt: Debt) = dao.upsert(debt.toEntity())
    override fun observePayments(debtId: String) = dao.observePayments(debtId).map { l -> l.map { it.toDomain() } }
    override fun observePaidTotal(debtId: String) = dao.observePaidTotal(debtId)
    override suspend fun upsertPayment(payment: DebtPayment) = dao.upsertPayment(payment.toEntity())
    override suspend fun deletePayment(id: String) = dao.deletePayment(id)
}
