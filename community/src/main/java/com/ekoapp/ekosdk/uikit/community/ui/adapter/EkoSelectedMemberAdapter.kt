package com.ekoapp.ekosdk.uikit.community.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoSelectedMemberListener
import com.ekoapp.ekosdk.uikit.community.ui.viewHolder.EkoSelectedMemberViewHolder
import com.ekoapp.ekosdk.uikit.community.utils.SelectMemberItemDiffCallBack

class EkoSelectedMemberAdapter(private val listener: EkoSelectedMemberListener) :
        EkoBaseRecyclerViewAdapter<SelectMemberItem>() {

    override fun getLayoutId(position: Int, obj: SelectMemberItem?): Int =
            R.layout.amity_item_selected_member

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder =
            EkoSelectedMemberViewHolder(view, listener)

    fun submitList(newList: List<SelectMemberItem>) {
        setItems(newList, SelectMemberItemDiffCallBack(list, newList))
    }
}