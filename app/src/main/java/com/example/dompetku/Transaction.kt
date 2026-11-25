package com.example.dompetku

data class Transaction(
    val id: String = System.currentTimeMillis().toString(),
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val description: String,
    val date: String,
    val type: String
)
