package com.ekoapp.ekosdk.uikit.community.utils

import androidx.recyclerview.widget.DiffUtil
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem

class SelectMemberItemDiffCallBack(
        private val oldList: List<SelectMemberItem>,
        private val newList: List<SelectMemberItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].name == newList[newItemPosition].name &&
                    oldList[oldItemPosition].subTitle == newList[newItemPosition].subTitle

}