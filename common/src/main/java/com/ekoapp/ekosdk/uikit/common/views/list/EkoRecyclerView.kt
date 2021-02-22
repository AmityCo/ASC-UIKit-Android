package com.ekoapp.ekosdk.uikit.common.views.list

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class EkoRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
            context,
            attributeSet,
            defStyle
    )
}