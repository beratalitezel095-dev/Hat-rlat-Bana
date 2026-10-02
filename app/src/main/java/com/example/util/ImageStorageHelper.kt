package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageStorageHelper {

    /**
     * Creates a new temporary file in the cache directory and returns its content Uri
     * using FileProvider for the camera capture intent.
     */
    fun createCameraImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.cacheDir, "camera_photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, imageFile)
    }

    /**
     * Copies any Uri (from Photo Picker or Camera) to a permanent internal files directory,
     * ensuring long-term persistence and safe reading from background services.
     * Compresses if necessary to keep storage fast and efficient.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val targetDir = File(context.filesDir, "reminders_photos")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val destinationFile = File(targetDir, "img_$timeStamp.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream: InputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    FileOutputStream(destinationFile).use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                    }
                    destinationFile.absolutePath
                } else {
                    // Fallback to direct stream copy
                    context.contentResolver.openInputStream(sourceUri)?.use { fallbackStream ->
                        FileOutputStream(destinationFile).use { out ->
                            fallbackStream.copyTo(out)
                        }
                    }
                    destinationFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Safely loads a bitmap for notifications.
     */
    fun loadBitmapFromPath(path: String, maxDimension: Int = 1024): Bitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            var sampleSize = 1
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
