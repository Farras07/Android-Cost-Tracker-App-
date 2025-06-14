package com.example.livingcosttracker.db

import androidx.room.*
import java.sql.Timestamp
import java.util.Date

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
    indices = [Index(value = ["idUser"])]
)

data class Cashflow (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "total") val total: Int,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "itemCategory") val itemCategory: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "idUser") val idUser: Int,
) {
    val amount: Any
        get() {
            TODO()
        }
}