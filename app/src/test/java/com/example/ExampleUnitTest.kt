package com.example

import com.example.data.SaleRecord
import com.example.model.CartItem
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.util.CloudSyncManager
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

  @Test
  fun testGoogleDriveJsonExport() {
    val sales = listOf(
      SaleRecord(
        id = 1001L,
        productId = "slushie",
        productName = "Slushie",
        unitPrice = 4000,
        quantity = 2,
        totalPrice = 8000,
        timestamp = 1700000000000L,
        dateString = "2026-08-27"
      )
    )
    val json = CloudSyncManager.exportSalesToJson(sales, emptyList())
    assertTrue(json.contains("Jolly Slushie POS"))
    assertTrue(json.contains("slushie"))
    assertTrue(json.contains("8000"))
  }
}
