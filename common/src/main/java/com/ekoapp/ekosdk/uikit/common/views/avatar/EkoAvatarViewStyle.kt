package com.ekoapp.ekosdk.uikit.common.views.avatar

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.StyleRes
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.common.views.Style

class EkoAvatarViewStyle : Style {
    var avatarHeight: Int = -1
    var avatarWidth: Int = -1
    var avatarShape: Int = -1
    var avatarDrawable: Int = -1
    var avatarUrl: String? = null

    constructor(context: Context, attributeSet: AttributeSet) : super(context) {
        parseStyle(context, attributeSet)
    }

    constructor(context: Context, @StyleRes style: Int) : super(context) {
        parseStyle(context, style)
    }

    private fun parseStyle(context: Context, attrs: AttributeSet) {
        val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.Avatar)
        parse(typedArray)
    }

    private fun parseStyle(context: Context, @StyleRes style: Int) {
        val typeArray = context.obtainStyledAttributes(style, R.styleable.Avatar);
        parse(typeArray)
    }

    private fun parse(typedArray: TypedArray) {
        avatarHeight = typedArray.getDimensionPixelSize(
                R.styleable.Avatar_avatarHeight,
                getDimensionPixelSize(R.dimen.avatar_default_height)
        )
        avatarWidth = typedArray.getDimensionPixelSize(
                R.styleable.Avatar_avatarWidth,
                getDimensionPixelSize(R.dimen.avatar_default_width)
        )
        avatarShape = typedArray.getInt(R.styleable.Avatar_avatarShape, AvatarShape.Circle)
        avatarDrawable = typedArray.getResourceId(R.styleable.Avatar_avatarDrawable, -1);
        avatarUrl = typedArray.getString(R.styleable.Avatar_avatarUrl)
        typedArray.recycle()

    }
}
