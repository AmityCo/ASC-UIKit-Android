package com.ekoapp.ekosdk.uikit.common.views.button

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.setPadding
import com.ekoapp.ekosdk.uikit.R

class EkoButton : AppCompatButton {

    lateinit var style: EkoButtonStyle


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
            context,
            attrs,
            defStyle
    ) {
        parseStyle(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {
        parseStyle(attrs)
    }


    private fun parseStyle(attrs: AttributeSet) {
        style = EkoButtonStyle(context, attrs)
        applyStyle()
    }

    fun setViewStyle(style: EkoButtonStyle) {
        this.style = style
        applyStyle()
    }

    private fun applyStyle() {
        if (style.backgroundColor != null) {
            setBackgroundColor(style.backgroundColor)
        }

        setTextColor(style.buttonTextColor)

        setPadding(context.resources.getDimensionPixelSize(R.dimen.padding_8dp))


    }
}