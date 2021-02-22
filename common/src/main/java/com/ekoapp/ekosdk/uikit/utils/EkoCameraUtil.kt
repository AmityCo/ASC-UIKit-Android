package com.ekoapp.ekosdk.uikit.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EkoCameraUtil {
    companion object {
        fun createPhotoUri(context: Context, file: File): Uri? {

            return FileProvider.getUriForFile(
                    context,
                    context.packageName,
                    file
            )

        }

        fun createImageFile(context: Context): File? {

            return try {
                val timeStamp: String =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                return File.createTempFile(
                        "JPEG_${timeStamp}_", /* prefix */
                        ".jpg", /* suffix */
                        storageDir /* directory */
                )
            } catch (ex: IOException) {
                Log.e("EkoPickerFragment", " Exception ${ex.localizedMessage}")
                null
            }

        }
    }
}