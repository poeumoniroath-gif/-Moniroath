package com.example.data

import kotlinx.coroutines.flow.Flow

class SalesRepository(private val salesDao: SalesDao) {
    fun getAllSales(): Flow<List<SaleRecord>> = salesDao.getAllSales()

    fun getSalesByDate(dateString: String): Flow<List<SaleRecord>> =
        salesDao.getSalesByDate(dateString)

    fun getAllSaleDates(): Flow<List<String>> = salesDao.getAllSaleDates()

    fun getTotalRevenueByDate(dateString: String): Flow<Long?> =
        salesDao.getTotalRevenueByDate(dateString)

    fun getTotalItemsByDate(dateString: String): Flow<Int?> =
        salesDao.getTotalItemsByDate(dateString)

    suspend fun recordSale(sale: SaleRecord): Long = salesDao.insertSale(sale)

    suspend fun recordDailyClosure(closure: DailyClosureRecord) =
        salesDao.insertDailyClosure(closure)

    fun getDailyClosure(dateString: String): Flow<DailyClosureRecord?> =
        salesDao.getDailyClosure(dateString)

    fun getAllDailyClosures(): Flow<List<DailyClosureRecord>> =
        salesDao.getAllDailyClosures()
}
