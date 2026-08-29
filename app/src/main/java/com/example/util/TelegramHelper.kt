package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.DailyClosureRecord
import com.example.data.SaleRecord
import com.example.ui.ProductSaleSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object TelegramHelper {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Formats a clean, readable sales report for Telegram dispatch
     */
    fun generateReportText(
        dateIso: String,
        sales: List<SaleRecord>,
        productSummaries: List<ProductSaleSummary>,
        dailyClosure: DailyClosureRecord?
    ): String {
        val totalRevenue = sales.sumOf { it.totalPrice.toLong() }
        val cashRevenue = sales.filter { it.paymentMethod != "ABA" }.sumOf { it.totalPrice.toLong() }
        val abaRevenue = sales.filter { it.paymentMethod == "ABA" }.sumOf { it.totalPrice.toLong() }
        val totalItems = sales.sumOf { it.quantity }
        val totalTransactions = sales.size
        val dateKhmer = Formatters.formatDateToKhmer(dateIso)
        val nowTime = Formatters.formatTimestampToTime(System.currentTimeMillis())

        val builder = StringBuilder()
        builder.append("📊 របាយការណ៍លក់ — JOLLY SLUSHIE POS\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("📅 កាលបរិច្ឆេទ: $dateKhmer ($dateIso)\n")
        builder.append("⏰ ពេលវេលាផ្ញើ: $nowTime\n\n")

        builder.append("💰 ចំណូលសរុប: ${Formatters.formatRiel(totalRevenue)}\n")
        builder.append("  💵 សាច់ប្រាក់ (Cash): ${Formatters.formatRiel(cashRevenue)}\n")
        builder.append("  📲 ABA Pay (ABA): ${Formatters.formatRiel(abaRevenue)}\n\n")
        builder.append("🥤 ចំនួនកែវលក់សរុប: $totalItems កែវ\n")
        builder.append("🧾 ចំនួនវិក្កយបត្រ: $totalTransactions លើក\n\n")

        builder.append("📋 តារាងលម្អិតតាមមុខទំនិញ:\n")
        if (productSummaries.isEmpty()) {
            builder.append("  (មិនទាន់មានការលក់)\n")
        } else {
            productSummaries.forEachIndexed { index, item ->
                builder.append("${index + 1}. ${item.product.iconEmoji} ${item.product.nameKh}\n")
                builder.append("   ↳ ចំនួន: ${item.totalQuantity} កែវ | សរុប: ${Formatters.formatRiel(item.totalAmount)}\n")
            }
        }

        builder.append("\n━━━━━━━━━━━━━━━━━━━━\n")
        if (dailyClosure != null) {
            builder.append("✅ ស្ថានភាព: បានបិទបញ្ជីលក់រួចរាល់\n")
            builder.append("🔒 បិទនៅម៉ោង: ${Formatters.formatTimestampToTime(dailyClosure.closedAtTimestamp)}\n")
            if (dailyClosure.notes.isNotBlank()) {
                builder.append("📝 ចំណាំ: ${dailyClosure.notes}\n")
            }
        } else {
            builder.append("⏳ ស្ថានភាព: កំពុងលក់ (មិនទាន់បិទបញ្ជី)\n")
        }
        builder.append("📍 ហាង Jolly Slushie")

        return builder.toString()
    }

    /**
     * Opens Telegram App to share the report text directly with the owner or group
     */
    fun shareViaTelegram(context: Context, text: String) {
        try {
            val telegramIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                `package` = "org.telegram.messenger"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(telegramIntent)
        } catch (e: Exception) {
            // Fallback to generic share chooser if Telegram app is not installed
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(genericIntent, "ផ្ញើរបាយការណ៍តាម Telegram / Share"))
        }
    }

    /**
     * Directly sends the report message to Telegram Bot via Telegram HTTP Bot API
     */
    suspend fun sendViaTelegramBotApi(
        botToken: String,
        chatId: String,
        message: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = botToken.trim()
            val cleanChatId = chatId.trim()

            if (cleanToken.isBlank() || cleanChatId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("សូមបញ្ចូល Bot Token និង Chat ID"))
            }

            val url = "https://api.telegram.org/bot$cleanToken/sendMessage"
            val jsonBody = JSONObject().apply {
                put("chat_id", cleanChatId)
                put("text", message)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Result.success("ផ្ញើរបាយការណ៍ទៅ Telegram បានជោគជ័យ!")
                } else {
                    Result.failure(Exception("បរាជ័យ (${response.code}): $bodyStr"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
