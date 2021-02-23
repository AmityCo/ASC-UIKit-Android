package com.ekoapp.ekosdk.uikit.community.ui.viewHolder

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.common.loadImage
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.databinding.AmityItemSelectedMemberBinding
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoSelectedMemberListener

class EkoSelectedMemberViewHolder(
        itemView: View,
        private val mClickListener: EkoSelectedMemberListener
) : RecyclerView.ViewHolder(itemView),
        EkoBaseRecyclerViewAdapter.IBinder<SelectMemberItem> {

    private val binding: AmityItemSelectedMemberBinding? = DataBindingUtil.bind(itemView)

    override fun bind(data: SelectMemberItem?, position: Int) {

        if (data != null) {
            binding?.ivAvatar?.loadImage(data.avatarUrl)
            binding?.tvName?.text = data.name

            binding?.ivCross?.setOnClickListener {
                mClickListener.onMemberRemoved(data)
            }
        }

    }
}