package com.example.livingcosttracker.dao

import androidx.room.*
import com.example.livingcosttracker.db.Cashflow
import kotlinx.coroutines.flow.Flow

@Dao
interface CashflowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashflow(cashflow: Cashflow)

    @Query("SELECT * FROM cashflow")
    fun getAllCashflow(): Flow<List<com.example.livingcosttracker.db.Cashflow>>
}