package com.example.livingcosttracker.db

import androidx.room.*
import java.sql.Timestamp

@Entity(
    tableName = "cashflow",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["idUser"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["idUser"])]  // recommended for foreign keys
)

data class Cashflow (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "total") val total: Int,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Timestamp,
    @ColumnInfo(name = "idUser") val idUser: Int,
)