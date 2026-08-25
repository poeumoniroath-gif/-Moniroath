package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.DailyClosureRecord
import com.example.data.SaleRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class CloudConfig(
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val cloudWebhookUrl: String = "",
    val autoSyncOnSale: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

object CloudSyncManager {

    private const val PREFS_NAME = "jolly_slushie_cloud_prefs"
    private const val KEY_BOT_TOKEN = "telegram_bot_token"
    private const val KEY_CHAT_ID = "telegram_chat_id"
    private const val KEY_WEBHOOK_URL = "cloud_webhook_url"
    private const val KEY_AUTO_SYNC = "auto_sync_enabled"
    private const val KEY_LAST_SYNC = "last_sync_time"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getSavedConfig(context: Context): CloudConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return CloudConfig(
            telegramBotToken = prefs.getString(KEY_BOT_TOKEN, "").orEmpty(),
            telegramChatId = prefs.getString(KEY_CHAT_ID, "").orEmpty(),
            cloudWebhookUrl = prefs.getString(KEY_WEBHOOK_URL, "").orEmpty(),
            autoSyncOnSale = prefs.getBoolean(KEY_AUTO_SYNC, false),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, 0L)
        )
    }

    fun saveConfig(context: Context, config: CloudConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_BOT_TOKEN, config.telegramBotToken)
            .putString(KEY_CHAT_ID, config.telegramChatId)
            .putString(KEY_WEBHOOK_URL, config.cloudWebhookUrl)
            .putBoolean(KEY_AUTO_SYNC, config.autoSyncOnSale)
            .putLong(KEY_LAST_SYNC, config.lastSyncTimestamp)
            .apply()
    }

    fun updateLastSyncTime(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    /**
     * Converts all sales records to structured JSON for Cloud export/sync
     */
    fun exportSalesToJson(sales: List<SaleRecord>, closures: List<DailyClosureRecord>): String {
        val root = JSONObject()
        root.put("shop", "Jolly Slushie POS")
        root.put("exportTime", System.currentTimeMillis())
        root.put("totalRecords", sales.size)

        val salesArray = JSONArray()
        sales.forEach { sale ->
            val sObj = JSONObject().apply {
                put("id", sale.id)
                put("productId", sale.productId)
                put("productName", sale.productName)
                put("unitPrice", sale.unitPrice)
                put("quantity", sale.quantity)
                put("totalPrice", sale.totalPrice)
                put("timestamp", sale.timestamp)
                put("dateString", sale.dateString)
            }
            salesArray.put(sObj)
        }
        root.put("sales", salesArray)

        val closureArray = JSONArray()
        closures.forEach { closure ->
            val cObj = JSONObject().apply {
                put("dateString", closure.dateString)
                put("closedAtTimestamp", closure.closedAtTimestamp)
                put("totalRevenue", closure.totalRevenue)
                put("totalItems", closure.totalItems)
                put("totalTransactions", closure.totalTransactions)
                put("notes", closure.notes)
            }
            closureArray.put(cObj)
        }
        root.put("dailyClosures", closureArray)

        return root.toString(2)
    }

    /**
     * Syncs sales dataset to custom remote Cloud server or Webhook endpoint
     */
    suspend fun syncToRemoteCloud(
        webhookUrl: String,
        payloadJson: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (webhookUrl.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("សូមបញ្ចូល Cloud Webhook URL"))
            }

            val requestBody = payloadJson.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(webhookUrl.trim())
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("ទិន្នន័យត្រូវបាន Sync ឡើង Cloud ជោគជ័យ!")
                } else {
                    Result.failure(Exception("Cloud Server Error: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
