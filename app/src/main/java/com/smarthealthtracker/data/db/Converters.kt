package com.smarthealthtracker.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smarthealthtracker.data.entities.Category
import com.smarthealthtracker.data.entities.DayOfWeek
import com.smarthealthtracker.data.entities.EntryStatus
import com.smarthealthtracker.data.entities.MealType

/**
 * Room TypeConverters for non-primitive types.
 */
class Converters {

    private val gson = Gson()

    @TypeConverter fun categoryToString(v: Category?): String? = v?.name
    @TypeConverter fun stringToCategory(v: String?): Category? = v?.let { Category.valueOf(it) }

    @TypeConverter fun mealTypeToString(v: MealType?): String? = v?.name
    @TypeConverter fun stringToMealType(v: String?): MealType? = v?.let { MealType.valueOf(it) }

    @TypeConverter fun statusToString(v: EntryStatus?): String? = v?.name
    @TypeConverter fun stringToStatus(v: String?): EntryStatus? = v?.let { EntryStatus.valueOf(it) }

    @TypeConverter
    fun dayListToJson(v: List<DayOfWeek>?): String? =
        v?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToDayList(v: String?): List<DayOfWeek>? =
        v?.let { gson.fromJson(it, object : TypeToken<List<DayOfWeek>>() {}.type) }

    @TypeConverter
    fun mapToJson(v: Map<String, String>?): String? =
        v?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToMap(v: String?): Map<String, String>? =
        v?.let { gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type) }
}
