package com.ekoapp.ekosdk.uikit.common.views.text

import android.content.Context
import android.util.AttributeSet

class EkoTextView : androidx.appcompat.widget.AppCompatTextView {

    lateinit var style: EkoTextStyle


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
        style = EkoTextStyle(context, attrs)
        applyStyle()
    }

    fun setViewStyle(style: EkoTextStyle) {
        this.style = style
        applyStyle()
    }

    private fun applyStyle() {
        setTextColor(style.textColor)
        setTypeface(typeface, style.textStyle)
    }
}