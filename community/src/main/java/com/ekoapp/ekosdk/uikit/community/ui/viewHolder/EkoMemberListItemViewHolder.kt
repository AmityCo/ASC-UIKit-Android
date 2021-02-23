package com.ekoapp.ekosdk.uikit.community.ui.viewHolder

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityItemSelectMemberBinding
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoSelectMemberListener
import com.ekoapp.ekosdk.user.EkoUser

class EkoMemberListItemViewHolder(
        itemView: View,
        private val mClickListener: EkoSelectMemberListener,
        private val membersSet: HashSet<String>
) : RecyclerView.ViewHolder(itemView), EkoBaseRecyclerViewPagedAdapter.Binder<EkoUser> {

    private val binding: AmityItemSelectMemberBinding? = DataBindingUtil.bind(itemView)

    override fun bind(data: EkoUser?, position: Int) {
        if (data != null) {
            binding?.smTitle?.text = if (data.getDisplayName().isNullOrEmpty()) {
                itemView.context.getString(R.string.amity_anonymous)
            } else {
                data.getDisplayName()
            }
            binding?.avatarUrl = data.getAvatar()?.getUrl(EkoImage.Size.MEDIUM)
            binding?.smSubTitle?.text = ""
            binding?.ivStatus?.isChecked = membersSet.contains(data.getUserId())
            binding?.ivStatus?.setOnClickListener {
                mClickListener.onMemberClicked(data, position)
            }
        }
    }
}