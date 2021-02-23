package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityItemCommunitySelectionListBinding
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.ICreatePostCommunitySelectionListener

class EkoCreatePostCommunitySelectionAdapter(private val listener: ICreatePostCommunitySelectionListener) :
        EkoBaseRecyclerViewPagedAdapter<EkoCommunity>(diffCallBack) {

    override fun getLayoutId(position: Int, obj: EkoCommunity?): Int =
            R.layout.amity_item_community_selection_list

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return EkoCommunityViewHolder(view, listener)
    }

    class EkoCommunityViewHolder(
            itemView: View,
            private val listener: ICreatePostCommunitySelectionListener?
    ) : RecyclerView.ViewHolder(itemView), Binder<EkoCommunity> {

        private val binding: AmityItemCommunitySelectionListBinding? =
                DataBindingUtil.bind(itemView)

        override fun bind(data: EkoCommunity?, position: Int) {
            if (data != null) {
                binding?.community = data
                setupCommunityNameView(data)
                setupCommunityImageView(data)
                itemView.setOnClickListener { listener?.onClickCommunity(data, position) }
            }
        }

        private fun setupCommunityNameView(data: EkoCommunity) {
            var leftDrawable: Drawable? = null
            var rightDrawable: Drawable? = null
            if (!data.isPublic()) {
                leftDrawable =
                        ContextCompat.getDrawable(itemView.context, R.drawable.amity_ic_lock2)
            }
            if (data.isOfficial()) {
                rightDrawable =
                        ContextCompat.getDrawable(itemView.context, R.drawable.amity_ic_verified)
            }
            binding?.tvCommunityName?.setCompoundDrawablesWithIntrinsicBounds(
                    leftDrawable,
                    null,
                    rightDrawable,
                    null
            )
        }

        private fun setupCommunityImageView(data: EkoCommunity) {

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