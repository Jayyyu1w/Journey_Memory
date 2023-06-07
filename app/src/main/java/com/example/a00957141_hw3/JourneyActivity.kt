package com.example.a00957141_hw3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class JourneyActivity : AppCompatActivity() {

    private lateinit var titles: TextView
    private lateinit var dates: TextView
    private lateinit var saveBtn: ImageView
    private lateinit var addBtn: ImageView
    private lateinit var textAdd: ImageView
    private lateinit var imageAdd: ImageView
    private lateinit var voiceAdd: ImageView // merge tana
    private lateinit var cameraAdd: ImageView // merge tana
    private lateinit var layout: LinearLayout
    private lateinit var journalType: String
    private lateinit var journalDates: Array<String>
    private lateinit var database: ItemRoomDatabase
    private lateinit var diaryDao: ItemDao
    private val CAMERA_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_CODE = 1002
    private var currentPhotoPath: String = ""

    // 照相
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "没有相机权限", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoFile = File(currentPhotoPath)
            val photoUri = Uri.fromFile(photoFile)
            insertImageToDiary(photoUri)
        } else {
            Toast.makeText(this, "取消拍照", Toast.LENGTH_SHORT).show()
        }
    }

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
        voiceAdd = findViewById(R.id.voice_add) // merge
        cameraAdd = findViewById(R.id.camera_add) // merge
        layout = findViewById(R.id.JourneyMainLayout)

        journalType = intent.getStringExtra("journalType")!!
        journalDates = intent.getStringArrayExtra("journalDates")!!

        database = ItemRoomDatabase.getDatabase(this)
        diaryDao = database.itemDao()

        // 註冊 ActivityResultLauncher 用於選擇圖片
        var imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { imageUri ->
                    insertImageToDiary(imageUri)
                }
            }

        titles.text = journalType
        dates.text = "${journalDates[0]} ~ ${journalDates[1]}"

        saveBtn.setOnClickListener {
            saveDiary(journalDates, journalType, diaryDao)

        }

        var extVisCnt = 0
        addBtn.setOnClickListener(View.OnClickListener {
            if (extVisCnt == 0) {
                textAdd.visibility = View.VISIBLE
                imageAdd.visibility = View.VISIBLE
                voiceAdd.visibility = View.VISIBLE // merge tana
                cameraAdd.visibility = View.VISIBLE // merge tana
            } else {
                textAdd.visibility = View.GONE
                imageAdd.visibility = View.GONE
                voiceAdd.visibility = View.GONE
                cameraAdd.visibility = View.GONE
            }
            extVisCnt = (extVisCnt + 1) % 2
        })

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
        
        imageAdd.setOnClickListener(View.OnClickListener { // merge tana
            // 啟動圖片選擇器
            imagePickerLauncher.launch("image/*")
        })
        
        voiceAdd.setOnClickListener(View.OnClickListener { // merge tana
            val permission = Manifest.permission.RECORD_AUDIO
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

        cameraAdd.setOnClickListener {
            val cameraPermission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                cameraPermissionLauncher.launch(cameraPermission)
            }
        }

    }

    // 資料庫存取
    private fun saveDiary(journalDates: Array<String>, journalType: String, diaryDao: ItemDao) {
        val itemDataList = ArrayList<CellPreserveData>()

        for (i in 0 until layout.childCount) {
            val itemData: CellPreserveData
            val view = layout.getChildAt(i)
            itemData = when (view) {
                is EditText -> {
                    val text: String? = view.text.toString()
                    CellPreserveData(text, null, null)
                }
                is ImageView -> {
                    val imageData: ByteArray? = getImageDataFromImageView(i)
                    CellPreserveData(null, imageData, null)
                }
                is Button -> {
                    val voiceData: ByteArray? = getVoiceDataFromButton(i)
                    CellPreserveData(null, null, voiceData)
                }
                else -> {
                    CellPreserveData(null, null, null)
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
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun getVoiceDataFromButton(i: Int): ByteArray? {
        val button = layout.getChildAt(i) as? Button
        if (button != null) {
            val recording = button.tag as? Recording
            if (recording != null) {
                val file = File(recording.filePath)
                val fileInputStream = FileInputStream(file)
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length: Int
                while (fileInputStream.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
                return outputStream.toByteArray()
            }
        }
        return null
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

    // 照相功能
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(this, "com.example.a00957141_hw3.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFileName = "JPEG_${timeStamp}_"
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = imageFile.absolutePath
        return imageFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageFile = File(currentPhotoPath)
            val imageUri = Uri.fromFile(imageFile)
            insertImageToDiary(imageUri)
        }
    }

    // 錄音相關功能
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
        recordingButton.text = "開始錄音"
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
            // 錄音失敗
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
                    // 錄音失敗
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
