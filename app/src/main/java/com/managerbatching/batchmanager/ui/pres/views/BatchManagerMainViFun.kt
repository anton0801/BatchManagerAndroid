package com.managerbatching.batchmanager.ui.pres.views

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatchManagerMainViFun(private val context: Context) {
    fun eggLabelSavePhoto() : Uri {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val df = sdf.format(Date())
        val dir = context.filesDir.absoluteFile
        if (!dir.exists()) {
            dir.mkdir()
        }
        return FileProvider.getUriForFile(
            context,
            "com.managerbatching.batchmanager.filesssss",
            File(dir, "/$df.jpg")
        )
    }

}