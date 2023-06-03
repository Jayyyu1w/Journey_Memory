package com.example.a00957141_hw3

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class MyAlertDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // 建立一個 AlertDialog.Builder 物件
            val builder = AlertDialog.Builder(it)
            builder.apply {
                // 設定 AlertDialog 的標題、訊息、按鈕等等
                setTitle("功能尚未完成")
                setMessage("儲存功能尚未完成，請見諒\uD83D\uDE4F")
                setPositiveButton("確認") { dialog, id ->
                }
            }
            // 建立 AlertDialog 物件並回傳
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class NoticeDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // 建立一個 AlertDialog.Builder 物件
            val builder = AlertDialog.Builder(it)
            builder.apply {
                // 設定 AlertDialog 的標題、訊息、按鈕等等
                setTitle("功能尚未完成")
                setMessage("使用者自選圖片尚未完成，請見諒\uD83D\uDE4F")
                setPositiveButton("確認") { dialog, id ->
                }
            }
            // 建立 AlertDialog 物件並回傳
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}