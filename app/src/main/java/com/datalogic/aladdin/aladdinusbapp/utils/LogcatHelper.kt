/**
 * LogcatHelper - Android Logcat Capture and Management Utility
 * 
 * This utility class provides functionality to capture Android logcat output for the current
 * application process and save it to rotating log files on external storage.
 * 
 * Features:
 * - Captures only logs from the current application process (filtered by PID)
 * - Implements rotating file system with maximum 5 files (SDK_log_1.txt to SDK_log_5.txt)
 * - Each file limited to 2MB, total folder limited to 10MB
 * - Automatically deletes oldest files when storage limit is reached
 * - Saves logs to user-accessible Downloads/AppLogs/logs/ directory
 * - Thread-safe background log capture
 * 
 * Usage:
 * ```
 * val logcatHelper = LogcatHelper.LogcatBuilder()
 *     .setMaxFileSize(2 * 1024 * 1024)  // 2MB per file
 *     .setMaxFolderSize(10 * 1024 * 1024) // 10MB total
 *     .build(context)
 * 
 * logcatHelper.start()  // Begin log capture
 * // ... app runs and logs are captured ...
 * logcatHelper.stop()   // Stop log capture
 * ```
 * 
 * File Structure:
 * Downloads/AppLogs/logs/
 * ├── SDK_log_1.txt (newest, actively written)
 * ├── SDK_log_2.txt
 * ├── SDK_log_3.txt
 * ├── SDK_log_4.txt
 * └── SDK_log_5.txt (oldest)
 * 
 * @author DataLogic Solutions
 * @version 1.0
 */
package com.dzungvu.packlog

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * Sealed class representing the result of an operation
 * Used for better error handling in asynchronous operations
 */
sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

/**
 * LogcatHelper main class for capturing and managing application logs
 * 
 * @param context Android context for accessing external storage
 * @param maxFileSize Maximum size per log file (default: 2MB)
 * @param maxFolderSize Maximum total size of all log files (default: 10MB)
 */

