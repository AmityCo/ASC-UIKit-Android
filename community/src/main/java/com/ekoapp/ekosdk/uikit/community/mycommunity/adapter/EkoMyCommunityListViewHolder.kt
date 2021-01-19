package com.ekoapp.ekosdk.uikit.community.mycommunity.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.common.loadImage
import com.ekoapp.ekosdk.uikit.common.setBackgroundColor
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.LayoutMyCommunityItemBinding
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener


class EkoMyCommunityListViewHolder(
        itemView: View,
        private val listener: IMyCommunityItemClickListener
) : RecyclerView.ViewHolder(itemView), EkoBaseRecyclerViewPagedAdapter.Binder<EkoCommunity> {
    private val binding: LayoutMyCommunityItemBinding? = DataBindingUtil.bind(itemView)

    override fun bind(data: EkoCommunity?, position: Int) {
        if (position == 8) {
            binding?.listener = listener
            binding?.executePendingBindings()
            binding?.tvName?.text = itemView.context.getString(R.string.see_all)
            binding?.ivAvatar?.setBackgroundColor(null, ColorShade.SHADE4)
            binding?.ivAvatar?.setImageResource(R.drawable.ic_uikit_arrow_back)
            binding?.ivAvatar?.rotation = 180F
        } else {
            binding?.ekoCommunity = data
            binding?.listener = listener
            binding?.ivAvatar?.loadImage(
                    data?.getAvatar()?.getUrl(),
                    R.drawable.ic_uikit_default_community_avatar_small
            )
        }
    }

}
