package com.ekoapp.ekosdk.uikit.community.explore.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.explore.listener.IEkoCommunityItemClickListener

class EkoCategoryCommunityListAdapter(
        diffUtil: EkoCommunityDiffUtil,
        private val itemClickListener: IEkoCommunityItemClickListener
) :
        EkoBaseRecyclerViewPagedAdapter<EkoCommunity>(diffUtil) {


    override fun getLayoutId(position: Int, obj: EkoCommunity?): Int {
        return R.layout.amity_item_category_community_list
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return EkoCategoryCommunityItemViewHolder(view, itemClickListener)
    }

    class EkoCommunityDiffUtil : DiffUtil.ItemCallback<EkoCommunity>() {
        override fun areItemsTheSame(
                oldItem: EkoCommunity,
                newItem: EkoCommunity
        ): Boolean {
            return oldItem.getCommunityId() == newItem.getCommunityId()
        }

        override fun areContentsTheSame(
                oldItem: EkoCommunity,
                newItem: EkoCommunity
        ): Boolean {
            return oldItem.getCommunityId() == newItem.getCommunityId()
                    && oldItem.getDisplayName() == newItem.getDisplayName()
                    && oldItem.getMemberCount() == newItem.getMemberCount()
        }

    }

}
