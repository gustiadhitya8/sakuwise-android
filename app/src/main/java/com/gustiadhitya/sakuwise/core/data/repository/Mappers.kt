package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.entity.AccountEntity
import com.gustiadhitya.sakuwise.core.database.entity.AccountSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.AllocationEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetDepositEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetDepositSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetGoldEntity
import com.gustiadhitya.sakuwise.core.database.entity.AssetLandEntity
import com.gustiadhitya.sakuwise.core.database.entity.CategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.IncomeCategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.LandTaxPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanEntity
import com.gustiadhitya.sakuwise.core.database.entity.PlanItemEntity
import com.gustiadhitya.sakuwise.core.database.entity.TransactionEntity
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AccountSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.AccountType
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.AssetStatus
import com.gustiadhitya.sakuwise.core.domain.model.Category
import com.gustiadhitya.sakuwise.core.domain.model.Debt
import com.gustiadhitya.sakuwise.core.domain.model.DebtDirection
import com.gustiadhitya.sakuwise.core.domain.model.DebtPayment
import com.gustiadhitya.sakuwise.core.domain.model.DepositAsset
import com.gustiadhitya.sakuwise.core.domain.model.DepositSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.DepositType
import com.gustiadhitya.sakuwise.core.domain.model.GoldAsset
import com.gustiadhitya.sakuwise.core.domain.model.IncomeCategory
import com.gustiadhitya.sakuwise.core.domain.model.LandAsset
import com.gustiadhitya.sakuwise.core.domain.model.LandTaxPayment
import com.gustiadhitya.sakuwise.core.domain.model.Plan
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.domain.model.Recurrence
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import java.time.LocalDate

internal fun AccountEntity.toDomain() = Account(
    id, name, AccountType.fromCode(type), initialBalance, iconName, archived,
)

internal fun Account.toEntity(createdAt: Long = System.currentTimeMillis()) = AccountEntity(
    id = id, name = name, type = type.code(), initialBalance = initialBalance,
    iconName = iconName, colorHex = null, archived = archived, createdAt = createdAt,
)

internal fun AccountSnapshotEntity.toDomain() = AccountSnapshot(
    id, accountId, LocalDate.ofEpochDay(snapshotEpochDay),
    observedBalance, computedBalance, diff, note,
)

internal fun AccountSnapshot.toEntity() = AccountSnapshotEntity(
    id, accountId, date.toEpochDay(), observedBalance, computedBalance, diff, note,
)

internal fun PlanEntity.toDomain() = Plan(
    id, LocalDate.ofEpochDay(startEpochDay), LocalDate.ofEpochDay(endEpochDay),
    label, expectedIncome, note,
)

internal fun Plan.toEntity() = PlanEntity(
    id, start.toEpochDay(), end.toEpochDay(), label, expectedIncome, note,
)

internal fun AllocationEntity.toDomain() = Allocation(id, planId, name, targetPct, sortOrder)
internal fun Allocation.toEntity() = AllocationEntity(id, planId, name, targetPct, sortOrder)

internal fun CategoryEntity.toDomain() = Category(id, allocationId, name, plannedAmount, sortOrder)
internal fun Category.toEntity() = CategoryEntity(id, allocationId, name, plannedAmount, sortOrder)

internal fun PlanItemEntity.toDomain() = PlanItem(
    id, categoryId, name, plannedAmount, Recurrence.fromCode(recurrence), note, sortOrder,
)
internal fun PlanItem.toEntity() = PlanItemEntity(
    id, categoryId, name, plannedAmount, recurrence.code(), note, sortOrder,
)

internal fun TransactionEntity.toDomain() = Transaction(
    id, LocalDate.ofEpochDay(dateEpochDay), amount, TxnType.fromCode(type),
    planItemId, sourceAccountId, destAccountId, transferFee, debtId, photoBlob,
    incomeCategoryId, note, createdAt,
)
internal fun Transaction.toEntity() = TransactionEntity(
    id, date.toEpochDay(), amount, type.code(),
    planItemId, sourceAccountId, destAccountId, transferFee, debtId, photoBlob,
    incomeCategoryId, note, createdAt,
)

