package com.example.a00957141_hw3

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.a00957141_hw3.data.Item
import com.example.a00957141_hw3.data.ItemRoomDatabase
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var journeyActivityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
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
                // 儲存成功的處理邏輯，顯示儲存成功的提示
                Toast.makeText(this, "儲存成功", Toast.LENGTH_SHORT).show()
            }
        }

        buttonStTime.setOnClickListener(View.OnClickListener { showDatePickerDialog(0, editStTime) })
        buttonEdTime.setOnClickListener(View.OnClickListener { showDatePickerDialog(1, editEdTime) })
        buttonMemory.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MemoryActivity::class.java)
            startActivity(intent)
        })
        buttonStJourney.setOnClickListener(View.OnClickListener {
            stDay = editStTime.text.toString()
            edDay = editEdTime.text.toString()
            if(stDay == "" || edDay == ""){
                Toast.makeText(applicationContext, "請選擇日期", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }else{
                val intent = Intent(this@MainActivity, JourneyActivity::class.java)
                intent.putExtra("journalType", spinner.selectedItem.toString())
                val journalDates = arrayOf(stDay, edDay)
                intent.putExtra("journalDates", journalDates)
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

                // Update EditText with selected date
                val selectedDate: String =
                    (mYear.toString() + "/" + (mMonth + 1).toString() + "/" + mDay.toString())
                if (i == 0)
                    editTime.setText(selectedDate)
                else
                    editTime.setText(selectedDate)
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }
}
