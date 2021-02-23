package com.ekoapp.ekosdk.uikit.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.databinding.AmityViewReadMoreBinding

class EkoReadMoreTextView : ConstraintLayout {

    private lateinit var mBinding: AmityViewReadMoreBinding
    private var isSender = true
    private var message: String? = ""
    private var mListener: ILongPressListener? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBinding = DataBindingUtil.inflate(inflater, R.layout.amity_view_read_more, this, true)
        mBinding.isSender = isSender
        mBinding.readMoreContainer.setOnClickListener {
            mBinding.tvMessage.maxLines = Int.MAX_VALUE
            mBinding.tvReadMore.visibility = View.GONE
            mBinding.tvMessage.text = message
        }

        mBinding.readMoreContainer.setOnLongClickListener {
            mListener?.onLongPress()
            true
        }
    }

    fun setReadMoreColor(color: Int) {
        mBinding.tvReadMore.setTextColor(color)
    }

    fun setText(text: String?) {
        message = text
        mBinding.tvMessage.text = text
        setReadMoreVisibility()
    }

    fun isSender(value: Boolean) {
        mBinding.isSender = value
    }

    fun setReadMoreListener(listener: ILongPressListener) {
        mListener = listener
    }

    fun setMaxLines(value: Int) {
        mBinding.tvMessage.maxLines = value
    }

    private fun setReadMoreVisibility() {
        val vto = mBinding.tvMessage.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val obs = mBinding.tvMessage.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)

                if (mBinding.tvMessage.lineCount > mBinding.tvMessage.maxLines) {
                    mBinding.tvReadMore.visibility = View.VISIBLE
                } else {
                    mBinding.tvReadMore.visibility = View.GONE
                }
            }
        })

    }
}