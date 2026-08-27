package com.example.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPriceRiel: Int
        get() = product.priceRiel * quantity
}
