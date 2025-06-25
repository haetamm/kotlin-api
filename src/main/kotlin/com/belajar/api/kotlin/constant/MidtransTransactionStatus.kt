package com.belajar.api.kotlin.constant

enum class MidtransTransactionStatus {
    SETTLEMENT,
    PENDING,
    EXPIRE,
    CANCEL,
    DENY,
    FAILURE,
    CHALLENGE;

    companion object {
        fun from(value: String): MidtransTransactionStatus? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
