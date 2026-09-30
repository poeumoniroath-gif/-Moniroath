package com.example

import com.example.model.CartItem
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testUserRoleAuthenticationCredentials() {
    // Admin credentials
    assertEquals("Admin", UserRole.ADMIN.defaultUsername)
    assertEquals("9999216", UserRole.ADMIN.fixedPin)
    assertTrue("9999216" == UserRole.ADMIN.fixedPin)
    assertFalse("1234" == UserRole.ADMIN.fixedPin)

    // Cashier credentials
    assertEquals("Cashier", UserRole.CASHIER.defaultUsername)
    assertEquals("9999", UserRole.CASHIER.fixedPin)
    assertTrue("9999" == UserRole.CASHIER.fixedPin)
    assertFalse("9999216" == UserRole.CASHIER.fixedPin)
  }

  @Test
  fun testProductStockFlags() {
    val healthyProduct = Product(
      id = "slushie",
      nameKh = "Slushie",
      priceRiel = 4000,
      category = ProductCategory.DRINKS_SWEETS,
      categoryKh = "ភេសជ្ជៈ",
      iconEmoji = "🥤",
      primaryColorHex = 0xFF00BCD4,
      stockCount = 20,
      lowStockThreshold = 5
    )
    assertFalse(healthyProduct.isOutOfStock)
    assertFalse(healthyProduct.isLowStock)

    val lowStockProduct = healthyProduct.copy(stockCount = 4)
    assertFalse(lowStockProduct.isOutOfStock)
    assertTrue(lowStockProduct.isLowStock)

    val outOfStockProduct = healthyProduct.copy(stockCount = 0)
    assertTrue(outOfStockProduct.isOutOfStock)
    assertFalse(outOfStockProduct.isLowStock)
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
