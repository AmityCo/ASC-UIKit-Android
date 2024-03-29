package com.ekoapp.ekosdk.uikit.community.views.createpost

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleRes
import com.ekoapp.ekosdk.uikit.common.views.Style
import com.ekoapp.ekosdk.uikit.community.R

class EkoPostComposeViewStyle : Style {
    var backgroundColor: Int = -1
    var padding: Int = -1
    var hint: Int = -1

    init {
        backgroundColor = getColor(android.R.color.transparent)
        padding = getDimensionPixelSize(R.dimen.amity_padding_xs)
        hint = R.string.amity_post_compose_hint
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context)
    constructor(context: Context, @StyleRes style: Int) : super(context)

}