package com.gustiadhitya.sakuwise.core.crypto

interface KeyManager {
    fun getDek(): ByteArray
    fun setupKeyOnFirstLaunch()
}
