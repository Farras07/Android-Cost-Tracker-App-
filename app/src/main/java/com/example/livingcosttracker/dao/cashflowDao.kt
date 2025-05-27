package com.example.livingcosttracker.dao

import androidx.room.*
import com.example.livingcosttracker.db.Cashflow
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

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
}