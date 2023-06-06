package com.example.a00957141_hw3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a00957141_hw3.data.CellPreserveData
import com.example.a00957141_hw3.data.Item
import com.example.a00957141_hw3.data.ItemDao
import kotlinx.coroutines.launch

class DiaryViewModel(private val itemDao: ItemDao) : ViewModel() {
    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(id: Long, itemStartDate: String, itemEndDate: String, itemTitle: String, itemContent: List<CellPreserveData>, itemTag: String) {
        val newItem = getNewItemEntry(id, itemStartDate, itemEndDate, itemTitle, itemContent, itemTag)
        insertItem(newItem)
    }
    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    // 擷取item物件，並將其以非封鎖的方式插入資料庫
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insertItem(item)
        }
    }
    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(itemName: String, itemPrice: String, itemCount: String): Boolean {
        if (itemName.isBlank() || itemPrice.isBlank() || itemCount.isBlank()) {
            return false
        }
        return true
    }
    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(id: Long, itemStartDate: String, itemEndDate: String, itemTitle: String, itemContent: List<CellPreserveData>, itemTag: String): Item {
        return Item(
            id=id,
            startDate = itemStartDate,
            endDate = itemEndDate,
            title = itemTitle,
            content = itemContent,
            tags = itemTag,
        )
    }
}

class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}