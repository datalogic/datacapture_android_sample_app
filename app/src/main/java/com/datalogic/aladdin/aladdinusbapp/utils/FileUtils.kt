package com.datalogic.aladdin.aladdinusbapp.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import com.datalogic.aladdin.aladdinusbapp.R
import java.io.File
import java.io.FileOutputStream

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

    fun getFileFromUri(context: Context, uri: Uri): File? {
        val fileName = getFileNameFromUri(context, uri) ?: return null
        val tempFile = File(context.cacheDir, fileName)

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }

        if (fileName == null) {
            fileName = uri.path?.substringAfterLast('/')
        }

        return fileName
    }

    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    fun getDisplayPath(context: Context, uri: Uri): String? {
        val cr = context.contentResolver

        // 1) Try RELATIVE_PATH (Q+) so we get "Download/sub/folder/"
        val projection = buildList {
            add(OpenableColumns.DISPLAY_NAME)
            add(MediaStore.MediaColumns.RELATIVE_PATH)
        }.toTypedArray()

        var name: String? = null
        var rel: String? = null

        cr.query(uri, projection, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                val idx = c.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                if (idx != -1) rel = c.getString(idx)
            }
        }

        if (!rel.isNullOrBlank() && !name.isNullOrBlank()) {
            return rel!!.trimEnd('/') + "/" + name
        }

        // 2) Parse DocumentsContract authorities to keep subfolders
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            when (uri.authority) {
                // e.g. "primary:Download/sub/folder/filename.ext"
                "com.android.externalstorage.documents" -> {
                    val afterColon = docId.substringAfter(':', "")
                    if (afterColon.isNotEmpty()) return afterColon
                }
                // e.g. "raw:/storage/emulated/0/Download/sub/folder/filename.ext"
                "com.android.providers.downloads.documents" -> {
                    if (docId.startsWith("raw:")) {
                        val raw = docId.removePrefix("raw:")
                        val root = Environment.getExternalStorageDirectory().absolutePath
                        return raw.removePrefix(root).trimStart('/', '\\')
                    }
                    // Pre-Q only: resolve numeric IDs to a filesystem path
                    // Fallback: we at least show "Download/filename"
                    if (name != null) return "Download/$name"
                }
                // Media provider (older APIs won’t expose RELATIVE_PATH)
                "com.android.providers.media.documents" -> {
                    val type = docId.substringBefore(':')
                    val base = when (type) {
                        "image" -> "Pictures"
                        "video" -> "Movies"
                        "audio" -> "Music"
                        else -> "Media"
                    }
                    if (name != null) return "$base/$name"
                }
            }
        }

        // 3) file:// legacy URIs
        if (uri.scheme == "file") {
            val path = uri.path ?: return null
            val root = Environment.getExternalStorageDirectory().absolutePath
            return path.removePrefix(root).trimStart('/', '\\')
        }

        // 4) Last resort: just the file name
        return name
    }

}