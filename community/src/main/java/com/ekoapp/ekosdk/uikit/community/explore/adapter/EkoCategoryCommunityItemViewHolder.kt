package com.ekoapp.ekosdk.uikit.community.explore.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityItemCategoryCommunityListBinding
import com.ekoapp.ekosdk.uikit.community.explore.listener.IEkoCommunityItemClickListener

open class EkoCategoryCommunityItemViewHolder(
        itemView: View,
        private val itemClickListener: IEkoCommunityItemClickListener?
) : RecyclerView.ViewHolder(itemView),
        EkoBaseRecyclerViewPagedAdapter.Binder<EkoCommunity> {
    private val binding: AmityItemCategoryCommunityListBinding? = DataBindingUtil.bind(itemView)

    override fun bind(data: EkoCommunity?, position: Int) {
        if (data?.isOfficial() == true) {
            binding?.tvCommunityName?.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.amity_ic_verified,
                    0
            )
        } else {
            binding?.tvCommunityName?.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
            )
        }

        data?.let {
            loadAvatar(data)
            setupCommunityDetails(data)
            itemView.setOnClickListener {
                itemClickListener?.onClick(data, position)
            }
        }
    }

    private fun setupCommunityDetails(data: EkoCommunity) {
        binding?.tvCommunityName?.text = data.getDisplayName()
        //TODO uncomment after finalizing
        //binding?.tvMembersCount?.text = data.getMemberCount().readableNumber()
    }

    private fun loadAvatar(data: EkoCommunity) {
        data.getAvatar()?.getUrl()?.let {
            Glide.with(itemView)
                    .load(it)
                    .centerCrop()
                    .into(binding?.communityAvatar!!)
        }
    }
}