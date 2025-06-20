package com.datalogic.aladdin.aladdinusbapp.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import com.datalogic.aladdin.aladdinusbapp.R

object FileUtils {
    fun saveTextToDownloads(context: Context, fileName: String, text: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val fileUri = resolver.insert(collection, contentValues)

        fileUri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(text.toByteArray())
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)

            Toast.makeText(context, context.getString(R.string.file_saved), Toast.LENGTH_SHORT).show()
        }
    }
}