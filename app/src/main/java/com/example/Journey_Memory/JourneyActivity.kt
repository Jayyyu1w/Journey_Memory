package com.example.Journey_Memory

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.Journey_Memory.data.CellPreserveData
import com.example.Journey_Memory.data.Item
import com.example.Journey_Memory.data.ItemDao
import com.example.Journey_Memory.data.ItemRoomDatabase
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*


class JourneyActivity : AppCompatActivity() {

    private lateinit var titles: TextView
    private lateinit var dates: TextView
    private lateinit var saveBtn: ImageView
    private lateinit var addBtn: ImageView
    private lateinit var textAdd: ImageView
    private lateinit var imageAdd: ImageView
    private lateinit var voiceAdd: ImageView
    private lateinit var cameraAdd: ImageView
    private lateinit var locationAdd: ImageView
    private lateinit var layout: LinearLayout
    private lateinit var journalType: String
    private lateinit var journalDates: Array<String>
    private lateinit var journalID: String
    private lateinit var database: ItemRoomDatabase
    private lateinit var diaryDao: ItemDao
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0
    private val CAMERA_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_CODE = 1002
    private val LOCATION_PERMISSION_REQUEST_CODE = 1003
    private val MAP_REQUEST_CODE = 1004
    private val RECORD_AUDIO_PERMISSION_CODE = 1005
    private val AUTOCOMPLETE_REQUEST_CODE = 1006
    private val LATITUDE_LOWER_BOUND = 21.901
    private val LONGITUDE_LOWER_BOUND = 119.132
    private val LATITUDE_UPPER_BOUND = 25.392
    private val LONGITUDE_UPPER_BOUND = 122.000
    private var currentPhotoPath: String = ""
    private var FILE_PROVIDER_AUTHORITY = "com.example.Journey_Memory.fileprovider"
    private lateinit var placesClient: PlacesClient

