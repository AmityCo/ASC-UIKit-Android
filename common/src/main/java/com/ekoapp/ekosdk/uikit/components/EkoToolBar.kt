package com.ekoapp.ekosdk.uikit.components

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.common.expandViewHitArea
import com.ekoapp.ekosdk.uikit.common.views.ColorPaletteUtil
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.databinding.EkoToolbarBinding
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.eko_toolbar.view.*

class EkoToolBar : MaterialToolbar {

    private lateinit var mBinding: EkoToolbarBinding

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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.eko_toolbar, this, true)
        mBinding.rightStringActive = false
        toggleRightTextColor(false)
        setContentInsetsRelative(0, 0)
        setUpImageViewLeft()
        setUpImageViewRight()
    }

    private fun setUpImageViewLeft() {
        ivLeft.expandViewHitArea()
    }

    private fun setUpImageViewRight() {
        tv_right.expandViewHitArea()
    }

    fun setLeftString(value: String) {
        mBinding.leftString = value
    }

    fun setLeftDrawable(value: Drawable?, color: Int? = null) {
        mBinding.leftDrawable = value
        if (color != null && mBinding.leftDrawable != null) {
            mBinding.leftDrawable!!.colorFilter =
                    PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }

    }

    fun setRightString(value: String) {
        mBinding.rightString = value
    }

    fun setRightStringActive(value: Boolean) {
        mBinding.rightStringActive = value
        toggleRightTextColor(value)
    }

    private fun toggleRightTextColor(value: Boolean) {
        if (value) {
            tv_right.setTextColor(
                    ColorPaletteUtil.getColor(
                            ContextCompat.getColor(context, R.color.upstraColorHighlight),
                            ColorShade.DEFAULT
                    )
            )
        } else {
            tv_right.setTextColor(
                    ColorPaletteUtil.getColor(
                            ContextCompat.getColor(context, R.color.upstraColorHighlight), ColorShade.SHADE2
                    )
            )
        }
    }

    fun setRightDrawable(value: Drawable?, color: Int? = null) {
        mBinding.rightDrawable = value
        if (color != null && mBinding.rightDrawable != null) {
            mBinding.rightDrawable!!.colorFilter =
                    PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    fun setClickListener(listener: EkoToolBarClickListener) {
        mBinding.clickListener = listener
    }
}