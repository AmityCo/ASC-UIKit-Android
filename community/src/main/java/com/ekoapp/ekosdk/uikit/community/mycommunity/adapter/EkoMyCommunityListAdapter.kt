package com.ekoapp.ekosdk.uikit.community.mycommunity.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener

class EkoMyCommunityListAdapter(
    private val listener: IMyCommunityItemClickListener,
    private val previewMode: Boolean
) :
    EkoBaseRecyclerViewPagedAdapter<EkoCommunity>(diffCallBack) {

    override fun getLayoutId(position: Int, obj: EkoCommunity?): Int {
        return if (previewMode) R.layout.layout_my_community_item else R.layout.layout_community_item
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.layout_my_community_item) {
            EkoMyCommunityListViewHolder(view, previewMode, listener)
        } else {
            EkoMyCommunitiesViewHolder(view, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (previewMode) {
            super.getItemCount().coerceAtMost(9)
        } else {
            super.getItemCount()
        }
    }

    companion object {
        private val diffCallBack = object : DiffUtil.ItemCallback<EkoCommunity>() {

            override fun areItemsTheSame(oldItem: EkoCommunity, newItem: EkoCommunity): Boolean =
                oldItem.getCommunityId() == newItem.getCommunityId()

            override fun areContentsTheSame(oldItem: EkoCommunity, newItem: EkoCommunity): Boolean =
                oldItem.getAvatar()?.getUrl() == newItem.getAvatar()?.getUrl()
                        && oldItem.getDisplayName() == newItem.getDisplayName()
                        && oldItem.isOfficial() == newItem.isOfficial()
        }
    }
}