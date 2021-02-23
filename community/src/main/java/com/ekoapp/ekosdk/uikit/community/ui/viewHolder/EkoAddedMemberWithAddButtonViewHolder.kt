package com.ekoapp.ekosdk.uikit.community.ui.viewHolder

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.common.toCircularShape
import com.ekoapp.ekosdk.uikit.common.views.ColorPaletteUtil
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoAddedMemberClickListener

class EkoAddedMemberWithAddButtonViewHolder(
        itemView: View,
        private val mClickListener: EkoAddedMemberClickListener
) : RecyclerView.ViewHolder(itemView), EkoBaseRecyclerViewAdapter.IBinder<SelectMemberItem> {

    private val ivAdd: ImageView = itemView.findViewById(R.id.ivAdd)

    init {
        ivAdd.toCircularShape(
                ColorPaletteUtil.getColor(
                        ContextCompat.getColor(itemView.context, R.color.amityColorBase), ColorShade.SHADE4
                )
        )
    }

    override fun bind(data: SelectMemberItem?, position: Int) {
        ivAdd.setOnClickListener {
            mClickListener.onAddButtonClicked()
        }
    }
}