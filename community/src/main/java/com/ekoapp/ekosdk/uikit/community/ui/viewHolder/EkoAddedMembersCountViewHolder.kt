package com.ekoapp.ekosdk.uikit.community.ui.viewHolder

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ekoapp.ekosdk.uikit.common.setShape
import com.ekoapp.ekosdk.uikit.common.toCircularShape
import com.ekoapp.ekosdk.uikit.common.views.ColorPaletteUtil
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.databinding.AmityViewAddedMemberWithCountBinding
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoAddedMemberClickListener

class EkoAddedMembersCountViewHolder(
        itemView: View,
        private val mClickListener: EkoAddedMemberClickListener
) : EkoAddedMembersViewHolder(itemView, mClickListener) {

    private val layoutMember: ConstraintLayout = itemView.findViewById(R.id.layoutAddedMember)
    private val binding: AmityViewAddedMemberWithCountBinding? = DataBindingUtil.bind(itemView)

    init {
        val radius = itemView.context.resources.getDimensionPixelSize(R.dimen.twenty_four).toFloat()
        layoutMember.setShape(
                radius,
                radius,
                radius,
                radius,
                R.color.amityColorBase,
                R.color.amityColorBase,
                ColorShade.SHADE4
        )
        binding?.ivAdd?.toCircularShape(
                ColorPaletteUtil.getColor(
                        ContextCompat.getColor(itemView.context, R.color.amityColorBase), ColorShade.SHADE4
                )
        )
        binding?.tvCount?.toCircularShape(
                ColorPaletteUtil.getColor(
                        ContextCompat.getColor(itemView.context, R.color.amityColorBase), ColorShade.SHADE4
                )
        )
    }

    override fun bind(data: SelectMemberItem?, position: Int) {
        super.bind(data, position)
        if (data != null) {
            binding?.tvCount?.text = "+${data.subTitle}"
            binding?.executePendingBindings()
            if (data.subTitle == "0") {
                binding?.tvCount?.visibility = View.GONE
                binding?.ivAdd?.visibility = View.VISIBLE
            } else {
                binding?.tvCount?.visibility = View.VISIBLE
                binding?.ivAdd?.visibility = View.GONE
            }
            binding?.ivAdd?.setOnClickListener {
                mClickListener.onAddButtonClicked()
            }

            binding?.tvCount?.setOnClickListener {
                mClickListener.onMemberCountClicked()
            }
        }

    }
}