package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyClosureRecord
import com.example.data.SaleRecord
import com.example.data.SalesRepository
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.ProductCatalog
import com.example.model.ProductCategory
import com.example.util.CloudConfig
import com.example.util.CloudSyncManager
import com.example.util.Formatters
import com.example.util.SyncState
import com.example.util.TelegramHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductSaleSummary(
    val product: Product,
    val totalQuantity: Int,
    val totalAmount: Long
)

data class SaleSuccessEvent(
    val productName: String,
    val quantity: Int,
    val totalAmount: Int,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val itemsSummary: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class SyncFeedbackMessage(
    val message: String,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SalesRepository

    // Cloud & Telegram Settings State
    private val _cloudConfig = MutableStateFlow(CloudSyncManager.getSavedConfig(application))
    val cloudConfig: StateFlow<CloudConfig> = _cloudConfig.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _isOnline = MutableStateFlow(CloudSyncManager.isNetworkAvailable(application))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<SyncFeedbackMessage?>(null)
    val feedbackMessage: StateFlow<SyncFeedbackMessage?> = _feedbackMessage.asStateFlow()

    // Shopping Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartTotalItems: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val cartTotalRiel: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.totalPriceRiel }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SalesRepository(db.salesDao())
        refreshNetworkStatus()

        // Auto-pull from Google Drive on startup if URL is configured
        if (_cloudConfig.value.googleDriveScriptUrl.isNotBlank()) {
            syncWithGoogleDrive(silent = true)
        }
    }

    // Active screen navigation tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Sale POS Screen state
    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _activeProductForSale = MutableStateFlow<Product?>(null)
    val activeProductForSale: StateFlow<Product?> = _activeProductForSale.asStateFlow()

    private val _activeQuantity = MutableStateFlow(1)
    val activeQuantity: StateFlow<Int> = _activeQuantity.asStateFlow()

    private val _lastSaleSuccess = MutableStateFlow<SaleSuccessEvent?>(null)
    val lastSaleSuccess: StateFlow<SaleSuccessEvent?> = _lastSaleSuccess.asStateFlow()

    // Date selection for Reports & History
    private val _selectedReportDate = MutableStateFlow(Formatters.getTodayIsoString())
    val selectedReportDate: StateFlow<String> = _selectedReportDate.asStateFlow()

    // All available sale dates
    val allSaleDates: StateFlow<List<String>> = repository.getAllSaleDates()
        .map { dates ->
            val today = Formatters.getTodayIsoString()
            if (dates.contains(today)) dates else listOf(today) + dates
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(Formatters.getTodayIsoString())
        )

    // Today's Sales stream
    val todaySales: StateFlow<List<SaleRecord>> = repository.getSalesByDate(Formatters.getTodayIsoString())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Today's Totals
    val todayTotalRevenue: StateFlow<Long> = todaySales.map { list ->
        list.sumOf { it.totalPrice.toLong() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val todayTotalItemsCount: StateFlow<Int> = todaySales.map { list ->
        list.sumOf { it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Selected Report Date Sales stream
    val selectedDateSales: StateFlow<List<SaleRecord>> = _selectedReportDate
        .flatMapLatest { date -> repository.getSalesByDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedDateCashRevenue: StateFlow<Long> = selectedDateSales.map { list ->
        list.filter { it.paymentMethod != "ABA" }.sumOf { it.totalPrice.toLong() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val selectedDateAbaRevenue: StateFlow<Long> = selectedDateSales.map { list ->
        list.filter { it.paymentMethod == "ABA" }.sumOf { it.totalPrice.toLong() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Selected Date Product Breakdown Summary
    val selectedDateProductSummaries: StateFlow<List<ProductSaleSummary>> = selectedDateSales.map { sales ->
        val group = sales.groupBy { it.productId }
        ProductCatalog.items.mapNotNull { product ->
            val matchingSales = group[product.id]
            if (matchingSales != null && matchingSales.isNotEmpty()) {
                val totalQty = matchingSales.sumOf { it.quantity }
                val totalAmt = matchingSales.sumOf { it.totalPrice.toLong() }
                ProductSaleSummary(
                    product = product,
                    totalQuantity = totalQty,
                    totalAmount = totalAmt
                )
            } else {
                null
            }
        }.sortedByDescending { it.totalAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Date Closure Record
    val selectedDateClosure: StateFlow<DailyClosureRecord?> = _selectedReportDate
        .flatMapLatest { date -> repository.getDailyClosure(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // All Sales History (flat chronological stream)
    val allSalesHistory: StateFlow<List<SaleRecord>> = repository.getAllSales()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Navigation Tab Action
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun openSaleDialog(product: Product) {
        _activeProductForSale.value = product
        _activeQuantity.value = 1
    }

    fun closeSaleDialog() {
        _activeProductForSale.value = null
        _activeQuantity.value = 1
    }

    fun incrementQuantity() {
        _activeQuantity.value = (_activeQuantity.value + 1).coerceAtMost(999)
    }

    fun decrementQuantity() {
        if (_activeQuantity.value > 1) {
            _activeQuantity.value -= 1
        }
    }

    fun setQuantity(qty: Int) {
        if (qty in 1..999) {
            _activeQuantity.value = qty
        }
    }

    // --- CART FUNCTIONALITY ---

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = (existing.quantity + quantity).coerceAtMost(999)
            currentList[index] = existing.copy(quantity = newQty)
        } else {
            currentList.add(CartItem(product = product, quantity = quantity.coerceAtLeast(1)))
        }
        _cartItems.value = currentList
        closeSaleDialog()
    }

    fun updateCartItemQuantity(productId: String, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = existing.copy(quantity = newQty.coerceAtMost(999))
            }
            _cartItems.value = currentList
        }
    }

    fun removeCartItem(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    /**
     * Checkout all items in the cart at once
     */
    fun checkoutCart(paymentMethod: PaymentMethod = PaymentMethod.CASH) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val now = System.currentTimeMillis()
        val todayStr = Formatters.getTodayIsoString()
        val totalRevenue = items.sumOf { it.totalPriceRiel }
        val totalQty = items.sumOf { it.quantity }

        val records = items.mapIndexed { idx, item ->
            SaleRecord(
                id = now + idx,
                productId = item.product.id,
                productName = item.product.nameKh,
                unitPrice = item.product.priceRiel,
                quantity = item.quantity,
                totalPrice = item.totalPriceRiel,
                timestamp = now,
                dateString = todayStr,
                paymentMethod = paymentMethod.code
            )
        }

        val summaries = items.map { "${it.product.iconEmoji} ${it.product.nameKh} x${it.quantity}" }

        viewModelScope.launch {
            repository.recordSales(records)
            _cartItems.value = emptyList()

            _lastSaleSuccess.value = SaleSuccessEvent(
                productName = if (items.size == 1) items.first().product.nameKh else "ការទូទាត់ ${items.size} មុខទំនិញ",
                quantity = totalQty,
                totalAmount = totalRevenue,
                paymentMethod = paymentMethod,
                itemsSummary = summaries
            )

            // Auto sync to Google Drive if configured
            val config = _cloudConfig.value
            if (config.autoSyncOnSale && config.googleDriveScriptUrl.isNotBlank() && _isOnline.value) {
                pushSalesToGoogleDriveSilent()
            }
        }
    }

    /**
     * Instant single-item sell without going through cart
     */
    fun quickSellSingle(paymentMethod: PaymentMethod = PaymentMethod.CASH) {
        val product = _activeProductForSale.value ?: return
        val qty = _activeQuantity.value.coerceAtLeast(1)
        val totalPrice = product.priceRiel * qty
        val todayStr = Formatters.getTodayIsoString()
        val now = System.currentTimeMillis()

        val record = SaleRecord(
            id = now,
            productId = product.id,
            productName = product.nameKh,
            unitPrice = product.priceRiel,
            quantity = qty,
            totalPrice = totalPrice,
            timestamp = now,
            dateString = todayStr,
            paymentMethod = paymentMethod.code
        )

        viewModelScope.launch {
            repository.recordSale(record)
            _lastSaleSuccess.value = SaleSuccessEvent(
                productName = product.nameKh,
                quantity = qty,
                totalAmount = totalPrice,
                paymentMethod = paymentMethod,
                itemsSummary = listOf("${product.iconEmoji} ${product.nameKh} x$qty")
            )
            _activeProductForSale.value = null
            _activeQuantity.value = 1

            // Auto sync to Google Drive if configured
            val config = _cloudConfig.value
            if (config.autoSyncOnSale && config.googleDriveScriptUrl.isNotBlank() && _isOnline.value) {
                pushSalesToGoogleDriveSilent()
            }
        }
    }

    fun dismissSuccessFeedback() {
        _lastSaleSuccess.value = null
    }

    fun selectReportDate(dateString: String) {
        _selectedReportDate.value = dateString
    }

    fun closeCurrentDay(notes: String = "") {
        val dateString = _selectedReportDate.value
        val sales = selectedDateSales.value
        val totalRevenue = sales.sumOf { it.totalPrice.toLong() }
        val totalItems = sales.sumOf { it.quantity }
        val totalTransactions = sales.size

        val closure = DailyClosureRecord(
            dateString = dateString,
            closedAtTimestamp = System.currentTimeMillis(),
            totalRevenue = totalRevenue,
            totalItems = totalItems,
            totalTransactions = totalTransactions,
            notes = notes
        )

        viewModelScope.launch {
            repository.recordDailyClosure(closure)

            // Auto sync closure to Google Drive
            val config = _cloudConfig.value
            if (config.googleDriveScriptUrl.isNotBlank() && _isOnline.value) {
                pushSalesToGoogleDriveSilent()
            }

            // If Telegram Bot is configured, automatically send closure report
            if (config.telegramBotToken.isNotBlank() && config.telegramChatId.isNotBlank()) {
                val reportText = TelegramHelper.generateReportText(
                    dateIso = dateString,
                    sales = sales,
                    productSummaries = selectedDateProductSummaries.value,
                    dailyClosure = closure
                )
                TelegramHelper.sendViaTelegramBotApi(
                    botToken = config.telegramBotToken,
                    chatId = config.telegramChatId,
                    message = reportText
                )
            }
        }
    }

    fun refreshNetworkStatus() {
        _isOnline.value = CloudSyncManager.isNetworkAvailable(getApplication())
    }

    fun updateCloudConfig(config: CloudConfig) {
        _cloudConfig.value = config
        CloudSyncManager.saveConfig(getApplication(), config)
        _feedbackMessage.value = SyncFeedbackMessage("បានរក្សាទុកការកំណត់ Google Drive & Telegram រួចរាល់", true)
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun triggerTelegramShare(context: android.content.Context, dateIso: String) {
        val sales = selectedDateSales.value
        val summaries = selectedDateProductSummaries.value
        val closure = selectedDateClosure.value
        val reportText = TelegramHelper.generateReportText(dateIso, sales, summaries, closure)
        TelegramHelper.shareViaTelegram(context, reportText)
    }

    fun sendTelegramBotReportDirect() {
        val config = _cloudConfig.value
        val dateString = _selectedReportDate.value
        val sales = selectedDateSales.value
        val summaries = selectedDateProductSummaries.value
        val closure = selectedDateClosure.value

        if (config.telegramBotToken.isBlank() || config.telegramChatId.isBlank()) {
            _feedbackMessage.value = SyncFeedbackMessage("សូមកំណត់ Bot Token និង Chat ID ជាមុនសិន", false)
            return
        }

        viewModelScope.launch {
            _syncState.value = SyncState.SYNCING
            val reportText = TelegramHelper.generateReportText(dateString, sales, summaries, closure)
            val result = TelegramHelper.sendViaTelegramBotApi(
                botToken = config.telegramBotToken,
                chatId = config.telegramChatId,
                message = reportText
            )

            result.onSuccess {
                _syncState.value = SyncState.SUCCESS
                _feedbackMessage.value = SyncFeedbackMessage("ផ្ញើរបាយការណ៍ទៅ Telegram បានជោគជ័យ!", true)
            }.onFailure { err ->
                _syncState.value = SyncState.ERROR
                _feedbackMessage.value = SyncFeedbackMessage("បរាជ័យក្នុងការផ្ញើទៅ Telegram: ${err.message}", false)
            }
        }
    }

    // --- GOOGLE DRIVE SHARED DATABASE SYNC ---

    /**
     * 2-Way Sync with Google Drive:
     * 1. Pulls all remote sales and closures from Google Drive and merges into local DB
     * 2. Pushes all local sales and closures to Google Drive
     */
    fun syncWithGoogleDrive(silent: Boolean = false) {
        val config = _cloudConfig.value
        refreshNetworkStatus()

        if (!_isOnline.value) {
            if (!silent) _feedbackMessage.value = SyncFeedbackMessage("គ្មានការតភ្ជាប់អ៊ីនធឺណិតទេ (Offline)", false)
            return
        }

        if (config.googleDriveScriptUrl.isBlank()) {
            if (!silent) _feedbackMessage.value = SyncFeedbackMessage("សូមកំណត់ Google Drive Script URL ជាមុនសិន", false)
            return
        }

        viewModelScope.launch {
            _syncState.value = SyncState.SYNCING

            // 1. Pull remote records from Google Drive
            val pullResult = CloudSyncManager.pullFromGoogleDrive(config.googleDriveScriptUrl)
            pullResult.onSuccess { data ->
                if (data.newSales.isNotEmpty()) {
                    repository.recordSales(data.newSales)
                }
                data.newClosures.forEach { closure ->
                    repository.recordDailyClosure(closure)
                }
            }

            // 2. Push current local dataset to Google Drive
            val allSales = repository.getAllSales().first()
            val allClosures = repository.getAllDailyClosures().first()

            val pushResult = CloudSyncManager.pushToGoogleDrive(config.googleDriveScriptUrl, allSales, allClosures)
            pushResult.onSuccess {
                val now = System.currentTimeMillis()
                val updated = config.copy(lastSyncTimestamp = now)
                _cloudConfig.value = updated
                CloudSyncManager.saveConfig(getApplication(), updated)
                _syncState.value = SyncState.SUCCESS
                if (!silent) {
                    _feedbackMessage.value = SyncFeedbackMessage("Sync ជាមួយ Google Drive Database ជោគជ័យ (${allSales.size} វិក្កយបត្រ)", true)
                }
            }.onFailure { err ->
                _syncState.value = SyncState.ERROR
                if (!silent) {
                    _feedbackMessage.value = SyncFeedbackMessage("Sync Google Drive មិនបានសម្រេច: ${err.message}", false)
                }
            }
        }
    }

    private suspend fun pushSalesToGoogleDriveSilent() {
        try {
            val config = _cloudConfig.value
            val allSales = repository.getAllSales().first()
            val allClosures = repository.getAllDailyClosures().first()
            CloudSyncManager.pushToGoogleDrive(config.googleDriveScriptUrl, allSales, allClosures)
        } catch (_: Exception) {
            // Background silent push
        }
    }
}
