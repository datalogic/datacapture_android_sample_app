package com.datalogic.aladdin.aladdinusbapp.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.datalogic.aladdin.aladdinusbscannersdk.usbaccess.USBDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.nio.charset.Charset
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.math.log
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import android.net.Uri
import android.os.Build
import java.io.OutputStream

class ImageCaptureModel(usbDeviceManager: USBDeviceManager, context: Context) : ViewModel() {

    private var usbDeviceManager: USBDeviceManager

    // Image state and data
    private var imgData: ByteArray? = null
    private var isCapturing = false
    private var imageResponseCallback: ((Bitmap?) -> Unit)? = null
    private var captureMode = HOST_CMD_NONE
    @SuppressLint("StaticFieldLeak")
    private var context = context
    companion object {
        // Protocol commands
        const val SENSOR_ID = "x0"
        const val IMAGE_FORMAT = "01"
        const val HOST_CMD_NONE = ""
        const val HOST_CMD_CAPTURE = "00"
        const val HOST_CMD_CAPTURE_WITH_PREVIEW = "07"
        const val HOST_CMD_MULTIPLE_CAPTURE_ON_TRIGGER = "11"
        const val HOST_CMD_MULTIPLE_CAPTURE_ON_DECODE = "21"
        const val HOST_CMD_CAPTURE_SEQUENCE = "25"
        const val HOST_CMD_IMG_TX = "30"
        const val HOST_CMD_ABORT_IMAGE_CAPTURE = "40"

        const val IMG_RDY_MSG = "\$i"

        // Image formats
        const val IMGE_JPG = "1"
        const val IMGE_BMP = "0"

        // Image configuration
        const val q = "0"
        const val bbContrastValue = "00" // contrast value
        const val ccBrightnessDirection = "00" // brightness direction
        const val ddContrastDirection = "00" // contrast direction
        const val aaBrightnessValue = "00" // brightness value
    }
    enum class CaptureState {
        WAITING_FOR_IMAGE_READY,  // State for steps 1-3
        RECEIVING_IMAGE,          // State for step 4
        IDLE                      // Default state
    }

    // Add state property
    private var state = CaptureState.IDLE
    private var packetIndex = 0
    private var imgSize = 0

    init {
        this.usbDeviceManager = usbDeviceManager
    }


    fun startCapture(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (mode) {

                HOST_CMD_CAPTURE,
                HOST_CMD_CAPTURE_WITH_PREVIEW,
                HOST_CMD_MULTIPLE_CAPTURE_ON_TRIGGER,
                HOST_CMD_MULTIPLE_CAPTURE_ON_DECODE,
                HOST_CMD_CAPTURE_SEQUENCE -> {

                    captureMode = mode
                    serialUsbComManagement()
                    true
                }

                else -> false
            }

        }
    }

    private fun serialUsbComManagement() {
        try {
            // Step 1: Send the abort capture command.
            sendMsg(HOST_CMD_ABORT_IMAGE_CAPTURE)
            sendMsg(captureMode)

            // Step 2: Wait for the scanner to send the "image ready" message.
            val response = usbDeviceManager.readRawCommand()
            if (!response.contains(IMG_RDY_MSG)) {
                // Handle the case where the ready message isn't received.
                // For example, you might log a message or return early.
                return
            }

            // Step 3: The "image ready" message is received.
            // Parse the image size and prepare the image data buffer.
            imgSize = parseImageSize(response)
            imgData = ByteArray(imgSize)
            packetIndex = 0

            // Step 4: Send the capture command.
            sendMsg(captureMode)

            // Step 5: Request image transmission.
            sendMsg(HOST_CMD_IMG_TX)

            // Step 6: Receive image data from the scanner.
            val bytesReceived = usbDeviceManager.receiveImage()

            // Step 7: Process the received bytes.
            for (byte in bytesReceived) {
                if (packetIndex < imgSize) {
                    imgData!![packetIndex++] = byte
                    // Check if we've received the complete image.
                    if (packetIndex >= imgSize) {
                        handleImage(imgData!!)
                        break  // Exit the loop once the full image is processed.
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            resetState()
        }
    }

    //
    fun captureOnTrigger() {
        // Implement your on trigger capture logic here.
        println("On Trigger capture triggered")
    }



    // Parse image size from response
    private fun parseImageSize(response: String): Int {
        // Extract size bytes (positions 4-11) and convert from hex
        val sizeBytes = response.substring(4, 12)
        return Integer.decode("0x$sizeBytes")
    }

    private fun sendCommand(cmd: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Convert command string to byte array
                val bytesCommand = cmd.toByteArray()

                // Send command to the device
                usbDeviceManager.writeRawCommand(bytesCommand)
            } catch (e: Exception) {
                // Handle any exceptions that occur during command sending
                e.printStackTrace()
            }
        }
    }

    private fun convertBytesToImage(byteArray: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun sendMsg(captureMode: String) {
        usbDeviceManager.writeRawCommand(formatMsg(captureMode).toByteArray())

    }


    private fun formatMsg(captureMode: String): String {
        val msg = SENSOR_ID + captureMode + q + aaBrightnessValue + bbContrastValue + ccBrightnessDirection + ddContrastDirection + "\r"
        return msg
    }


    fun handleImage(imageData: ByteArray){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Decode image
                val imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

                // Save to gallery and get URI
                val imageUri = saveBitmapToGallery(imageBitmap)

                // Notify UI with bitmap and URI on main thread
                withContext(Dispatchers.Main) {
                    imageResponseCallback?.invoke(imageBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun resetState() {
        state = CaptureState.IDLE
        packetIndex = 0
        imgData = null
        isCapturing = false
    }

    fun saveBitmapToGallery( bitmap: Bitmap): Uri? {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val contentResolver = context.contentResolver
        val imageUri: Uri? = contentResolver.insert(imageCollection, contentValues)
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }
        return imageUri
    }

    fun setImageCallback(callback: ((Bitmap?) -> Unit)?) {
        imageResponseCallback = callback
    }
}

