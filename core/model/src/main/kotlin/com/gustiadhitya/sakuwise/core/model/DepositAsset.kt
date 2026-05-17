package com.gustiadhitya.sakuwise.core.model

data class DepositAsset(
    val id: String,
    val name: String,
    val typeLabel: DepositAssetType,
    val institutionInfo: String?,
    val note: String?,
    val status: DepositAssetStatus,
)
