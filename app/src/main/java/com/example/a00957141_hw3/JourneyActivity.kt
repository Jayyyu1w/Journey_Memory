package com.example.a00957141_hw3

import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.*


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
        val voiceAdd: ImageView = findViewById(R.id.voice_add)
        var extVisCnt: Int = 0

        // 註冊 ActivityResultLauncher 用於選擇圖片
        var imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { imageUri ->
                    insertImageToDiary(imageUri)
                }
            }

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
            val voiceAdd: ImageView = findViewById(R.id.voice_add)
            if(extVisCnt == 0) {
                textAdd.visibility = View.VISIBLE
                imageAdd.visibility = View.VISIBLE
                voiceAdd.visibility = View.VISIBLE
            }
            else {
                textAdd.visibility = View.GONE
                imageAdd.visibility = View.GONE
                voiceAdd.visibility = View.GONE
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
            // 啟動圖片選擇器
            imagePickerLauncher.launch("image/*")
        })
        voiceAdd.setOnClickListener(View.OnClickListener {
            val permission = android.Manifest.permission.RECORD_AUDIO
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                addRecordingButton()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
            }
        })
    }


    private fun playRecording(recording: Recording) {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(recording.filePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

            mediaPlayer.setOnCompletionListener {
                recording.button.text = "播放录音"
            }
        } catch (e: IOException) {
            // 处理播放录音异常
            // 例如显示错误信息、记录日志等
            print(e)
        } catch (e: IllegalStateException) {
            // 处理播放录音异常
            // 例如显示错误信息、记录日志等
            print(e)
        }
    }

    private fun toggleRecordingState(recording: Recording) {
        if (!recording.isRecording && !recording.isPaused) {
            startRecording(recording)
        } else if (recording.isRecording && !recording.isPaused) {
            pauseRecording(recording)
        } else {
            playRecording(recording)
        }
    }

    private fun addRecordingButton() {
        val layout = findViewById<LinearLayout>(R.id.JourneyMainLayout)
        val recordingButton = Button(this)
        recordingButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        recordingButton.text = "开始录音"
        layout.addView(recordingButton)

        val recording = Recording(recordingButton, "", false, false, null)
        recordingButton.setOnClickListener {
            toggleRecordingState(recording)
        }

        recordingsList.add(recording)
    }

    private fun startRecording(recording: Recording) {
        val mediaRecorder = MediaRecorder()
        val outputFile = createRecordingFile()

        recording.filePath = outputFile.absolutePath

        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.setOutputFile(outputFile.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()

            recording.mediaRecorder = mediaRecorder
            recording.isRecording = true
            recording.isPaused = false

            recording.button.text = "暫停錄音"
        } catch (e: IOException) {
            // 处理录音异常
        }
    }

    private fun pauseRecording(recording: Recording) {
        if (recording.isRecording) {
            recording.mediaRecorder?.apply {
                try {
                    recording.mediaRecorder!!.stop()
                    recording.isPaused = true
                    recording.button.text = "播放錄音"
                } catch (e: IllegalStateException) {
                    // 处理暂停录音异常
                }
            }
        }
    }

    data class Recording(
        val button: Button,
        var filePath: String = "",
        var isRecording: Boolean = false,
        var isPaused: Boolean = false,
        var mediaRecorder: MediaRecorder? = null
    )

    private fun createRecordingFile(): File {
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "recording_$timestamp.3gp"
        return File(outputDir, fileName)
    }


    private val recordingsList: MutableList<Recording> = mutableListOf()


    // 將選擇的圖片插入到日記中
    private fun insertImageToDiary(imageUri: Uri) {
        val layout = findViewById<LinearLayout>(R.id.JourneyMainLayout)
        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        imageView.setImageURI(imageUri)
        layout.addView(imageView)
    }
}