package com.ekoapp.ekosdk.uikit.community.ui.viewHolder

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityItemHeaderSelectMemberBinding
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoSelectMemberListener
import com.ekoapp.ekosdk.user.EkoUser

class EkoMemberListHeaderViewHolder(
        itemView: View,
        private val mClickListener: EkoSelectMemberListener,
        private val membersSet: HashSet<String>
) : RecyclerView.ViewHolder(itemView),
        EkoBaseRecyclerViewPagedAdapter.Binder<EkoUser> {

    private val binding: AmityItemHeaderSelectMemberBinding? = DataBindingUtil.bind(itemView)

    override fun bind(data: EkoUser?, position: Int) {
        if (data != null) {
            if (data.getDisplayName().isNullOrEmpty()) {
                binding?.tvHeader?.text = "#"
                binding?.layoutMember?.smTitle?.text =
                        itemView.context.getString(R.string.amity_anonymous)
            } else {
                binding?.tvHeader?.text = data.getDisplayName()!![0].toString()
                binding?.layoutMember?.smTitle?.text = data.getDisplayName()
            }
            binding?.layoutMember?.avatarUrl = data.getAvatar()?.getUrl(EkoImage.Size.MEDIUM)
            binding?.layoutMember?.smSubTitle?.text = ""
            binding?.layoutMember?.ivStatus?.isChecked = membersSet.contains(data.getUserId())
            binding?.layoutMember?.ivStatus?.setOnClickListener {
                mClickListener.onMemberClicked(data, position)
            }
        }
    }
}