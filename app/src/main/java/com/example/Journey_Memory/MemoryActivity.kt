package com.example.Journey_Memory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Journey_Memory.data.Item
import com.example.Journey_Memory.data.ItemRoomDatabase
import kotlinx.coroutines.launch


class MemoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide() // 隱藏標題欄
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)
        val recyclerView = findViewById<RecyclerView>(R.id.diary_list)
        recyclerView.layoutManager = LinearLayoutManager(this) // 設置布局管理器，例如 LinearLayoutManager
        val adapter = DiaryAdapter() // 建立adapter instance
        recyclerView.adapter = adapter // 設置adapter
        // 使用 Room 建立資料庫
        val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(this) }
        val diaryDao = database.itemDao()
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        // 設定日曆點擊事件
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // 每次點擊日曆時，會去查詢該日期的日記
            // 使用協程來執行異步操作
            lifecycleScope.launch {
                val items = diaryDao.getItemsByDate("$year/${month + 1}/$dayOfMonth")
                // 觀察數據變化，當數據變化時，會自動更新UI
                // 這裡的this指的是LifecycleOwner，也就是說，當Activity或Fragment被銷毀時，這個Observer也會被自動移除
                // 而這裡的this@MemoryActivity則是指向外層的Activity，如果用this則是lifecycleOwner的實例
                items.observe(this@MemoryActivity,
                    Observer<List<Item?>?> { items ->
                        // 在這裡獲取最新的數據並進行相應的處理
                        val itemList: List<Item> = items.filterNotNull()
                        Log.d("MemoryActivity", "itemList: $itemList")
                        adapter.setData(itemList)
                    })
            }
        }
    }
}

class DiaryViewHolder : RecyclerView.ViewHolder{
    val title: TextView
    val date: TextView
    val tags: TextView

    constructor(itemView: View) : super(itemView) {
        title = itemView.findViewById(R.id.diary_item_title)
        date = itemView.findViewById(R.id.diary_item_date)
        tags = itemView.findViewById(R.id.diary_item_type)
    }
}

class DiaryAdapter : RecyclerView.Adapter<DiaryViewHolder>() {

    private val dataList = mutableListOf<Item>()
    // 建立 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_list_item, parent, false)
        val layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, dpToPx(16, parent.context)) // 設置間距
        view.layoutParams = layoutParams
        return DiaryViewHolder(view)
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    // 綁定數據到 ViewHolder
    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val data = dataList[position]
        Log.d("data", "data: $data")
        // 設置 ViewHolder 中的元件
        // 例如：holder.textView.text = data.text
        holder.title.text = data.title
        holder.date.text = data.startDate + " ~ " + data.endDate
        holder.tags.text = data.tags
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, JourneyActivity::class.java)
            intent.putExtra("journalType", data.title.toString())
            val journalDates = arrayOf(data.startDate, data.endDate)
            intent.putExtra("journalDates", journalDates)
            intent.putExtra("journalID",data.id.toString())
            holder.itemView.context.startActivity(intent)
        }
    }

    // 返回數據的數量
    override fun getItemCount(): Int {
        return dataList.size
    }
    // 設置資料的地方
    fun setData(data: List<Item>) {
        dataList.clear()
        dataList.addAll(data)
        Log.d("dataList", "dataList: $dataList")
        notifyDataSetChanged()
    }
}