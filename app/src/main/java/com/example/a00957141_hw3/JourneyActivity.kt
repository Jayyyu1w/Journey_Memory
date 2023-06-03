package com.example.a00957141_hw3

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu


class JourneyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey)

        val journalType = intent.getStringExtra("journalType")
        val journalDates = intent.getStringArrayExtra("journalDates")
        val titles: TextView = findViewById(R.id.journeyTp)
        val dates: TextView = findViewById(R.id.journeyDt)
        val saveBtn: ImageView = findViewById(R.id.checkmark_icon)
        val addBtn: ImageView = findViewById(R.id.add_icon)
        val textAdd: ImageView = findViewById(R.id.text_add)
        val imageAdd: ImageView = findViewById(R.id.image_add)
        var extVisCnt: Int = 0

        titles.text = journalType
        dates.text = journalDates!![0] + " ~ " + journalDates[1]
        saveBtn.setOnClickListener{
            // 觸發點擊事件時，顯示 Alert Dialog Fragment
            val alertDialogFragment = MyAlertDialogFragment()
            alertDialogFragment.show(supportFragmentManager, "dialog")
        }
        addBtn.setOnClickListener(View.OnClickListener {
            val popup = PopupMenu(this@JourneyActivity, addBtn)
            val textAdd: ImageView = findViewById(R.id.text_add)
            val imageAdd: ImageView = findViewById(R.id.image_add)
            if(extVisCnt == 0) {
                textAdd.visibility = View.VISIBLE
                imageAdd.visibility = View.VISIBLE
            }
            else {
                textAdd.visibility = View.GONE
                imageAdd.visibility = View.GONE
            }
            ((extVisCnt + 1) % 2).also { extVisCnt = it }
        })
        textAdd.setOnClickListener(View.OnClickListener {
            val layout = findViewById<LinearLayout>(R.id.JourneyMainLayout)
            val editText = EditText(this)
            editText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            editText.hint = "請在這裡輸入文字"
            layout.addView(editText)
        })
        imageAdd.setOnClickListener(View.OnClickListener {
            val layout = findViewById<LinearLayout>(R.id.JourneyMainLayout)
            val imageView = ImageView(this)
            val alertDialogFragment = NoticeDialogFragment()
            imageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            imageView.setImageResource(R.drawable.baseline_rocket_24)
            alertDialogFragment.show(supportFragmentManager, "dialog")
            layout.addView(imageView)
        })
    }
}