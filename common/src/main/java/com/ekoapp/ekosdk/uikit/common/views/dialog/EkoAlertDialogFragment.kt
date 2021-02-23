package com.ekoapp.ekosdk.uikit.common.views.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.ekoapp.ekosdk.uikit.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class EkoAlertDialogFragment : DialogFragment() {
    var title: Int = -1
    var message: Int = -1
    private var positiveButtonTitle: Int = -1
    private var negativeButtonTitle: Int = -1
    var listener: IAlertDialogActionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = requireArguments().getInt(EXTRA_PARAM_TITLE)!!
            message = requireArguments().getInt(EXTRA_PARAM_MESSAGE)!!
            positiveButtonTitle = requireArguments().getInt(EXTRA_PARAM_POSITIVE_BUTTON_TITLE, -1)
            negativeButtonTitle = requireArguments().getInt(EXTRA_PARAM_NEGATIVE_BUTTON_TITLE, -1)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(title))
                .setMessage(resources.getString(message))
        if (positiveButtonTitle != -1) {
            dialog.setPositiveButton(resources.getString(positiveButtonTitle!!)) { dialog, which ->
                listener?.onClickPositiveButton()
                dialog.dismiss()
            }
        }
        if (negativeButtonTitle != -1) {
            dialog.setNegativeButton(resources.getString(negativeButtonTitle!!)) { dialog, which ->
                listener?.onClickNegativeButton()
                dialog.dismiss()
            }
        }
        val alertDialog = dialog.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.amityColorPrimary))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.amityColorPrimary))
        }
        return alertDialog
    }

    fun setAlertDialogActionListener(listener: IAlertDialogActionListener) {
        this.listener = listener
    }


    companion object {
        const val EXTRA_PARAM_TITLE = "title"
        const val EXTRA_PARAM_MESSAGE = "message"
        const val EXTRA_PARAM_POSITIVE_BUTTON_TITLE = "positive"
        const val EXTRA_PARAM_NEGATIVE_BUTTON_TITLE = "negative"
        val TAG: String = EkoAlertDialogFragment::class.java.simpleName
        fun newInstance(
                @StringRes title: Int,
                @StringRes message: Int,
                @StringRes positiveButtonTitle: Int?,
                @StringRes negativeButtonTitle: Int?
        ): EkoAlertDialogFragment {
            val args = Bundle()
            args.putInt(EXTRA_PARAM_TITLE, title)
            args.putInt(EXTRA_PARAM_MESSAGE, message)
            positiveButtonTitle?.let { args.putInt(EXTRA_PARAM_POSITIVE_BUTTON_TITLE, it) }
            negativeButtonTitle?.let { args.putInt(EXTRA_PARAM_NEGATIVE_BUTTON_TITLE, it) }
            val fragment = EkoAlertDialogFragment()
            fragment.arguments = args
            return fragment
        }


    }

    interface IAlertDialogActionListener {
        fun onClickPositiveButton()
        fun onClickNegativeButton()
    }
}