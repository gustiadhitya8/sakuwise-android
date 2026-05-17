package com.gustiadhitya.sakuwise.core.database.converter

import androidx.room.TypeConverter
import com.gustiadhitya.sakuwise.core.model.AccountStatus
import com.gustiadhitya.sakuwise.core.model.AccountType
import com.gustiadhitya.sakuwise.core.model.AllocationName
import com.gustiadhitya.sakuwise.core.model.DebtDirection
import com.gustiadhitya.sakuwise.core.model.DebtStatus
import com.gustiadhitya.sakuwise.core.model.DepositAssetStatus
import com.gustiadhitya.sakuwise.core.model.DepositAssetType
import com.gustiadhitya.sakuwise.core.model.GoldAssetStatus
import com.gustiadhitya.sakuwise.core.model.LandAssetStatus
import com.gustiadhitya.sakuwise.core.model.RecurrenceType
import com.gustiadhitya.sakuwise.core.model.TransactionType

class EnumConverters {
    @TypeConverter fun fromAccountType(v: AccountType): String = v.name
    @TypeConverter fun toAccountType(v: String): AccountType = AccountType.valueOf(v)

    @TypeConverter fun fromAccountStatus(v: AccountStatus): String = v.name
    @TypeConverter fun toAccountStatus(v: String): AccountStatus = AccountStatus.valueOf(v)

    @TypeConverter fun fromTransactionType(v: TransactionType): String = v.name
    @TypeConverter fun toTransactionType(v: String): TransactionType = TransactionType.valueOf(v)

    @TypeConverter fun fromRecurrenceType(v: RecurrenceType): String = v.name
    @TypeConverter fun toRecurrenceType(v: String): RecurrenceType = RecurrenceType.valueOf(v)

    @TypeConverter fun fromAllocationName(v: AllocationName): String = v.name
    @TypeConverter fun toAllocationName(v: String): AllocationName = AllocationName.valueOf(v)

    @TypeConverter fun fromGoldAssetStatus(v: GoldAssetStatus): String = v.name
    @TypeConverter fun toGoldAssetStatus(v: String): GoldAssetStatus = GoldAssetStatus.valueOf(v)

    @TypeConverter fun fromLandAssetStatus(v: LandAssetStatus): String = v.name
    @TypeConverter fun toLandAssetStatus(v: String): LandAssetStatus = LandAssetStatus.valueOf(v)

    @TypeConverter fun fromDepositAssetStatus(v: DepositAssetStatus): String = v.name
    @TypeConverter fun toDepositAssetStatus(v: String): DepositAssetStatus = DepositAssetStatus.valueOf(v)

    @TypeConverter fun fromDepositAssetType(v: DepositAssetType): String = v.name
    @TypeConverter fun toDepositAssetType(v: String): DepositAssetType = DepositAssetType.valueOf(v)

    @TypeConverter fun fromDebtDirection(v: DebtDirection): String = v.name
    @TypeConverter fun toDebtDirection(v: String): DebtDirection = DebtDirection.valueOf(v)

    @TypeConverter fun fromDebtStatus(v: DebtStatus): String = v.name
    @TypeConverter fun toDebtStatus(v: String): DebtStatus = DebtStatus.valueOf(v)
}
