package com.example.a00957141_hw3.data

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertItem(item: Item)

    @Update
    fun updateItem(item: Item)

    @Delete
    fun deleteItem(item: Item)

    @Query("SELECT * FROM Item")
    fun getAllItems(): LiveData<List<Item>>

    @Query("SELECT * FROM Item WHERE start_date <= :curDate AND end_date >= :curDate")
    fun getItemsByDate(curDate: String): LiveData<List<Item>>
}