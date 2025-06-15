package com.example.livingcosttracker.dao

import androidx.room.*
import com.example.livingcosttracker.db.Cashflow
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import java.util.Date

@Dao
interface CashflowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashflow(cashflow: Cashflow)

    @Query("SELECT * FROM cashflow")
    fun getAllCashflow(): Flow<List<Cashflow>>

    @Query("""
    SELECT SUM(
        CASE 
            WHEN category = 'Income' THEN total
            WHEN category = 'Cost' THEN -total
            ELSE 0
        END
    ) 
    FROM cashflow 
    WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :yearMonth
""")
    suspend fun getBalanceThisMonth(yearMonth: String): Int?

    @Query("""
    SELECT SUM(total) FROM cashflow 
    WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :yearMonth 
    AND category = 'Cost'
    """)
    suspend fun getCashflowExpenseByMonth(yearMonth: String): Int?

    @Query("""
    SELECT DISTINCT strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) AS yearMonth
    FROM cashflow
    ORDER BY yearMonth
    """)
    suspend fun getYearMonthCashflow(): List<String>

    @Query(""" SELECT * FROM cashflow
            WHERE strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) = :yearMonth
    """)
    suspend fun getCashflowByYearMonth(yearMonth: String): List<Cashflow>

    @Query("""
    SELECT * FROM cashflow 
    WHERE category = :category
    """)
    suspend fun getCashflowByCategories(category: String): List<Cashflow>

    @Query("""
    SELECT * FROM cashflow 
    WHERE itemCategory = :itemCategory
    """)
    suspend fun getCashflowByItemCategories(itemCategory: String): List<Cashflow>

    @Query("""
        DELETE FROM cashflow
        WHERE id = :id
    """)
    suspend fun deleteCashflowById(id: String)
}