package com.example.livingcosttracker.db
import androidx.room.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "fix_monthly_income") val fix_monthly_income: Int,
    @ColumnInfo(name = "savingPercentage") val savingPercentage: Float,
    )

