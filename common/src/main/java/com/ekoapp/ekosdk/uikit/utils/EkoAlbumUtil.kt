package com.ekoapp.ekosdk.uikit.utils

import android.Manifest
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.uikit.R
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine

object EkoAlbumUtil {

    private const val MAX_SELECTION_COUNT = 20

    fun pickMultipleImage(activity: AppCompatActivity, currentCount: Int, resultCode: Int) {
        val pickImagePermission =
                activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        if (currentCount == MAX_SELECTION_COUNT) {
                            Toast.makeText(
                                    activity,
                                    activity.getString(R.string.amity_max_image_selected),
                                    Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Matisse.from(activity)
                                    .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))
                                    .countable(true)
                                    .maxSelectable(MAX_SELECTION_COUNT - currentCount)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    .imageEngine(GlideEngine())
                                    .theme(R.style.ImagePickerTheme)
                                    .forResult(resultCode)
                        }
                    } else {
                        Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
        pickImagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun pickMultipleImage(fragment: Fragment, currentCount: Int, resultCode: Int) {
        val pickImagePermission =
                fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    if (it) {
                        if (currentCount == MAX_SELECTION_COUNT) {
                            Toast.makeText(
                                    fragment.requireContext(),
                                    fragment.getString(R.string.amity_max_image_selected),
                                    Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Matisse.from(fragment)
                                    .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))
                                    .countable(true)
                                    .maxSelectable(MAX_SELECTION_COUNT - currentCount)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    .imageEngine(GlideEngine())
                                    .theme(R.style.ImagePickerTheme)
                                    .forResult(resultCode)
                        }
                    } else {
                        Toast.makeText(
                                fragment.requireContext(),
                                "Permission denied",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        pickImagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}