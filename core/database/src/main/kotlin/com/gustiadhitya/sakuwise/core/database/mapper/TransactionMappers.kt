package com.gustiadhitya.sakuwise.core.database.mapper

import com.gustiadhitya.sakuwise.core.database.entity.IncomeCategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.TransactionEntity
import com.gustiadhitya.sakuwise.core.model.IncomeCategory
import com.gustiadhitya.sakuwise.core.model.Transaction
import com.gustiadhitya.sakuwise.core.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = when (type) {
    TransactionType.INCOME -> Transaction.Income(
        id = id,
        date = date,
        amount = amount,
        incomeCategoryId = requireNotNull(incomeCategoryId),
        accountId = requireNotNull(sourceAccountId),
        note = note,
        createdAt = createdAt,
    )
    TransactionType.EXPENSE -> Transaction.Expense(
        id = id,
        date = date,
        amount = amount,
        planItemId = requireNotNull(planItemId),
        accountId = requireNotNull(sourceAccountId),
        debtId = debtId,
        photoBlob = photoBlob,
        note = note,
        createdAt = createdAt,
    )
    TransactionType.TRANSFER -> Transaction.Transfer(
        id = id,
        date = date,
        amount = amount,
        sourceAccountId = requireNotNull(sourceAccountId),
        destinationAccountId = requireNotNull(destinationAccountId),
        feeAmount = feeAmount,
        feePlanItemId = feePlanItemId,
        note = note,
        createdAt = createdAt,
    )
    TransactionType.DEBT_INFLOW -> Transaction.DebtInflow(
        id = id,
        date = date,
        amount = amount,
        accountId = requireNotNull(sourceAccountId),
        debtId = requireNotNull(debtId),
        note = note,
        createdAt = createdAt,
    )
    TransactionType.DEBT_OUTFLOW -> Transaction.DebtOutflow(
        id = id,
        date = date,
        amount = amount,
        accountId = requireNotNull(sourceAccountId),
        debtId = requireNotNull(debtId),
        note = note,
        createdAt = createdAt,
    )
    TransactionType.RECONCILIATION -> Transaction.Reconciliation(
        id = id,
        date = date,
        amount = amount,
        accountId = requireNotNull(sourceAccountId),
        note = note,
        createdAt = createdAt,
    )
}

fun Transaction.toEntity(): TransactionEntity = when (this) {
    is Transaction.Income -> TransactionEntity(
        id = id, date = date, amount = amount,
        type = TransactionType.INCOME,
        incomeCategoryId = incomeCategoryId,
        planItemId = null, sourceAccountId = accountId, destinationAccountId = null,
        feeAmount = null, feePlanItemId = null, debtId = null, photoBlob = null,
        note = note, createdAt = createdAt,
    )
    is Transaction.Expense -> TransactionEntity(
        id = id, date = date, amount = amount,
        type = TransactionType.EXPENSE,
        incomeCategoryId = null,
        planItemId = planItemId, sourceAccountId = accountId, destinationAccountId = null,
        feeAmount = null, feePlanItemId = null, debtId = debtId, photoBlob = photoBlob,
        note = note, createdAt = createdAt,
    )
    is Transaction.Transfer -> TransactionEntity(
        id = id, date = date, amount = amount,
        type = TransactionType.TRANSFER,
        incomeCategoryId = null, planItemId = null,
        sourceAccountId = sourceAccountId, destinationAccountId = destinationAccountId,
        feeAmount = feeAmount, feePlanItemId = feePlanItemId, debtId = null, photoBlob = null,
        note = note, createdAt = createdAt,
    )
    is Transaction.DebtInflow -> TransactionEntity(
        id = id, date = date, amount = amount,
        type = TransactionType.DEBT_INFLOW,
        incomeCategoryId = null, planItemId = null,
        sourceAccountId = accountId, destinationAccountId = null,
        feeAmount = null, feePlanItemId = null, debtId = debtId, photoBlob = null,
        note = note, createdAt = createdAt,
    )
    is Transaction.DebtOutflow -> TransactionEntity(
        id = id, date = date, amount = amount,
        type = TransactionType.DEBT_OUTFLOW,
        incomeCategoryId = null, planItemId = null,
        sourceAccountId = accountId, destinationAccountId = null,
        feeAmount = null, feePlanItemId = null, debtId = debtId, photoBlob = null,
        note = note, createdAt = createdAt,
    )
    is Transaction.Reconciliation -> TransactionEntity(
        id = id, date = date, amount = amount,
        type = TransactionType.RECONCILIATION,
        incomeCategoryId = null, planItemId = null,
        sourceAccountId = accountId, destinationAccountId = null,
        feeAmount = null, feePlanItemId = null, debtId = null, photoBlob = null,
        note = note, createdAt = createdAt,
    )
}

fun IncomeCategoryEntity.toDomain(): IncomeCategory = IncomeCategory(id = id, name = name)

fun IncomeCategory.toEntity(): IncomeCategoryEntity = IncomeCategoryEntity(id = id, name = name)
