package com.andarb.movietinder.util

import androidx.room.TypeConverter
import java.util.*

/**
 * Allows storing and retrieving date value from local db.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}