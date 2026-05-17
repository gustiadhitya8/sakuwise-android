package com.gustiadhitya.sakuwise.core.database.mapper

import com.gustiadhitya.sakuwise.core.database.entity.DebtEntity
import com.gustiadhitya.sakuwise.core.database.entity.DebtPaymentEntity
import com.gustiadhitya.sakuwise.core.database.entity.DepositAssetEntity
import com.gustiadhitya.sakuwise.core.database.entity.DepositSnapshotEntity
import com.gustiadhitya.sakuwise.core.database.entity.GoldAssetEntity
import com.gustiadhitya.sakuwise.core.database.entity.LandAssetEntity
import com.gustiadhitya.sakuwise.core.database.entity.LandTaxPaymentEntity
import com.gustiadhitya.sakuwise.core.model.Debt
import com.gustiadhitya.sakuwise.core.model.DebtPayment
import com.gustiadhitya.sakuwise.core.model.DepositAsset
import com.gustiadhitya.sakuwise.core.model.DepositSnapshot
import com.gustiadhitya.sakuwise.core.model.GoldAsset
import com.gustiadhitya.sakuwise.core.model.LandAsset
import com.gustiadhitya.sakuwise.core.model.LandTaxPayment

fun GoldAssetEntity.toDomain(): GoldAsset = GoldAsset(
    id = id, purchaseDate = purchaseDate, weightGram = weightGram, serial = serial,
    purchasePrice = purchasePrice, note = note, photoBlob = photoBlob,
    status = status, soldDate = soldDate, soldPrice = soldPrice,
)

fun GoldAsset.toEntity(): GoldAssetEntity = GoldAssetEntity(
    id = id, purchaseDate = purchaseDate, weightGram = weightGram, serial = serial,
    purchasePrice = purchasePrice, note = note, photoBlob = photoBlob,
    status = status, soldDate = soldDate, soldPrice = soldPrice,
)

fun LandAssetEntity.toDomain(): LandAsset = LandAsset(
    id = id, name = name, location = location, sertifikatId = sertifikatId,
    areaSqm = areaSqm, purchasePrice = purchasePrice, currentValue = currentValue,
    note = note, photoBlob = photoBlob, status = status, soldDate = soldDate, soldPrice = soldPrice,
)

fun LandAsset.toEntity(): LandAssetEntity = LandAssetEntity(
    id = id, name = name, location = location, sertifikatId = sertifikatId,
    areaSqm = areaSqm, purchasePrice = purchasePrice, currentValue = currentValue,
    note = note, photoBlob = photoBlob, status = status, soldDate = soldDate, soldPrice = soldPrice,
)

fun LandTaxPaymentEntity.toDomain(): LandTaxPayment = LandTaxPayment(
    id = id, assetLandId = assetLandId, paymentDate = paymentDate, amount = amount,
    accountId = accountId, photoBlob = photoBlob, note = note,
)

fun LandTaxPayment.toEntity(): LandTaxPaymentEntity = LandTaxPaymentEntity(
    id = id, assetLandId = assetLandId, paymentDate = paymentDate, amount = amount,
    accountId = accountId, photoBlob = photoBlob, note = note,
)

fun DepositAssetEntity.toDomain(): DepositAsset = DepositAsset(
    id = id, name = name, typeLabel = typeLabel, institutionInfo = institutionInfo,
    note = note, status = status,
)

fun DepositAsset.toEntity(): DepositAssetEntity = DepositAssetEntity(
    id = id, name = name, typeLabel = typeLabel, institutionInfo = institutionInfo,
    note = note, status = status,
)

fun DepositSnapshotEntity.toDomain(): DepositSnapshot = DepositSnapshot(
    id = id, assetDepositId = assetDepositId, snapshotDate = snapshotDate,
    balance = balance, note = note,
)

fun DepositSnapshot.toEntity(): DepositSnapshotEntity = DepositSnapshotEntity(
    id = id, assetDepositId = assetDepositId, snapshotDate = snapshotDate,
    balance = balance, note = note,
)

fun DebtEntity.toDomain(): Debt = Debt(
    id = id, counterparty = counterparty, direction = direction, principal = principal,
    dateOpened = dateOpened, expectedCloseDate = expectedCloseDate, status = status, note = note,
)

fun Debt.toEntity(): DebtEntity = DebtEntity(
    id = id, counterparty = counterparty, direction = direction, principal = principal,
    dateOpened = dateOpened, expectedCloseDate = expectedCloseDate, status = status, note = note,
)

fun DebtPaymentEntity.toDomain(): DebtPayment = DebtPayment(
    id = id, debtId = debtId, paymentDate = paymentDate, amount = amount,
    accountId = accountId, transactionId = transactionId,
)

fun DebtPayment.toEntity(): DebtPaymentEntity = DebtPaymentEntity(
    id = id, debtId = debtId, paymentDate = paymentDate, amount = amount,
    accountId = accountId, transactionId = transactionId,
)
