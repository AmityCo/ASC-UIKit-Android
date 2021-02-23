package com.ekoapp.ekosdk.uikit.community.ui.adapter

import android.view.View
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoSelectMemberListener
import com.ekoapp.ekosdk.uikit.community.ui.viewHolder.EkoMemberListItemViewHolder
import com.ekoapp.ekosdk.user.EkoUser

class EkoSearchResultAdapter(private val listener: EkoSelectMemberListener)
    : EkoBaseRecyclerViewPagedAdapter<EkoUser>(diffCallback) {

    private val selectedMemberSet = HashSet<String>()

    override fun getLayoutId(position: Int, obj: EkoUser?): Int = R.layout.amity_item_select_member

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder =
        EkoMemberListItemViewHolder(view, listener, selectedMemberSet)

    fun submitPagedList(pagedList: PagedList<EkoUser>?, memberSet: HashSet<String>) {
        selectedMemberSet.clear()
        selectedMemberSet.addAll(memberSet)
        super.submitList(pagedList)
    }

    fun notifyChange(position: Int, memberSet: HashSet<String>) {
        selectedMemberSet.clear()
        selectedMemberSet.addAll(memberSet)
        super.notifyItemChanged(position)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<EkoUser>() {
            override fun areItemsTheSame(oldItem: EkoUser, newItem: EkoUser): Boolean =
                oldItem.getUserId() == newItem.getUserId()

            override fun areContentsTheSame(oldItem: EkoUser, newItem: EkoUser): Boolean {
                return oldItem.getUserId() == newItem.getUserId()
                        && oldItem.getDisplayName() == newItem.getDisplayName()
            }
        }
    }
}