internal fun IncomeCategoryEntity.toDomain() = IncomeCategory(id, name, iconName, sortOrder, isDefault)
internal fun IncomeCategory.toEntity() = IncomeCategoryEntity(id, name, iconName, sortOrder, isDefault)

internal fun AssetGoldEntity.toDomain() = GoldAsset(
    id, LocalDate.ofEpochDay(purchaseEpochDay), weightGram, serial, buyPrice, note,
    AssetStatus.fromCode(status), soldEpochDay?.let(LocalDate::ofEpochDay), soldPrice,
)
internal fun GoldAsset.toEntity() = AssetGoldEntity(
    id, purchaseDate.toEpochDay(), weightGram, serial, buyPrice, note, null,
    status.code(), soldDate?.toEpochDay(), soldPrice,
)

internal fun AssetLandEntity.toDomain() = LandAsset(
    id = id, name = name, location = location, sertifikatId = sertifikatId,
    sizeM2 = sizeM2, buyPrice = buyPrice, currentValue = currentValue, note = note,
    status = AssetStatus.fromCode(status),
    soldDate = soldEpochDay?.let(LocalDate::ofEpochDay), soldPrice = soldPrice,
    // Legacy rows migrated in with epoch day 0 — surface today instead of
    // "1970" so the UI looks sane until the user edits to set a real date.
    purchaseDate = if (purchaseEpochDay <= 0L) LocalDate.now()
        else LocalDate.ofEpochDay(purchaseEpochDay),
)
internal fun LandAsset.toEntity() = AssetLandEntity(
    id = id, name = name, location = location, sertifikatId = sertifikatId,
    sizeM2 = sizeM2, buyPrice = buyPrice, currentValue = currentValue, note = note,
    photoBlob = null,
    status = status.code(), soldEpochDay = soldDate?.toEpochDay(), soldPrice = soldPrice,
    purchaseEpochDay = purchaseDate.toEpochDay(),
)

internal fun LandTaxPaymentEntity.toDomain() = LandTaxPayment(
    id, assetLandId, LocalDate.ofEpochDay(payEpochDay), amount, accountId, note,
)
internal fun LandTaxPayment.toEntity() = LandTaxPaymentEntity(
    id, assetLandId, date.toEpochDay(), amount, accountId, null, note,
)

internal fun AssetDepositEntity.toDomain() = DepositAsset(
    id, name, DepositType.fromCode(typeLabel), institutionInfo, note, status == "active",
)
internal fun DepositAsset.toEntity() = AssetDepositEntity(
    id, name, typeLabel.code(), institutionInfo, note, if (active) "active" else "closed",
)

internal fun AssetDepositSnapshotEntity.toDomain() = DepositSnapshot(
    id, assetDepositId, LocalDate.ofEpochDay(snapshotEpochDay), balance, note,
)
internal fun DepositSnapshot.toEntity() = AssetDepositSnapshotEntity(
    id, depositAssetId, date.toEpochDay(), balance, note,
)

internal fun DebtEntity.toDomain() = Debt(
    id, counterparty, DebtDirection.fromCode(direction), principal,
    LocalDate.ofEpochDay(startEpochDay), dueEpochDay?.let(LocalDate::ofEpochDay),
    status == "open", note,
)
internal fun Debt.toEntity() = DebtEntity(
    id, counterparty, direction.code(), principal,
    startDate.toEpochDay(), dueDate?.toEpochDay(),
    if (open) "open" else "closed", note,
)

internal fun DebtPaymentEntity.toDomain() = DebtPayment(
    id, debtId, LocalDate.ofEpochDay(payEpochDay), amount, accountId, transactionId, note,
)
internal fun DebtPayment.toEntity() = DebtPaymentEntity(
    id, debtId, date.toEpochDay(), amount, accountId, transactionId, note,
)
