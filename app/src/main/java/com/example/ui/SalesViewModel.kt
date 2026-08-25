package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyClosureRecord
import com.example.data.SaleRecord
import com.example.data.SalesRepository
import com.example.model.Product
import com.example.model.ProductCatalog
import com.example.model.ProductCategory
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val timestamp: Long = System.currentTimeMillis()
)

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SalesRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SalesRepository(db.salesDao())
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

    // Actions
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

    fun confirmSale() {
        val product = _activeProductForSale.value ?: return
        val qty = _activeQuantity.value.coerceAtLeast(1)
        val totalPrice = product.priceRiel * qty
        val todayStr = Formatters.getTodayIsoString()

        val record = SaleRecord(
            productId = product.id,
            productName = product.nameKh,
            unitPrice = product.priceRiel,
            quantity = qty,
            totalPrice = totalPrice,
            timestamp = System.currentTimeMillis(),
            dateString = todayStr
        )

        viewModelScope.launch {
            repository.recordSale(record)
            _lastSaleSuccess.value = SaleSuccessEvent(
                productName = product.nameKh,
                quantity = qty,
                totalAmount = totalPrice
            )
            _activeProductForSale.value = null
            _activeQuantity.value = 1
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
        }
    }
}
