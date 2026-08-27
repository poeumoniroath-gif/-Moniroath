package com.example

import com.example.model.CartItem
import com.example.model.Product
import com.example.model.ProductCategory
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCartItemCalculation() {
    val sampleProduct = Product(
      id = "slushie",
      nameKh = "Slushie",
      priceRiel = 4000,
      category = ProductCategory.DRINKS_SWEETS,
      categoryKh = "ភេសជ្ជៈ",
      iconEmoji = "🥤",
      primaryColorHex = 0xFF00BCD4
    )
    val cartItem = CartItem(product = sampleProduct, quantity = 3)
    assertEquals(12000, cartItem.totalPriceRiel)
  }
}

