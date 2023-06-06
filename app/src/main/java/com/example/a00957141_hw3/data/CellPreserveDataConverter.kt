package com.example.a00957141_hw3.data

import androidx.room.TypeConverter
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

class CellPreserveDataConverter {
    @TypeConverter
    fun fromList(value: List<CellPreserveData>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<CellPreserveData> {
        val gson = Gson()
        val type = object : TypeToken<List<CellPreserveData>>() {}.type
        return gson.fromJson(value, type)
    }
}