package com.ekoapp.ekosdk.uikit.base

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.ViewDataBinding
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.contract.EkoPickFileContract
import com.ekoapp.ekosdk.uikit.contract.EkoPickImageContract
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

abstract class EkoPickerActivity<T : ViewDataBinding, V : EkoBaseViewModel>
    : EkoBaseActivity<T, V>() {
    private var photoUri: Uri? = null

    abstract fun onImagePicked(data: Uri?)
    abstract fun onFilePicked(data: Uri?)
    abstract fun onPhotoClicked(photoUri: Uri?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun pickImage() {
        val pickImagePermission =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        val pickImage = registerForActivityResult(EkoPickImageContract()) { data ->
                            onImagePicked(data)
                        }
                        pickImage.launch(getString(R.string.amity_choose_image))
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
        pickImagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    fun pickFile() {
        val pickFilePermission =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        val pickFile = registerForActivityResult(EkoPickFileContract()) { data ->
                            onFilePicked(data)
                        }
                        pickFile.launch("")
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }

        //pickFilePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun takePicture() {

        val cameraPermission =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    var permissionGranted = false
                    permissions.entries.forEach {
                        permissionGranted = it.value
                    }
                    if (permissionGranted) {
                        val takePhoto =
                                registerForActivityResult(ActivityResultContracts.TakePicture()) {
                                    if (it) {
                                        onPhotoClicked(photoUri)
                                    }
                                }
                        createPhotoUri()
                        takePhoto.launch(photoUri)
                    }
                }

        cameraPermission.launch(
                arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        )
    }

    private fun createPhotoUri() {

        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e("EkoPickerFragment", " Exception ${ex.localizedMessage}")
            null
        }

        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                    this,
                    packageName,
                    it
            )
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
    }
}