class LogcatHelper private constructor(
    context: Context,
    private val maxFileSize: Long,
    private val maxFolderSize: Long
) {

    /**
     * Builder pattern class for creating LogcatHelper instances
     * Provides fluent API for configuring log capture parameters
     */
    class LogcatBuilder() {
        private var maxFileSize: Long = MAX_FILE_SIZE
        private var maxFolderSize: Long = MAX_FOLDER_SIZE

        /**
         * Set maximum file size for individual log files
         * @param fileSize Maximum size in bytes (default: 2MB)
         * @return Builder instance for chaining
         */
        fun setMaxFileSize(fileSize: Long): LogcatBuilder {
            maxFileSize = fileSize
            return this
        }

        /**
         * Set maximum total folder size for all log files
         * @param folderSize Maximum total size in bytes (default: 10MB)
         * @return Builder instance for chaining
         */
        fun setMaxFolderSize(folderSize: Long): LogcatBuilder {
            maxFolderSize = folderSize
            return this
        }

        /**
         * Build the LogcatHelper instance with configured parameters
         * @param context Android context for file operations
         * @return Configured LogcatHelper instance
         * @throws IllegalStateException if maxFileSize > maxFolderSize
         */
        fun build(context: Context): LogcatHelper {
            if (maxFileSize > maxFolderSize) {
                throw IllegalStateException("maxFileSize must be less than maxFolderSize")
            }
            return LogcatHelper(context, maxFileSize, maxFolderSize)
        }
    }

    // Private properties
    private var logDumper: LogDumper? = null  // Background thread for log capture
    private val pID: Int                      // Current application process ID
    private var publicAppDirectory = ""       // Base directory for app logs
    private var logcatPath = ""              // Directory path for log files
    private var debugMode = false

    companion object {
        private const val MAX_FILE_SIZE = 2097152L     // Default: 2MB per file
        private const val MAX_FOLDER_SIZE = 10485760L  // Default: 10MB total
        private const val MAX_LOG_FILES = 5            // Maximum: 5 rotating files (SDK_log_1 to SDK_log_5)
    }

    init {
        init(context)
        pID = android.os.Process.myPid()  // Get current application process ID
    }

    /**
     * Initialize log directory structure in external storage
     * Creates: Downloads/AppLogs/logs/ directory structure
     * @param context Android context for accessing external storage
     */

    private fun init(context: Context) {
        // Store logs in user-accessible Downloads folder for easy access
//        val externalStorageDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
//        publicAppDirectory = externalStorageDir.absolutePath + File.separator + "AppLogs"
//        logcatPath = publicAppDirectory + File.separator + "logs"

        val internalLogDir = File(context.filesDir, "logs")

        if (!internalLogDir.exists()) {
            internalLogDir.mkdirs()
        }

        logcatPath = internalLogDir.absolutePath
        publicAppDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .absolutePath + File.separator + "AppLogs"

        val appLogDirectory = File(publicAppDirectory)
        val logDirectory = File(logcatPath)
        
        // Create directory structure if it doesn't exist
        if (!appLogDirectory.exists()) {
            appLogDirectory.mkdirs()
        }
        if (!logDirectory.exists()) {
            logDirectory.mkdirs()
        }
    }

    fun exportLogsToPublicFolder(context: Context) : Boolean {
        try {
            val internalDir = File(context.filesDir, "logs")
            if (!internalDir.exists()) {
                Log.e("LogcatHelper", "No internal log directory found: ${internalDir.absolutePath}")
                return false
            }
            val files = internalDir.listFiles()?.filter { it.isFile } ?: emptyList()
            if (files.isEmpty()) {
                Log.w("LogcatHelper", "No log files to export in ${internalDir.absolutePath}")
                return false
            }
            val resolver = context.contentResolver
            var result = false
            for (src in files) {
                val fileName = src.name
                val existingUri = resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Downloads._ID),
                    "${MediaStore.Downloads.DISPLAY_NAME}=?",
                    arrayOf(fileName),
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                        ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                    } else null
                }
                val targetUri = if (existingUri != null) {
                    Log.d("LogcatHelper", "Overwriting existing: $fileName")
                    existingUri
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        put(MediaStore.Downloads.RELATIVE_PATH, "Download/AppLogs/logs/")
                    }
                    resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                }
                if (targetUri != null) {
                    resolver.openOutputStream(targetUri, "wt")?.use { output ->
                        src.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("LogcatHelper", "Exported: $targetUri $fileName")
                    result = true
                } else {
                    Log.e("LogcatHelper", "Failed to create file for: $fileName")
                }
            }
            Log.d("LogcatHelper", "Export completed. Logs are available in /Download/AppLogs/logs")
            return result
        } catch (e: Exception) {
            Log.e("LogcatHelper", "Failed to export logs: ${e.message}")
            e.printStackTrace()
        }
        return false
    }

    /**
     * Start log capture process
     * Creates and starts background thread to capture logcat output
     * Only captures logs from current application process
     */
    fun start() {
        logDumper ?: run {
            logDumper = LogDumper(pID.toString(), logcatPath)
        }
        logDumper?.let { logDumper ->
            if (!logDumper.isAlive) logDumper.start()
        }
    }

    /**
     * Stop log capture process
     * Terminates background thread and cleans up resources
     */
    fun stop() {
        logDumper?.stopLogs()
        logDumper = null
    }

    /**
     * Check if log capture is currently active
     * @return true if logging is active, false otherwise
     */

    fun isActive(): Boolean {
        return logDumper != null && logDumper?.isAlive == true
    }

    /**
     * Inner class that handles the actual log capture in a background thread
     * Implements rotating file system and automatic cleanup
     * 
     * @param pID Process ID to filter logs (only capture logs from this process)
     * @param logcatPath Directory path where log files will be stored
     */

    private inner class LogDumper constructor(
        private val pID: String,
        private val logcatPath: String
    ) : Thread() {
        // Process and stream management
        private var logcatProc: Process? = null           // Logcat process
        private var reader: BufferedReader? = null        // Reader for logcat output
        private var mRunning = true                       // Thread control flag
        private var command = ""                          // Logcat command with PID filter
        private var clearLogCommand = ""                  // Command to clear logcat buffer
        private var outputStream: FileOutputStream? = null // Current log file output stream

        init {
            try {
                // Initialize output stream with first available log file in APPEND mode
                val logFile = getNextLogFile(logcatPath)
                val isExistingFile = logFile.exists() && logFile.length() > 0
                outputStream = FileOutputStream(logFile, true)
                
                if (isExistingFile) {
                    Log.d("LogcatHelper", "Appending to existing log file: ${logFile.name} (${logFile.length()} bytes)")
                } else {
                    Log.d("LogcatHelper", "Creating new log file: ${logFile.name}")
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            // Setup logcat commands
            command = "logcat | grep \"($pID)\""  // Filter logs by process ID
            clearLogCommand = "logcat -c"         // Clear existing logcat buffer
        }

        /**
         * Signal the thread to stop log capture
         */
        internal fun stopLogs() {
            mRunning = false
        }

        /**
         * Main thread execution method
         * Continuously captures logcat output and writes to rotating files
         */

        override fun run() {
            if (outputStream == null) return
            try {
                // Clear existing logcat buffer and start fresh capture
                Runtime.getRuntime().exec(clearLogCommand)
                logcatProc = Runtime.getRuntime().exec(command)
                reader = BufferedReader(InputStreamReader(logcatProc!!.inputStream), 1024)

                while (mRunning) {
                    val line = reader?.readLine() ?: break

                    if (!mRunning) break
                    if (line.isEmpty()) continue

                    // Check if current file has reached maximum size
                    if (outputStream!!.channel.size() >= maxFileSize) {
                        outputStream!!.close()
                        val nextLogFile = getNextLogFile(logcatPath)
                        val isExistingFile = nextLogFile.exists() && nextLogFile.length() > 0
                        outputStream = FileOutputStream(nextLogFile, true)
                        
                        if (isExistingFile) {
                            Log.d("LogcatHelper", "Switching to existing log file: ${nextLogFile.name} (${nextLogFile.length()} bytes)")
                        } else {
                            Log.d("LogcatHelper", "Creating new log file: ${nextLogFile.name}")
                        }
                    }
                    
                    // Check if total folder size exceeds limit
                    if (getFolderSize(logcatPath) >= maxFolderSize) {
                        deleteOldestFile(logcatPath)
                    }

                    val regex = Regex("""\b$pID\s+\d+\s+[IWE]\b""")
                    if (regex.containsMatchIn(line) || debugMode) {
                        outputStream?.write((line + System.lineSeparator()).toByteArray())
                    }
//                    // Write log line to current file
//                    if (line.contains(" I ") || line.contains(" W ") || line.contains(" E ") || debugMode) {
//                        outputStream?.write((line + System.lineSeparator()).toByteArray())
//                        outputStream?.flush()
//                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                // Cleanup resources
                logcatProc?.destroy()
                logcatProc = null

                try {
                    reader?.close()
                    outputStream?.close()
                    reader = null
                    outputStream = null
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Calculate total size of all files in the log directory
         * Uses recursive approach to handle subdirectories
         * @param path Directory path to calculate size for
         * @return Total size in bytes
         */

        private fun getFolderSize(path: String): Long {
            File(path).run {
                var size = 0L
                if (this.isDirectory && this.listFiles() != null) {
                    // Recursively calculate size of all files in directory
                    for (file in this.listFiles()!!) {
                        size += getFolderSize(file.absolutePath)
                    }
                } else {
                    // Add individual file size
                    size = this.length()
                }
                return size
            }
        }

        /**
         * Get the next available log file for writing
         * Implements rotating file system: SDK_log_1.txt to SDK_log_5.txt
         * Always appends to existing files, never overwrites
         * @param dir Directory path where log files are stored
         * @return File object for the next available log file
         */
        private fun getNextLogFile(dir: String): File {
            // Try to find an existing file that's not at max size
            for (i in 1..MAX_LOG_FILES) {
                val logFile = File(dir, "SDK_log_$i.txt")
                
                // If file doesn't exist or is under max size, use it for appending
                if (!logFile.exists() || logFile.length() < maxFileSize) {
                    return logFile
                }
            }
            
            // All files are at max size, rotate by deleting oldest and reusing that slot
            val deletedFileNumber = deleteOldestFile(dir)
            // Return the file that was just deleted (it will be recreated when written to)
            return File(dir, "SDK_log_$deletedFileNumber.txt")
        }

        /**
         * Delete the oldest log file when storage limit is reached
         * Only deletes files matching SDK_log_*.txt pattern
         * @param path Directory path containing log files
         * @return The number of the deleted file (1-5), or 1 if no files found
         */
        private fun deleteOldestFile(path: String): Int {
            val directory = File(path)
            if (directory.isDirectory) {
                // Get all SDK_log files only
                val logFiles = directory.listFiles { file ->
                    file.name.startsWith("SDK_log_") && file.name.endsWith(".txt")
                }
                
                logFiles?.let { files ->
                    if (files.isNotEmpty()) {
                        // Sort by last modified time and delete the oldest
                        val oldestFile = files.minByOrNull { it.lastModified() }
                        oldestFile?.let { fileToDelete ->
                            val fileName = fileToDelete.name
                            fileToDelete.delete()
                            Log.d("LogcatHelper", "Deleted oldest log file: $fileName")
                            
                            // Extract number from filename (SDK_log_X.txt)
                            val numberMatch = Regex("SDK_log_(\\d+)\\.txt").find(fileName)
                            return numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                        }
                    }
                }
            }
            // Default to 1 if no files found or error occurred
            return 1
        }
    }

    fun setDebugMode(debug: Boolean = true) {
        debugMode = debug
    }
}