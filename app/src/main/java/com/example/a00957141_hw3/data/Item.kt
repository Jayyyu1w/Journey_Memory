package com.example.a00957141_hw3.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Item")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "start_date")
    val startDate: String,
    @ColumnInfo(name = "end_date")
    val endDate: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "content")
    val content: List<CellPreserveData>,
    @ColumnInfo(name = "tags")
    val tags: String)