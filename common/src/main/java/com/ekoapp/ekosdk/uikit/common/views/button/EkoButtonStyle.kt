package com.ekoapp.ekosdk.uikit.common.views.button

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.StyleRes
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.common.views.Style

class EkoButtonStyle : Style {
    var outlineColor: Int = -1
    var backgroundColor: Int = -1
    var buttonType: Int = EkoButtonType.Solid
    var buttonTextColor: Int = -1

    constructor(context: Context, attributeSet: AttributeSet) : super(context) {
        parseStyle(context, attributeSet)
    }

    constructor(context: Context, @StyleRes style: Int) : super(context) {
        parseStyle(context, style)
    }


    private fun parseStyle(context: Context, attrs: AttributeSet) {
        val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.Button)
        parse(typedArray)
    }

    private fun parseStyle(context: Context, @StyleRes style: Int) {
        val typeArray = context.obtainStyledAttributes(style, R.styleable.Button);
        parse(typeArray)
    }

    private fun parse(typedArray: TypedArray) {
        outlineColor = typedArray.getColor(
                R.styleable.Button_outlineColor,
                getColor(R.color.amityColorBase)
        )

        backgroundColor = typedArray.getColor(
                R.styleable.Button_backgroundColor,
                getColor(R.color.amityColorBlack)
        )

        buttonType = typedArray.getInt(R.styleable.Button_buttonType, EkoButtonType.Solid)
        buttonTextColor =
                typedArray.getInt(R.styleable.Button_buttonTextColor, getColor(R.color.amityColorBase))

        typedArray.recycle()

    }
}
