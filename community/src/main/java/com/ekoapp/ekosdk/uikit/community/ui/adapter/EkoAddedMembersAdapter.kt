package com.ekoapp.ekosdk.uikit.community.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoAddedMemberClickListener
import com.ekoapp.ekosdk.uikit.community.ui.viewHolder.EkoAddedMemberWithAddButtonViewHolder
import com.ekoapp.ekosdk.uikit.community.ui.viewHolder.EkoAddedMembersCountViewHolder
import com.ekoapp.ekosdk.uikit.community.ui.viewHolder.EkoAddedMembersViewHolder
import com.ekoapp.ekosdk.uikit.community.utils.SelectMemberItemDiffCallBack

class EkoAddedMembersAdapter(private val listener: EkoAddedMemberClickListener) :
        EkoBaseRecyclerViewAdapter<SelectMemberItem>() {
    override fun getLayoutId(position: Int, obj: SelectMemberItem?): Int {
        return when (obj?.name) {
            "ADD" -> R.layout.amity_view_added_member_with_add_icon
            else -> {
                if (position == 7) {
                    R.layout.amity_view_added_member_with_count
                } else {
                    R.layout.amity_item_added_member
                }
            }
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.amity_view_added_member_with_count -> EkoAddedMembersCountViewHolder(view, listener)
            R.layout.amity_view_added_member_with_add_icon -> EkoAddedMemberWithAddButtonViewHolder(
                    view,
                    listener
            )
            else -> EkoAddedMembersViewHolder(view, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < 8) {
            super.getItemCount()
        } else {
            8
        }

    }

    fun submitList(newList: List<SelectMemberItem>) {
        setItems(newList, SelectMemberItemDiffCallBack(list, newList))
    }
}