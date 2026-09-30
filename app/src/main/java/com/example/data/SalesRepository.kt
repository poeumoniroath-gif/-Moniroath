package com.example.data

import com.example.model.Product
import com.example.model.ProductCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SalesRepository(
    private val salesDao: SalesDao,
    private val productDao: ProductDao
) {
    // --- SALES ---
    fun getAllSales(): Flow<List<SaleRecord>> = salesDao.getAllSales()

    fun getSalesByDate(dateString: String): Flow<List<SaleRecord>> =
        salesDao.getSalesByDate(dateString)

    fun getAllSaleDates(): Flow<List<String>> = salesDao.getAllSaleDates()

    fun getTotalRevenueByDate(dateString: String): Flow<Long?> =
        salesDao.getTotalRevenueByDate(dateString)

    fun getTotalItemsByDate(dateString: String): Flow<Int?> =
        salesDao.getTotalItemsByDate(dateString)

    suspend fun recordSale(sale: SaleRecord): Long = salesDao.insertSale(sale)

    suspend fun recordSales(sales: List<SaleRecord>): List<Long> = salesDao.insertSales(sales)

    suspend fun recordDailyClosure(closure: DailyClosureRecord) =
        salesDao.insertDailyClosure(closure)

    fun getDailyClosure(dateString: String): Flow<DailyClosureRecord?> =
        salesDao.getDailyClosure(dateString)

    fun getAllDailyClosures(): Flow<List<DailyClosureRecord>> =
        salesDao.getAllDailyClosures()

    // --- PRODUCTS & INVENTORY ---
    fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities ->
            entities.map { it.toProduct() }
        }

    suspend fun getProductById(id: String): Product? =
        productDao.getProductById(id)?.toProduct()

    suspend fun saveProduct(product: Product) {
        productDao.insertOrUpdateProduct(ProductEntity.fromProduct(product))
    }

    suspend fun deleteProduct(productId: String) {
        productDao.deleteProductById(productId)
    }

    suspend fun decrementStock(productId: String, quantity: Int) {
        productDao.decrementStock(productId, quantity)
    }

    suspend fun restockProduct(productId: String, amount: Int) {
        productDao.restockProduct(productId, amount)
    }

    suspend fun updateStockCount(productId: String, newStock: Int) {
        productDao.updateStockCount(productId, newStock)
    }

    suspend fun ensureDefaultProductsSeeded() {
        if (productDao.getProductCount() == 0) {
            val entities = ProductCatalog.items.map { ProductEntity.fromProduct(it) }
            productDao.insertProducts(entities)
        }
    }
}
