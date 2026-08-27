package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleRecord>): List<Long>

    @Query("SELECT * FROM sales_records ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleRecord>>

    @Query("SELECT * FROM sales_records WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getSalesByDate(dateString: String): Flow<List<SaleRecord>>

    @Query("SELECT DISTINCT dateString FROM sales_records ORDER BY dateString DESC")
    fun getAllSaleDates(): Flow<List<String>>

    @Query("SELECT SUM(totalPrice) FROM sales_records WHERE dateString = :dateString")
    fun getTotalRevenueByDate(dateString: String): Flow<Long?>

    @Query("SELECT SUM(quantity) FROM sales_records WHERE dateString = :dateString")
    fun getTotalItemsByDate(dateString: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyClosure(closure: DailyClosureRecord)

    @Query("SELECT * FROM daily_closures WHERE dateString = :dateString")
    fun getDailyClosure(dateString: String): Flow<DailyClosureRecord?>

    @Query("SELECT * FROM daily_closures ORDER BY dateString DESC")
    fun getAllDailyClosures(): Flow<List<DailyClosureRecord>>
}
