package com.example.Journey_Memory

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.Journey_Memory.data.ItemRoomDatabase
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var journeyActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var soundPool: SoundPool
    private var clickErrorId: Int = 0
    private var clickCoolId: Int = 0
    private var clickGameId: Int = 0
    private var clickSelctId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide() // 隱藏標題欄
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var stDay: String
        var edDay: String
        val buttonStTime: Button = findViewById(R.id.button_stdate)
        val editStTime: EditText = findViewById(R.id.edit_stdate)
        val buttonEdTime: Button = findViewById(R.id.button_eddate)
        val editEdTime: EditText = findViewById(R.id.edit_eddate)
        val buttonStJourney: Button = findViewById(R.id.button_start)
        val buttonMemory: Button = findViewById(R.id.button_edit)
        val spinner: Spinner = findViewById(R.id.spinners)
        val opts: List<String?> = listOf("旅遊", "美食", "其他")
        val adapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(applicationContext, R.layout.spinner_textset, opts)
        val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(this) }
        val diaryDao = database.itemDao()

        // 初始化音效
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        clickErrorId = soundPool.load(this, R.raw.click_error, 1)
        clickCoolId = soundPool.load(this, R.raw.click_cool, 1)
        clickGameId = soundPool.load(this, R.raw.click_game, 1)
        clickSelctId = soundPool.load(this, R.raw.click_select, 1)

        adapter.setDropDownViewResource(R.layout.spinner_textset)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "您選擇了：$selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 如果沒有選擇任何項目，這個方法會被呼叫
                val selectedItem = parent?.getItemAtPosition(0).toString()
                Toast.makeText(applicationContext, "您選擇了：$selectedItem", Toast.LENGTH_SHORT).show()
            }
        }
        // 初始化 ActivityResultLauncher
        journeyActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                soundPool.play(clickGameId, 1.0f, 1.0f, 1, 0, 1.0f) // 成功音效
                // 儲存成功的處理邏輯，顯示儲存成功的提示
                Toast.makeText(this, "儲存成功", Toast.LENGTH_SHORT).show()
            }
        }

        buttonStTime.setOnClickListener(View.OnClickListener {
            soundPool.play(clickCoolId, 1.0f, 1.0f, 1, 0, 1.0f) // 點擊音效
            showDatePickerDialog(0, editStTime)
        })
        buttonEdTime.setOnClickListener(View.OnClickListener {
            soundPool.play(clickCoolId, 1.0f, 1.0f, 1, 0, 1.0f) // 點擊音效
            showDatePickerDialog(1, editEdTime)
        })
        buttonMemory.setOnClickListener(View.OnClickListener {
            soundPool.play(clickCoolId, 1.0f, 1.0f, 1, 0, 1.0f) // 點擊音效
            val intent = Intent(this, MemoryActivity::class.java)
            startActivity(intent)
        })
        buttonStJourney.setOnClickListener(View.OnClickListener {
            stDay = editStTime.text.toString()
            edDay = editEdTime.text.toString()
            if(stDay == "" || edDay == ""){
                soundPool.play(clickErrorId, 1.0f, 1.0f, 1, 0, 1.0f) // 錯誤音效
                Toast.makeText(applicationContext, "請選擇日期", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }else{
                soundPool.play(clickCoolId, 1.0f, 1.0f, 1, 0, 1.0f) // 點擊音效
                val intent = Intent(this@MainActivity, JourneyActivity::class.java)
                intent.putExtra("journalType", spinner.selectedItem.toString())
                val journalDates = arrayOf(stDay, edDay)
                intent.putExtra("journalDates", journalDates)
                val id = "null"
                intent.putExtra("journalID",id)
                journeyActivityResultLauncher.launch(intent)
                lifecycleScope.launch {
                    // 在需要確認資料庫內容的地方（例如 MainActivity），使用 observe 方法觀察 LiveData
                    diaryDao.getAllItems().observe(this@MainActivity) { items ->
                        // 對獲取到的項目進行處理，例如顯示在 RecyclerView 或 Logcat 中
                        for (item in items) {
                            Log.d("DATABASE", "Item: $item")
                        }
                    }
                }
            }
        })
    }
    /**
     * 顯示選擇的日期
     */
    private fun showDatePickerDialog(i: Int, editTime: EditText) {
        // Get current date
        var mYear: Int
        var mMonth: Int
        var mDay: Int
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]

        // Create date picker dialog
        val datePickerDialog = DatePickerDialog(
            this@MainActivity,
            { _, year, monthOfYear, dayOfMonth -> // Save selected date
                mYear = year
                mMonth = monthOfYear
                mDay = dayOfMonth
                mMonth
                // Update EditText with selected date
                val selectedDate: String = String.format(
                    Locale.getDefault(),
                    "%04d/%02d/%02d",
                    mYear,
                    mMonth + 1,
                    mDay
                )
                if (i == 0)
                    editTime.setText(selectedDate)
                else
                    editTime.setText(selectedDate)
                soundPool.play(clickSelctId, 1.0f, 1.0f, 1, 0, 1.0f) // 點擊音效
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }
}
