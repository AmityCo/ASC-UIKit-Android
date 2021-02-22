package com.ekoapp.ekosdk.uikit.common.views.text

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.StyleRes
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.common.views.Style

class EkoTextStyle : Style {

    var textColor: Int = -1
    var textStyle: Int = -1

    constructor(context: Context, attributeSet: AttributeSet) : super(context) {
        parseStyle(context, attributeSet)
    }

    constructor(context: Context, @StyleRes style: Int) : super(context) {
        parseStyle(context, style)
    }


    private fun parseStyle(context: Context, attrs: AttributeSet) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.Text)
        parse(typedArray)
    }

    private fun parseStyle(context: Context, @StyleRes style: Int) {
        val typeArray = context.obtainStyledAttributes(style, R.styleable.Text);
        parse(typeArray)
    }

    private fun parse(typedArray: TypedArray) {
        textColor = typedArray.getColor(
            R.styleable.Text_textColor,
            getColor(R.color.amityColorBase)
        )
        textStyle = typedArray.getInt(
            R.styleable.Text_textStyleValue,
            Typeface.NORMAL
        )

        typedArray.recycle()

    }


}