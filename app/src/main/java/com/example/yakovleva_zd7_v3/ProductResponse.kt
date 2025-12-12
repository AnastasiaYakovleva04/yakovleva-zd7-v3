package com.example.yakovleva_zd7_v3

data class ProductResponse(
    val success: Boolean = false,
    val data: List<Product> = emptyList(),
    val message: String? = null
)