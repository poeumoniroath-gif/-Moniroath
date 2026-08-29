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
    val googleDriveScriptUrl: String = "",
    val autoSyncOnSale: Boolean = true,
    val lastSyncTimestamp: Long = 0L
)

data class SyncPullResult(
    val newSales: List<SaleRecord>,
    val newClosures: List<DailyClosureRecord>,
    val message: String
)

object CloudSyncManager {

    private const val PREFS_NAME = "jolly_slushie_cloud_prefs"
    private const val KEY_BOT_TOKEN = "telegram_bot_token"
    private const val KEY_CHAT_ID = "telegram_chat_id"
    private const val KEY_GOOGLE_DRIVE_URL = "google_drive_script_url"
    private const val KEY_AUTO_SYNC = "auto_sync_enabled"
    private const val KEY_LAST_SYNC = "last_sync_time"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    const val GOOGLE_APPS_SCRIPT_SAMPLE = """
/**
 * Google Apps Script for Jolly Slushie POS - Google Drive Shared Database
 * 
 * របៀបដំឡើង (Instructions):
 * 1. បើក Google Sheet របស់អ្នក -> ចុច Extensions -> Apps Script
 *    (ឬបើក script.google.com រួចដាក់ SPREADSHEET_ID នៃ Google Sheet របស់អ្នកខាងក្រោម)
 * 2. Paste កូដនេះចូលក្នុង Code.gs រួចចុច Save (💾)
 * 3. ចុច Deploy -> New deployment -> Select type: Web app
 * 4. Execute as: "Me" | Who has access: "Anyone"
 * 5. ចុច Deploy រួច Copy យក Web App URL មកដាក់ក្នុង App POS
 */

// ប្រសិនបើបង្កើត Script ដាច់ដោយឡែក សូមដាក់ Google Sheet ID នៅទីនេះ (ទុកទទេបើបើកតាម Extensions -> Apps Script):
var SPREADSHEET_ID = ""; 

function getSpreadsheet() {
  if (SPREADSHEET_ID && SPREADSHEET_ID.trim() !== "") {
    return SpreadsheetApp.openById(SPREADSHEET_ID.trim());
  }
  var active = SpreadsheetApp.getActiveSpreadsheet();
  if (active) return active;
  
  // ស្វែងរក Sheet ដែលមានស្រាប់ ឬបង្កើតថ្មី
  var files = DriveApp.getFilesByName("Jolly Slushie POS Database");
  if (files.hasNext()) {
    return SpreadsheetApp.open(files.next());
  }
  return SpreadsheetApp.create("Jolly Slushie POS Database");
}

function doGet(e) {
  var ss = getSpreadsheet();
  var salesSheet = ss.getSheetByName("Sales") || ss.insertSheet("Sales");
  var closuresSheet = ss.getSheetByName("Closures") || ss.insertSheet("Closures");
  
  var salesData = salesSheet.getDataRange().getValues();
  var salesList = [];
  for (var i = 1; i < salesData.length; i++) {
    var row = salesData[i];
    if (row[0]) {
      salesList.push({
        id: Number(row[0]),
        productId: String(row[1]),
        productName: String(row[2]),
        unitPrice: Number(row[3]),
        quantity: Number(row[4]),
        totalPrice: Number(row[5]),
        timestamp: Number(row[6]),
        dateString: String(row[7])
      });
    }
  }
  
  var closuresData = closuresSheet.getDataRange().getValues();
  var closuresList = [];
  for (var j = 1; j < closuresData.length; j++) {
    var cRow = closuresData[j];
    if (cRow[0]) {
      closuresList.push({
        dateString: String(cRow[0]),
        closedAtTimestamp: Number(cRow[1]),
        totalRevenue: Number(cRow[2]),
        totalItems: Number(cRow[3]),
        totalTransactions: Number(cRow[4]),
        notes: String(cRow[5] || "")
      });
    }
  }
  
  var response = {
    status: "success",
    sales: salesList,
    dailyClosures: closuresList
  };
  
  return ContentService.createTextOutput(JSON.stringify(response))
    .setMimeType(ContentService.MimeType.JSON);
}

function doPost(e) {
  try {
    var contents = e.postData.contents;
    var data = JSON.parse(contents);
    var ss = getSpreadsheet();
    
    // Save/Update Sales Sheet
    var salesSheet = ss.getSheetByName("Sales") || ss.insertSheet("Sales");
    if (salesSheet.getLastRow() === 0) {
      salesSheet.appendRow(["id", "productId", "productName", "unitPrice", "quantity", "totalPrice", "timestamp", "dateString", "paymentMethod"]);
    }
    
    var existingSales = salesSheet.getDataRange().getValues();
    var existingIds = {};
    for (var i = 1; i < existingSales.length; i++) {
      existingIds[existingSales[i][0]] = true;
    }
    
    var incomingSales = data.sales || [];
    for (var k = 0; k < incomingSales.length; k++) {
      var s = incomingSales[k];
      if (!existingIds[s.id]) {
        salesSheet.appendRow([s.id, s.productId, s.productName, s.unitPrice, s.quantity, s.totalPrice, s.timestamp, s.dateString, s.paymentMethod || "CASH"]);
        existingIds[s.id] = true;
      }
    }
    
    // Save/Update Daily Closures Sheet
    var closuresSheet = ss.getSheetByName("Closures") || ss.insertSheet("Closures");
    if (closuresSheet.getLastRow() === 0) {
      closuresSheet.appendRow(["dateString", "closedAtTimestamp", "totalRevenue", "totalItems", "totalTransactions", "notes"]);
    }
    
    var existingClosures = closuresSheet.getDataRange().getValues();
    var existingDates = {};
    for (var n = 1; n < existingClosures.length; n++) {
      existingDates[existingClosures[n][0]] = true;
    }
    
    var incomingClosures = data.dailyClosures || [];
    for (var m = 0; m < incomingClosures.length; m++) {
      var c = incomingClosures[m];
      if (!existingDates[c.dateString]) {
        closuresSheet.appendRow([c.dateString, c.closedAtTimestamp, c.totalRevenue, c.totalItems, c.totalTransactions, c.notes]);
        existingDates[c.dateString] = true;
      }
    }
    
    return ContentService.createTextOutput(JSON.stringify({ status: "success", count: incomingSales.length }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}
"""

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
            googleDriveScriptUrl = prefs.getString(KEY_GOOGLE_DRIVE_URL, "").orEmpty(),
            autoSyncOnSale = prefs.getBoolean(KEY_AUTO_SYNC, true),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, 0L)
        )
    }

    fun saveConfig(context: Context, config: CloudConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_BOT_TOKEN, config.telegramBotToken)
            .putString(KEY_CHAT_ID, config.telegramChatId)
            .putString(KEY_GOOGLE_DRIVE_URL, config.googleDriveScriptUrl)
            .putBoolean(KEY_AUTO_SYNC, config.autoSyncOnSale)
            .putLong(KEY_LAST_SYNC, config.lastSyncTimestamp)
            .apply()
    }

    fun updateLastSyncTime(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    /**
     * Converts all sales records to structured JSON for Google Drive sync
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
                put("paymentMethod", sale.paymentMethod)
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
     * Pushes sales records to Google Drive Web App database
     */
    suspend fun pushToGoogleDrive(
        scriptUrl: String,
        sales: List<SaleRecord>,
        closures: List<DailyClosureRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (scriptUrl.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("សូមបញ្ចូល Google Drive Script URL"))
            }

            val payloadJson = exportSalesToJson(sales, closures)
            val requestBody = payloadJson.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(scriptUrl.trim())
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("ទិន្នន័យត្រូវបាន Sync ឡើង Google Drive ជោគជ័យ!")
                } else {
                    Result.failure(Exception("Google Drive Server Error: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pulls shared database from Google Drive so every device is updated
     */
    suspend fun pullFromGoogleDrive(
        scriptUrl: String
    ): Result<SyncPullResult> = withContext(Dispatchers.IO) {
        try {
            if (scriptUrl.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("សូមបញ្ចូល Google Drive Script URL"))
            }

            val request = Request.Builder()
                .url(scriptUrl.trim())
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful || bodyStr.isBlank()) {
                    return@withContext Result.failure(Exception("Google Drive Server Code: ${response.code}"))
                }

                val json = JSONObject(bodyStr)
                val salesArray = json.optJSONArray("sales") ?: JSONArray()
                val closuresArray = json.optJSONArray("dailyClosures") ?: JSONArray()

                val salesList = mutableListOf<SaleRecord>()
                for (i in 0 until salesArray.length()) {
                    val item = salesArray.getJSONObject(i)
                    salesList.add(
                        SaleRecord(
                            id = item.optLong("id", System.currentTimeMillis() + i),
                            productId = item.optString("productId"),
                            productName = item.optString("productName"),
                            unitPrice = item.optInt("unitPrice"),
                            quantity = item.optInt("quantity", 1),
                            totalPrice = item.optInt("totalPrice"),
                            timestamp = item.optLong("timestamp", System.currentTimeMillis()),
                            dateString = item.optString("dateString"),
                            paymentMethod = item.optString("paymentMethod", "CASH")
                        )
                    )
                }

                val closuresList = mutableListOf<DailyClosureRecord>()
                for (j in 0 until closuresArray.length()) {
                    val c = closuresArray.getJSONObject(j)
                    closuresList.add(
                        DailyClosureRecord(
                            dateString = c.optString("dateString"),
                            closedAtTimestamp = c.optLong("closedAtTimestamp"),
                            totalRevenue = c.optLong("totalRevenue"),
                            totalItems = c.optInt("totalItems"),
                            totalTransactions = c.optInt("totalTransactions"),
                            notes = c.optString("notes")
                        )
                    )
                }

                Result.success(
                    SyncPullResult(
                        newSales = salesList,
                        newClosures = closuresList,
                        message = "ទាញយក ${salesList.size} វិក្កយបត្រពី Google Drive បានជោគជ័យ"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
