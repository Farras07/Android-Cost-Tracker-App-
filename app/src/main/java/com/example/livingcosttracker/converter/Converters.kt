package com.example.livingcosttracker.converter

import androidx.room.TypeConverter
import java.sql.Timestamp
import java.util.Date
import java.text.NumberFormat
import java.util.Locale

class Converters {

//    @TypeConverter
//    fun fromTimestamp(value: Long?): Timestamp? {
//        return value?.let { Timestamp(it) }
//    }
//
//    @TypeConverter
//    fun timestampToLong(timestamp: Timestamp?): Long? {
//        return timestamp?.time
//    }
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    fun formatRupiah(amount: Int): String {
        val formatter = NumberFormat.getInstance(Locale("in", "ID"))
        return "Rp. ${formatter.format(amount)}"
    }
}