    // 照相
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "沒有相機權限", Toast.LENGTH_SHORT).show()
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

    // 位置
    private val locationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedLocation = result.data?.getStringExtra("selected_location")
            selectedLocation?.let {
                createLocationButton(it)
            }
        }
    }

    companion object {
        const val SAVE_SUCCESS_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide() // 隱藏標題欄
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey)

        titles = findViewById(R.id.journeyTp)
        dates = findViewById(R.id.journeyDt)
        saveBtn = findViewById(R.id.checkmark_icon)
        addBtn = findViewById(R.id.add_icon)
        textAdd = findViewById(R.id.text_add)
        imageAdd = findViewById(R.id.image_add)
        voiceAdd = findViewById(R.id.voice_add)
        cameraAdd = findViewById(R.id.camera_add)
        locationAdd = findViewById(R.id.location_add)
        layout = findViewById(R.id.JourneyMainLayout)
        journalType = intent.getStringExtra("journalType")!!
        journalDates = intent.getStringArrayExtra("journalDates")!!
        journalID = intent.getStringExtra("journalID")!!
        database = ItemRoomDatabase.getDatabase(this)
        diaryDao = database.itemDao()

        // 取得開場動畫
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.anim)
        // 應用開場動畫到視圖
        layout.startAnimation(fadeInAnimation)

        // 初始化 Places SDK
        val apiKey = getString(R.string.places_api_key)
        Places.initialize(applicationContext, apiKey)
        placesClient = Places.createClient(this)

        // 初始化音效
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        soundId = soundPool.load(this, R.raw.click_1, 1)

        // 註冊 ActivityResultLauncher 用於選擇圖片
        var imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                val imagePath = getImagePathFromUri(imageUri)
                if (imagePath != null) {
                    insertImageToDiaryByPath(imagePath)
                } else {
                    Toast.makeText(this, "無法讀取圖片路徑", Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (journalID != "null") {
            val itemLiveData: LiveData<Item> = diaryDao.getItemById(journalID.toInt())
            itemLiveData.observe(this, androidx.lifecycle.Observer { item ->
                if (item != null) {
                    // 在這裡獲取最新的數據並進行相應的處理
                    Log.d("MemoryActivity", "Item with id: $item")
                    // 可以將item的信息顯示在界面上或進行其他操作
                    val contentList = item.content
                    for (cellPreserveData in contentList) {
                        val text = cellPreserveData.text
                        val imageData = cellPreserveData.imageData
                        val voiceData = cellPreserveData.voiceDate
                        Log.d("MemoryActivity", "Text: $text, Image Data: $imageData, Voice Data: $voiceData")
                        if(text!=null){
                            val editText = EditText(this)
                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            )
                            editText.layoutParams = layoutParams
                            val editableText = Editable.Factory.getInstance().newEditable(text)
                            editText.text = editableText
                            editText.setOnLongClickListener {
                                showConfirmationDialog("確認刪除", "您確定要刪除該文字嗎？") {
                                    layout.removeView(editText)
                                }
                                true
                            }
                            layout.addView(editText)
                        }
                        if(imageData!=null){
                            val path=imageData.toString()
                            insertImageToDiaryByPath(path)
                        }
                        if(voiceData!=null){

                        }
                    }
                }
            })
        }
        titles.text = journalType
        dates.text = "${journalDates[0]} ~ ${journalDates[1]}"

        saveBtn.setOnClickListener {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) // 音效
            saveDiary(journalDates, journalType, diaryDao)
        }

        var extVisCnt = 0
        addBtn.setOnClickListener(object : View.OnClickListener {

            private var isExpanded = false
            override fun onClick(v: View) {
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) // 音效
                if (!isExpanded) {
                    expandButtons()
                } else {
                    collapseButtons()
                }
                isExpanded = !isExpanded
            }

            private fun expandButtons() {
                // 顯示更多按鈕並添加動畫效果
                textAdd.visibility = View.VISIBLE
                imageAdd.visibility = View.VISIBLE
                voiceAdd.visibility = View.VISIBLE
                cameraAdd.visibility = View.VISIBLE
                locationAdd.visibility = View.VISIBLE

                // 執行動畫效果
                animateVisibility(textAdd, View.VISIBLE)
                animateVisibility(imageAdd, View.VISIBLE)
                animateVisibility(voiceAdd, View.VISIBLE)
                animateVisibility(cameraAdd, View.VISIBLE)
                animateVisibility(locationAdd, View.VISIBLE)
            }

            private fun collapseButtons() {
                // 隱藏更多按鈕並添加動畫效果
                animateVisibility(textAdd, View.GONE)
                animateVisibility(imageAdd, View.GONE)
                animateVisibility(voiceAdd, View.GONE)
                animateVisibility(cameraAdd, View.GONE)
                animateVisibility(locationAdd, View.GONE)
            }

            private fun animateVisibility(view: View, visibility: Int) {
                // 創建 alpha 動畫
                val alphaAnimator = ObjectAnimator.ofFloat(
                    view,
                    "alpha",
                    if (visibility == View.VISIBLE) 0f else 1f,
                    if (visibility == View.VISIBLE) 1f else 0f
                )
                alphaAnimator.duration = 300 // 設置動畫持續時間為 300 毫秒

                // 創建 scale 動畫
                val scaleAnimatorX = ObjectAnimator.ofFloat(
                    view,
                    "scaleX",
                    if (visibility == View.VISIBLE) 0f else 1f,
                    if (visibility == View.VISIBLE) 1f else 0f
                )
                val scaleAnimatorY = ObjectAnimator.ofFloat(
                    view,
                    "scaleY",
                    if (visibility == View.VISIBLE) 0f else 1f,
                    if (visibility == View.VISIBLE) 1f else 0f
                )
                scaleAnimatorX.duration = 300 // 設置動畫持續時間為 300 毫秒
                scaleAnimatorY.duration = 300 // 設置動畫持續時間為 300 毫秒

                // 創建動畫集合
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(alphaAnimator, scaleAnimatorX, scaleAnimatorY)

                // 設置動畫結束後的可見性
                animatorSet.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = visibility
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })

                // 開始動畫
                animatorSet.start()
            }
        })

        textAdd.setOnClickListener {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) // 音效
            val editText = EditText(this)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            editText.layoutParams = layoutParams
            editText.hint = "請在這裡輸入文字"
            editText.setOnLongClickListener {
                showConfirmationDialog("確認刪除", "您確定要刪除該文字嗎？") {
                    layout.removeView(editText)
                }
                true
            }
            layout.addView(editText)
        }
        
        imageAdd.setOnClickListener(View.OnClickListener { // merge tana
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) // 音效
            // 啟動圖片選擇器
            imagePickerLauncher.launch("image/*")
        })
        
        voiceAdd.setOnClickListener(View.OnClickListener { // merge tana
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) // 音效
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
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) // 音效
            val cameraPermission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                cameraPermissionLauncher.launch(cameraPermission)
            }
        }

        locationAdd.setOnClickListener {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f) // 音效
            val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED) {
                showLocationPopup()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(locationPermission), LOCATION_PERMISSION_REQUEST_CODE)
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
                    val imageData: String? = view.tag.toString()
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
        if(journalID!="null"){
            val diaryRecord = Item(
                journalID.toLong(), // 使用journalID来更新特定的項目
                journalDates[0],
                journalDates[1],
                titles.text.toString(),
                itemDataList,
                journalType
            )
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    diaryDao.updateItem(diaryRecord) // 使用updateItem方法進行更新
                }
                setResult(RESULT_OK)
                finish()
            }
        }else{
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
            val photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(this, "無法創建圖片", Toast.LENGTH_SHORT).show()
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
        // 相機相關功能
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageFile = File(currentPhotoPath)
            val imageUri = Uri.fromFile(imageFile)
            //insertImageToDiary(imageUri)
            insertImageToDiaryByPath(currentPhotoPath)
        }
        // 位置相關功能
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            val location = place.name
            createLocationButton(location)
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
                recording.button.text = "播放錄音"
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
        recordingButton.setOnLongClickListener {
            showConfirmationDialog("確認刪除", "您確定要刪除該 Button 嗎？") {
                layout.removeView(recordingButton)
            }
            true
        }
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

    private fun showLocationPopup() {
        val popupMenu = PopupMenu(this, locationAdd)
        popupMenu.menuInflater.inflate(R.menu.location_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.auto_location -> {
                    // 自動定位
                    val currentLocation = getUserLocation() // 获取用户位置信息
                    createLocationButton(currentLocation)
                    true
                }
                R.id.manual_location -> {
                    // 手動定位
                    openPlacePicker()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun createLocationButton(location: String) {
        val layout = findViewById<LinearLayout>(R.id.JourneyMainLayout)
        val locationButton = Button(this)
        locationButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        locationButton.text = location
        locationButton.setOnLongClickListener {
            showConfirmationDialog("確認刪除", "您確定要刪除該 Button 嗎？") {
                layout.removeView(locationButton)
            }
            true
        }
        layout.addView(locationButton)

        // 顯示地圖
        locationButton.setOnClickListener {
            if (location == "無法獲取當前位置") {
                Toast.makeText(this, "無法獲取當前位置", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openMapForLocation(location)
        }
    }

    private fun openMapForLocation(location: String) {
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location"))
        startActivity(mapIntent)
    }

    private fun openPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun getUserLocation(): String {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider = LocationManager.GPS_PROVIDER
            val location = locationManager.getLastKnownLocation(locationProvider)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                return "($latitude, $longitude)"
            }
        }
        return "無法獲取當前位置"
    }

    private fun insertImageToDiaryByPath(imagePath: String) {
        val layout = findViewById<LinearLayout>(R.id.JourneyMainLayout)
        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        imageView.tag = imagePath

        // 通过文件路徑創建 Bitmap 对象
        val bitmap = BitmapFactory.decodeFile(imagePath)
        imageView.setImageBitmap(bitmap)
        imageView.setOnLongClickListener {
            showConfirmationDialog("確認刪除", "您確定要刪除該圖片嗎？") {
                layout.removeView(imageView)
            }
            true
        }
        layout.addView(imageView)
    }
    // 取得圖片在相簿的路徑
    private fun getImagePathFromUri(uri: Uri): String? {
        var imagePath: String? = null
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val fileName = it.getString(columnIndex)
                val filePath = File(filesDir, fileName)
                imagePath = filePath.absolutePath
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    val outputStream = FileOutputStream(filePath)
                    outputStream.use { output ->
                        val buffer = ByteArray(4 * 1024) // 4KB buffer
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                        output.flush()
                    }
                }
            }
        }
        return imagePath
    }
    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("確定") { dialog, _ ->
            dialog.dismiss()
            onConfirm.invoke()
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}
