package com.example.Journey_Memory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Journey_Memory.data.Item
import com.example.Journey_Memory.data.ItemDao
import com.example.Journey_Memory.data.ItemRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.reflect.typeOf

class MemoryActivity : AppCompatActivity() {
    private lateinit var journeyActivityResultLauncher: ActivityResultLauncher<Intent>
    private var clickGameId: Int = 0
    private lateinit var soundPool: SoundPool
    override fun onCreate(savedInstanceState: Bundle?) {
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        clickGameId = soundPool.load(this, R.raw.click_game, 1)
        supportActionBar?.hide() // 隱藏標題欄
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)
        // 使用 Room 建立資料庫
        val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(this) }
        val diaryDao = database.itemDao()
        val recyclerView = findViewById<RecyclerView>(R.id.diary_list)
        recyclerView.layoutManager = LinearLayoutManager(this) // 設置布局管理器，例如 LinearLayoutManager
        journeyActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                soundPool.play(clickGameId, 1.0f, 1.0f, 1, 0, 1.0f) // 成功音效
                // 儲存成功的處理邏輯，顯示儲存成功的提示
                Toast.makeText(this, "儲存成功", Toast.LENGTH_SHORT).show()
            }
        }
        val adapter = DiaryAdapter(diaryDao,lifecycleScope,journeyActivityResultLauncher) // 建立adapter instance
        recyclerView.adapter = adapter // 設置adapter
        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        // 取得日曆的動畫
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.anim)
        // 設置動畫
        calendarView.startAnimation(fadeInAnimation)
        recyclerView.startAnimation(fadeInAnimation)

        // 取得當前日期
        val currentDate = getCurrentDate()

        // 查詢當天的資料庫紀錄
        lifecycleScope.launch {
            val items = diaryDao.getItemsByDate(currentDate)
            // 觀察數據變化，當數據變化時，會自動更新UI
            items.observe(this@MemoryActivity,
                Observer<List<Item?>?> { items ->
                    // 在這裡獲取最新的數據並進行相應的處理
                    val itemList: List<Item> = items.filterNotNull()
                    Log.d("MemoryActivity", "itemList: $itemList")
                    adapter.setData(itemList)
                })
        }

        // 設定日曆點擊事件
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // 每次點擊日曆時，會去查詢該日期的日記
            // 使用協程來執行異步操作
            lifecycleScope.launch {
                val monthString = "%02d".format(month + 1)
                val dayString = "%02d".format(dayOfMonth)
                val items = diaryDao.getItemsByDate("$year/${monthString}/$dayString")
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
    private fun getCurrentDate(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        println("Current Date: ${dateFormat.format(currentDate)}")
        //println(dateFormat)
        return dateFormat.format(currentDate)
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

class DiaryAdapter(private val diaryDao: ItemDao,private val lifecycleScope: LifecycleCoroutineScope,private val journeyActivityResultLauncher: ActivityResultLauncher<Intent>) : RecyclerView.Adapter<DiaryViewHolder>() {
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
            intent.putExtra("journalTitle", data.tags.toString())
            journeyActivityResultLauncher.launch(intent)
        }
        holder.itemView.setOnLongClickListener {
            if (position != RecyclerView.NO_POSITION) {
                val itemToDelete = dataList[position]
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("刪除?")
                    .setMessage("您確認要刪除該日記嗎?\n(刪除後即無法恢復)")
                    .setPositiveButton("確定") { dialog, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            diaryDao.deleteItem(itemToDelete)
                        }
                        dataList.removeAt(position)
                        notifyItemRemoved(position)
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            true
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