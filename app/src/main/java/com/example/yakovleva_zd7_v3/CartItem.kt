package com.example.yakovleva_zd7_v3

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    var quantity: Int = 1
)