package com.lppsa.domain.model

data class Session(
    val id: String,
    val requestType: RequestType,
    val productName: String,
    val purchaseDate: String,
    val description: String,
    val decision: Decision? = null,
    val createdAt: String,
)
