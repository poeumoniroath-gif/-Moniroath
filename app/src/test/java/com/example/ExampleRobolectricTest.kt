package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SaleRecord
import com.example.util.CloudSyncManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jolly Slushie", appName)
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
        dateString = "2026-08-27",
        paymentMethod = "ABA"
      )
    )
    val json = CloudSyncManager.exportSalesToJson(sales, emptyList())
    assertTrue(json.contains("Jolly Slushie POS"))
    assertTrue(json.contains("slushie"))
    assertTrue(json.contains("8000"))
    assertTrue(json.contains("ABA"))
  }
}

