package com.andarb.movietinder.util

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Allows storing and retrieving date value from local db.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}