package com.example.livingcosttracker.db
import com.example.livingcosttracker.dao.UserDao
import com.example.livingcosttracker.dao.CashflowDao

import android.content.Context
import androidx.room.*
import com.example.livingcosttracker.converter.Converters

@Database(entities = [User::class, Cashflow::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cashflowDao(): CashflowDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cost_tracker_db"
                )
                    .build().also { INSTANCE = it }
            }
    }
}
