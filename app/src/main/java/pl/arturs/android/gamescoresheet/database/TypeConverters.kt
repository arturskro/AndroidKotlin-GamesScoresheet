package pl.arturs.android.gamescoresheet.database

import androidx.room.TypeConverter
import java.util.Date

class TypeConverters {
    @TypeConverter
    fun fromDate(date: Date) : Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch : Long) : Date {
        return Date(millisSinceEpoch)
    }
}