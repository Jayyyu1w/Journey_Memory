package com.example.a00957141_hw3

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.example.a00957141_hw3.data.Item
import com.example.a00957141_hw3.data.ItemDao
import com.example.a00957141_hw3.data.ItemRoomDatabase
import com.example.a00957141_hw3.data.CellPreserveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class JourneyActivity : AppCompatActivity() {

    private lateinit var titles: TextView
    private lateinit var dates: TextView
    private lateinit var saveBtn: ImageView
    private lateinit var addBtn: ImageView
    private lateinit var textAdd: ImageView
    private lateinit var imageAdd: ImageView
    private lateinit var layout: LinearLayout
    private lateinit var journalType: String
    private lateinit var journalDates: Array<String>
    private lateinit var database: ItemRoomDatabase
    private lateinit var diaryDao: ItemDao

    companion object {
        const val SAVE_SUCCESS_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey)

        titles = findViewById(R.id.journeyTp)
        dates = findViewById(R.id.journeyDt)
        saveBtn = findViewById(R.id.checkmark_icon)
        addBtn = findViewById(R.id.add_icon)
        textAdd = findViewById(R.id.text_add)
        imageAdd = findViewById(R.id.image_add)
        layout = findViewById(R.id.JourneyMainLayout)

        journalType = intent.getStringExtra("journalType")!!
        journalDates = intent.getStringArrayExtra("journalDates")!!

        database = ItemRoomDatabase.getDatabase(this)
        diaryDao = database.itemDao()

        titles.text = journalType
        dates.text = "${journalDates[0]} ~ ${journalDates[1]}"

        saveBtn.setOnClickListener {
            saveDiary(journalDates, journalType, diaryDao)

        }
        var extVisCnt = 0
        addBtn.setOnClickListener {
            if (extVisCnt == 0) {
                textAdd.visibility = View.VISIBLE
                imageAdd.visibility = View.VISIBLE
            } else {
                textAdd.visibility = View.GONE
                imageAdd.visibility = View.GONE
            }
            extVisCnt = (extVisCnt + 1) % 2
        }

        textAdd.setOnClickListener {
            val editText = EditText(this)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            editText.layoutParams = layoutParams
            editText.hint = "請在這裡輸入文字"
            layout.addView(editText)
        }

        imageAdd.setOnClickListener {
            val imageView = ImageView(this)
            imageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            imageView.setImageResource(R.drawable.baseline_rocket_24)
            layout.addView(imageView)
        }
    }

    private fun saveDiary(journalDates: Array<String>, journalType: String, diaryDao: ItemDao) {
        val itemDataList = ArrayList<CellPreserveData>()

        for (i in 0 until layout.childCount) {
            val itemData: CellPreserveData
            val view = layout.getChildAt(i)
            itemData = when (view) {
                is EditText -> {
                    val text: String? = view.text.toString()
                    CellPreserveData(text, null)
                }
                is ImageView -> {
                    val imageData: ByteArray? = getImageDataFromImageView(i)
                    CellPreserveData(null, imageData)
                }
                else -> {
                    CellPreserveData(null, null)
                }
            }
            itemDataList.add(itemData)
        }

        val diaryRecord = Item(
            0,
            journalDates[0],
            journalDates[1],
            titles.text.toString(),
            itemDataList,
            journalType
        )
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                diaryDao.insertItem(diaryRecord)
            }
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun getImageDataFromImageView(imageViewIndex: Int): ByteArray? {
        val imageView = layout.getChildAt(imageViewIndex) as? ImageView
        if (imageView != null) {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                return outputStream.toByteArray()
            }
        }
        return null
    }
}
