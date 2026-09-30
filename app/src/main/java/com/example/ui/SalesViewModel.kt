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
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.util.CloudConfig
import com.example.util.CloudSyncManager
import com.example.util.Formatters
import com.example.util.SessionManager
import com.example.util.SyncState
import com.example.util.TelegramHelper
import java.util.Locale
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

enum class ReportPeriod(val titleKh: String, val titleEn: String, val iconEmoji: String) {
    DAILY("ប្រចាំថ្ងៃ", "Daily", "📅"),
    WEEKLY("ប្រចាំសប្ដាហ៍", "Weekly", "📊"),
    ANNUALLY("ប្រចាំឆ្នាំ", "Annually", "📈")
}

data class DaySalesStat(
    val dateIso: String,
    val dayNameKhmer: String,
    val dayOfMonth: String,
    val revenue: Long,
    val itemsCount: Int,
    val transactionsCount: Int,
    val cashRevenue: Long,
    val abaRevenue: Long,
    val isToday: Boolean
)

data class MonthSalesStat(
    val monthNumber: Int,
    val monthNameKhmer: String,
    val revenue: Long,
    val itemsCount: Int,
    val transactionsCount: Int,
    val cashRevenue: Long,
    val abaRevenue: Long
)

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

    // --- AUTHENTICATION & SESSION STATE ---
    private val _currentUserSession = MutableStateFlow<UserSession?>(SessionManager.getSession(application))
    val currentUserSession: StateFlow<UserSession?> = _currentUserSession.asStateFlow()

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
        repository = SalesRepository(db.salesDao(), db.productDao())
        refreshNetworkStatus()

        // Seed default products if not yet in database
        viewModelScope.launch {
            repository.ensureDefaultProductsSeeded()
        }

        // Auto-pull from Google Drive on startup if URL is configured
        if (_cloudConfig.value.googleDriveScriptUrl.isNotBlank()) {
            syncWithGoogleDrive(silent = true)
        }
    }

    // --- USER AUTHENTICATION ACTIONS ---

    fun login(role: UserRole, pin: String): Boolean {
        if (pin.trim() == role.fixedPin) {
            val session = UserSession(
                username = role.defaultUsername,
                role = role
            )
            SessionManager.saveSession(getApplication(), session)
            _currentUserSession.value = session
            return true
        }
        return false
    }

    fun logout() {
        SessionManager.clearSession(getApplication())
        _currentUserSession.value = null
        _selectedTab.value = 0
        _cartItems.value = emptyList()
        _activeProductForSale.value = null
    }

    // --- PRODUCTS & INVENTORY STATE ---

    val products: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductCatalog.items
        )

    val lowStockProducts: StateFlow<List<Product>> = products.map { list ->
        list.filter { it.isLowStock }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val outOfStockProducts: StateFlow<List<Product>> = products.map { list ->
        list.filter { it.isOutOfStock }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val alertStockProducts: StateFlow<List<Product>> = products.map { list ->
        list.filter { it.isOutOfStock || it.isLowStock }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Product CRUD Operations (Admin Only)

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _feedbackMessage.value = SyncFeedbackMessage("បានរក្សាទុកទំនិញ '${product.nameKh}' ជោគជ័យ", true)
        }
    }

    fun deleteProduct(productId: String, productName: String = "") {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            // Also remove from cart if present
            _cartItems.value = _cartItems.value.filter { it.product.id != productId }
            _feedbackMessage.value = SyncFeedbackMessage("បានលុបទំនិញ '$productName' រួចរាល់", true)
        }
    }

    fun quickRestock(productId: String, amount: Int) {
        viewModelScope.launch {
            repository.restockProduct(productId, amount)
            _feedbackMessage.value = SyncFeedbackMessage("បានបន្ថែមស្តុក +$amount ជោគជ័យ", true)
        }
    }

    fun updateStockCount(productId: String, newStock: Int) {
        viewModelScope.launch {
            repository.updateStockCount(productId, newStock.coerceAtLeast(0))
            _feedbackMessage.value = SyncFeedbackMessage("បានកែប្រែចំនួនស្តុកជោគជ័យ", true)
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

    // Selected Date Product Breakdown Summary (dynamically joins sales with products)
    val selectedDateProductSummaries: StateFlow<List<ProductSaleSummary>> = combine(
        selectedDateSales,
        products
    ) { sales, currentProducts ->
        val group = sales.groupBy { it.productId }
        currentProducts.mapNotNull { product ->
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

    // --- REPORT PERIOD (Daily, Weekly, Annually) ---
    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.DAILY)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    // Weekly anchor date (defaults to today)
    private val _selectedWeeklyDate = MutableStateFlow(Formatters.getTodayIsoString())
    val selectedWeeklyDate: StateFlow<String> = _selectedWeeklyDate.asStateFlow()

    // Annual anchor year (defaults to current year)
    private val _selectedAnnualYear = MutableStateFlow(Formatters.getCurrentYear())
    val selectedAnnualYear: StateFlow<Int> = _selectedAnnualYear.asStateFlow()

    // Weekly Sales Stream
    val weeklySales: StateFlow<List<SaleRecord>> = combine(allSalesHistory, _selectedWeeklyDate) { allSales, anchorDate ->
        val (startIso, endIso) = Formatters.getWeekBoundaries(anchorDate)
        allSales.filter { it.dateString in startIso..endIso }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyRevenue: StateFlow<Long> = weeklySales.map { list -> list.sumOf { it.totalPrice.toLong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val weeklyCashRevenue: StateFlow<Long> = weeklySales.map { list ->
        list.filter { it.paymentMethod != "ABA" }.sumOf { it.totalPrice.toLong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val weeklyAbaRevenue: StateFlow<Long> = weeklySales.map { list ->
        list.filter { it.paymentMethod == "ABA" }.sumOf { it.totalPrice.toLong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val weeklyItemsCount: StateFlow<Int> = weeklySales.map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weeklyTransactionsCount: StateFlow<Int> = weeklySales.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weeklyDailyBreakdown: StateFlow<List<DaySalesStat>> = combine(allSalesHistory, _selectedWeeklyDate) { allSales, anchorDate ->
        val days = Formatters.getDaysOfWeek(anchorDate)
        val salesByDate = allSales.groupBy { it.dateString }
        days.map { dayItem ->
            val daySales = salesByDate[dayItem.dateIso] ?: emptyList()
            DaySalesStat(
                dateIso = dayItem.dateIso,
                dayNameKhmer = dayItem.dayNameKhmer,
                dayOfMonth = dayItem.dayOfMonth,
                revenue = daySales.sumOf { it.totalPrice.toLong() },
                itemsCount = daySales.sumOf { it.quantity },
                transactionsCount = daySales.size,
                cashRevenue = daySales.filter { it.paymentMethod != "ABA" }.sumOf { it.totalPrice.toLong() },
                abaRevenue = daySales.filter { it.paymentMethod == "ABA" }.sumOf { it.totalPrice.toLong() },
                isToday = dayItem.isToday
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyProductSummaries: StateFlow<List<ProductSaleSummary>> = combine(weeklySales, products) { sales, currentProducts ->
        val group = sales.groupBy { it.productId }
        currentProducts.mapNotNull { product ->
            val matching = group[product.id]
            if (matching != null && matching.isNotEmpty()) {
                ProductSaleSummary(
                    product = product,
                    totalQuantity = matching.sumOf { it.quantity },
                    totalAmount = matching.sumOf { it.totalPrice.toLong() }
                )
            } else null
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Annual Sales Stream
    val annualSales: StateFlow<List<SaleRecord>> = combine(allSalesHistory, _selectedAnnualYear) { allSales, year ->
        val yearPrefix = "$year-"
        allSales.filter { it.dateString.startsWith(yearPrefix) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val annualRevenue: StateFlow<Long> = annualSales.map { list -> list.sumOf { it.totalPrice.toLong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val annualCashRevenue: StateFlow<Long> = annualSales.map { list ->
        list.filter { it.paymentMethod != "ABA" }.sumOf { it.totalPrice.toLong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val annualAbaRevenue: StateFlow<Long> = annualSales.map { list ->
        list.filter { it.paymentMethod == "ABA" }.sumOf { it.totalPrice.toLong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val annualItemsCount: StateFlow<Int> = annualSales.map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val annualTransactionsCount: StateFlow<Int> = annualSales.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val annualMonthlyBreakdown: StateFlow<List<MonthSalesStat>> = combine(annualSales, _selectedAnnualYear) { sales, year ->
        (1..12).map { monthNum ->
            val monthStr = String.format(Locale.US, "%02d", monthNum)
            val prefix = "$year-$monthStr"
            val monthSales = sales.filter { it.dateString.startsWith(prefix) }
            MonthSalesStat(
                monthNumber = monthNum,
                monthNameKhmer = Formatters.getKhmerMonthName(monthNum),
                revenue = monthSales.sumOf { it.totalPrice.toLong() },
                itemsCount = monthSales.sumOf { it.quantity },
                transactionsCount = monthSales.size,
                cashRevenue = monthSales.filter { it.paymentMethod != "ABA" }.sumOf { it.totalPrice.toLong() },
                abaRevenue = monthSales.filter { it.paymentMethod == "ABA" }.sumOf { it.totalPrice.toLong() }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val annualProductSummaries: StateFlow<List<ProductSaleSummary>> = combine(annualSales, products) { sales, currentProducts ->
        val group = sales.groupBy { it.productId }
        currentProducts.mapNotNull { product ->
            val matching = group[product.id]
            if (matching != null && matching.isNotEmpty()) {
                ProductSaleSummary(
                    product = product,
                    totalQuantity = matching.sumOf { it.quantity },
                    totalAmount = matching.sumOf { it.totalPrice.toLong() }
                )
            } else null
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAvailableYears: StateFlow<List<Int>> = allSalesHistory.map { sales ->
        val years = sales.mapNotNull { sale ->
            sale.dateString.take(4).toIntOrNull()
        }.toSet().toMutableSet()
        years.add(Formatters.getCurrentYear())
        years.sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Formatters.getCurrentYear()))

    // Navigation Tab Action
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun openSaleDialog(product: Product) {
        if (product.isOutOfStock) {
            _feedbackMessage.value = SyncFeedbackMessage("ទំនិញ '${product.nameKh}' អស់ពីស្តុកហើយ!", false)
            return
        }
        _activeProductForSale.value = product
        _activeQuantity.value = 1
    }

    fun closeSaleDialog() {
        _activeProductForSale.value = null
        _activeQuantity.value = 1
    }

    fun incrementQuantity() {
        val currentProduct = _activeProductForSale.value ?: return
        val maxAllowed = currentProduct.stockCount.coerceAtLeast(1)
        if (_activeQuantity.value < maxAllowed) {
            _activeQuantity.value += 1
        } else {
            _feedbackMessage.value = SyncFeedbackMessage("ស្តុកមានត្រឹមតែ $maxAllowed ឯកតា!", false)
        }
    }

    fun decrementQuantity() {
        if (_activeQuantity.value > 1) {
            _activeQuantity.value -= 1
        }
    }

    fun setQuantity(qty: Int) {
        val currentProduct = _activeProductForSale.value ?: return
        val maxAllowed = currentProduct.stockCount.coerceAtLeast(1)
        if (qty in 1..maxAllowed) {
            _activeQuantity.value = qty
        } else if (qty > maxAllowed) {
            _activeQuantity.value = maxAllowed
            _feedbackMessage.value = SyncFeedbackMessage("កំណត់ត្រឹម $maxAllowed (ស្តុកអតិបរមា)", false)
        }
    }

    // --- CART FUNCTIONALITY WITH STOCK CHECKS ---

    fun addToCart(product: Product, quantity: Int = 1) {
        if (product.isOutOfStock) {
            _feedbackMessage.value = SyncFeedbackMessage("ទំនិញ '${product.nameKh}' អស់ពីស្តុកហើយ មិនអាចដាក់កន្ត្រកបានទេ!", false)
            return
        }

        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        val existingQty = if (index >= 0) currentList[index].quantity else 0
        val targetQty = existingQty + quantity

        if (targetQty > product.stockCount) {
            val addable = (product.stockCount - existingQty).coerceAtLeast(0)
            if (addable > 0) {
                if (index >= 0) {
                    currentList[index] = currentList[index].copy(quantity = product.stockCount)
                } else {
                    currentList.add(CartItem(product = product, quantity = addable))
                }
                _cartItems.value = currentList
                _feedbackMessage.value = SyncFeedbackMessage("បានដាក់កន្ត្រកត្រឹម $addable ឯកតា (ស្តុកសរុប ${product.stockCount})", false)
            } else {
                _feedbackMessage.value = SyncFeedbackMessage("ស្តុកមានត្រឹម ${product.stockCount} ឯកតា (ក្នុងកន្ត្រករួចហើយ)", false)
            }
        } else {
            if (index >= 0) {
                currentList[index] = currentList[index].copy(quantity = targetQty)
            } else {
                currentList.add(CartItem(product = product, quantity = quantity.coerceAtLeast(1)))
            }
            _cartItems.value = currentList
        }
        closeSaleDialog()
    }

    fun updateCartItemQuantity(productId: String, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = currentList[index]
            val productInCatalog = products.value.find { it.id == productId } ?: existing.product
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else if (newQty > productInCatalog.stockCount) {
                _feedbackMessage.value = SyncFeedbackMessage("ស្តុកមានត្រឹមតែ ${productInCatalog.stockCount} ឯកតា!", false)
            } else {
                currentList[index] = existing.copy(quantity = newQty)
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
     * Checkout all items in the cart at once, automatically depleting stock
     */
    fun checkoutCart(paymentMethod: PaymentMethod = PaymentMethod.CASH) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        // Verify stock for all items
        val currentCatalog = products.value.associateBy { it.id }
        for (item in items) {
            val liveProduct = currentCatalog[item.product.id]
            if (liveProduct != null && liveProduct.stockCount < item.quantity) {
                _feedbackMessage.value = SyncFeedbackMessage("ទំនិញ '${liveProduct.nameKh}' នៅសល់តែ ${liveProduct.stockCount} ប៉ុណ្ណោះ មិនគ្រប់គ្រាន់ទេ!", false)
                return
            }
        }

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
            // Record sales
            repository.recordSales(records)

            // Real-time stock depletion for each checked out product
            items.forEach { item ->
                repository.decrementStock(item.product.id, item.quantity)
            }

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
     * Instant single-item sell without going through cart, automatically depleting stock
     */
    fun quickSellSingle(paymentMethod: PaymentMethod = PaymentMethod.CASH) {
        val product = _activeProductForSale.value ?: return
        val currentCatalog = products.value.associateBy { it.id }
        val liveProduct = currentCatalog[product.id] ?: product

        val qty = _activeQuantity.value.coerceAtLeast(1)
        if (liveProduct.stockCount < qty) {
            _feedbackMessage.value = SyncFeedbackMessage("ទំនិញ '${liveProduct.nameKh}' នៅសល់តែ ${liveProduct.stockCount} ឯកតា មិនគ្រប់គ្រាន់ទេ!", false)
            return
        }

        val totalPrice = liveProduct.priceRiel * qty
        val todayStr = Formatters.getTodayIsoString()
        val now = System.currentTimeMillis()

        val record = SaleRecord(
            id = now,
            productId = liveProduct.id,
            productName = liveProduct.nameKh,
            unitPrice = liveProduct.priceRiel,
            quantity = qty,
            totalPrice = totalPrice,
            timestamp = now,
            dateString = todayStr,
            paymentMethod = paymentMethod.code
        )

        viewModelScope.launch {
            repository.recordSale(record)

            // Real-time stock decrement
            repository.decrementStock(liveProduct.id, qty)

            _lastSaleSuccess.value = SaleSuccessEvent(
                productName = liveProduct.nameKh,
                quantity = qty,
                totalAmount = totalPrice,
                paymentMethod = paymentMethod,
                itemsSummary = listOf("${liveProduct.iconEmoji} ${liveProduct.nameKh} x$qty")
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

    fun setReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
    }

    fun previousWeek() {
        _selectedWeeklyDate.value = Formatters.shiftWeek(_selectedWeeklyDate.value, -1)
    }

    fun nextWeek() {
        _selectedWeeklyDate.value = Formatters.shiftWeek(_selectedWeeklyDate.value, 1)
    }

    fun resetToCurrentWeek() {
        _selectedWeeklyDate.value = Formatters.getTodayIsoString()
    }

    fun selectAnnualYear(year: Int) {
        _selectedAnnualYear.value = year
    }

    fun previousYear() {
        _selectedAnnualYear.value -= 1
    }

    fun nextYear() {
        _selectedAnnualYear.value += 1
    }

    fun getCurrentReportText(): String {
        return when (_selectedReportPeriod.value) {
            ReportPeriod.DAILY -> {
                val dateIso = _selectedReportDate.value
                val sales = selectedDateSales.value
                val summaries = selectedDateProductSummaries.value
                val closure = selectedDateClosure.value
                TelegramHelper.generateReportText(dateIso, sales, summaries, closure)
            }
            ReportPeriod.WEEKLY -> {
                val (startIso, endIso) = Formatters.getWeekBoundaries(_selectedWeeklyDate.value)
                val sales = weeklySales.value
                val summaries = weeklyProductSummaries.value
                val breakdown = weeklyDailyBreakdown.value.map { it.dayNameKhmer to it.revenue }
                TelegramHelper.generateWeeklyReportText(startIso, endIso, sales, summaries, breakdown)
            }
            ReportPeriod.ANNUALLY -> {
                val year = _selectedAnnualYear.value
                val sales = annualSales.value
                val summaries = annualProductSummaries.value
                val breakdown = annualMonthlyBreakdown.value.map { it.monthNameKhmer to it.revenue }
                TelegramHelper.generateAnnualReportText(year, sales, summaries, breakdown)
            }
        }
    }

    fun triggerTelegramShare(context: android.content.Context, dateIso: String = _selectedReportDate.value) {
        val reportText = getCurrentReportText()
        TelegramHelper.shareViaTelegram(context, reportText)
    }

    fun sendTelegramBotReportDirect() {
        val config = _cloudConfig.value

        if (config.telegramBotToken.isBlank() || config.telegramChatId.isBlank()) {
            _feedbackMessage.value = SyncFeedbackMessage("សូមកំណត់ Bot Token និង Chat ID ជាមុនសិន", false)
            return
        }

        viewModelScope.launch {
            _syncState.value = SyncState.SYNCING
            val reportText = getCurrentReportText()
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
