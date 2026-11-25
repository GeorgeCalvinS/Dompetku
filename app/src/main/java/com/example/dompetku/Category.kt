package com.example.dompetku

data class Category(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val type: String
)
