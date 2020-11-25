package com.ekoapp.ekosdk.uikit.base

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

open class EkoBaseFragment : Fragment() {
    protected var disposable: CompositeDisposable = CompositeDisposable()
    private var consumeBackPress = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    private fun listenBackPress() {
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (consumeBackPress) {
                    handleBackPress()
                } else {
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
    }

    open fun handleBackPress() {
        backPressFragment()
    }

    fun backPressFragment() {
        consumeBackPress = false
        activity?.onBackPressed()
    }

    override fun onDestroyView() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        super.onDestroyView()
    }

    fun hasPermission(permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context?.let {
                val status = ContextCompat.checkSelfPermission(
                    it,
                    permission
                )
                return (status == PackageManager.PERMISSION_GRANTED)
            }
        }
        return true
    }

    fun requestPermission(permission: String, requestCode: Int) {
        (activity as? Activity)?.let {
            ActivityCompat.requestPermissions(
                it, arrayOf(
                    permission
                ), requestCode
            )
        }
    }

    fun requestPermission(permission: Array<String>, requestCode: Int) {
        (activity as? Activity)?.let {
            ActivityCompat.requestPermissions(
                it,
                permission, requestCode
            )
        }

    }
}