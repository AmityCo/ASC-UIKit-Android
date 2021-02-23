package com.ekoapp.ekosdk.uikit.community.members

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.community.membership.EkoCommunityMembership
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.common.views.bottomsheet.EkoBottomSheetListFragment
import com.ekoapp.ekosdk.uikit.common.views.bottomsheet.IEkoMenuItemClickListener
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityItemCommunityMembershipBinding
import com.ekoapp.ekosdk.uikit.model.EkoMenuItem
import com.ekoapp.ekosdk.uikit.utils.EkoConstants
import com.ekoapp.ekosdk.user.EkoUser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EkoCommunityMembersAdapter(
        private val context: Context,
        private val listener: IMemberClickListener,
        private val communityMemberViewModel: EkoCommunityMembersViewModel
) :
        EkoBaseRecyclerViewPagedAdapter<EkoCommunityMembership>(diffCallBack) {

    private var isJoined: Boolean = false

    override fun getLayoutId(position: Int, obj: EkoCommunityMembership?): Int =
            R.layout.amity_item_community_membership

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        val itemViewModel = EkoMembershipItemViewModel()
        itemViewModel.communityId = communityMemberViewModel.communityId
        itemViewModel.isModerator.set(communityMemberViewModel.isModerator.get())
        return EkoMembershipViewHolder(view, context, listener, itemViewModel)
    }

    fun setUserIsJoined(isJoined: Boolean) {
        this.isJoined = isJoined
    }

    inner class EkoMembershipViewHolder(
            itemView: View, private val context: Context,
            private val listener: IMemberClickListener,
            private val itemViewModel: EkoMembershipItemViewModel
    ) :
            EkoCommunityMembersBaseViewHolder(
                    itemView,
                    context,
                    itemViewModel,
                    communityMemberViewModel
            ),
            Binder<EkoCommunityMembership> {
        private val binding: AmityItemCommunityMembershipBinding? = DataBindingUtil.bind(itemView)

        override fun bind(data: EkoCommunityMembership?, position: Int) {
            binding?.avatarUrl = data?.getUser()?.getAvatar()?.getUrl(EkoImage.Size.MEDIUM)
            binding?.communityMemberShip = data
            binding?.listener = listener
            binding?.isJoined = isJoined
            binding?.ivMore?.setOnClickListener {
                if (!data?.getUserId().isNullOrEmpty()) {
                    getUser(data?.getUserId()!!)
                } else {
                    //TODO Handle when user id is null or empty
                }
            }

            binding?.isMyUser = data?.getUserId() == EkoClient.getUserId()
        }

        private fun getUser(userId: String) {
            itemViewModel.getUser(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .firstOrError()
                    .doOnError {

                    }
                    .doOnSuccess { ekoUser ->
                        showBottomSheet(context, ekoUser)
                    }
                    .subscribe()
        }

        private fun showBottomSheet(context: Context, ekoUser: EkoUser) {
            val itemList = arrayListOf<EkoMenuItem>()
            if (itemViewModel.isModerator.get()) {
                itemList.add(
                        EkoMenuItem(
                                EkoConstants.ID_PROMOTE_MODERATOR,
                                context.getString(R.string.amity_promote_moderator)
                        )
                )
            }
            if (ekoUser.isFlaggedByMe()) {
                itemList.add(
                        EkoMenuItem(
                                EkoConstants.ID_UN_REPORT_USER,
                                context.getString(R.string.amity_unreport_user)
                        )
                )
            } else {
                itemList.add(
                        EkoMenuItem(
                                EkoConstants.ID_REPORT_USER,
                                context.getString(R.string.amity_report_user)
                        )
                )
            }
            if (itemViewModel.isModerator.get()) {
                itemList.add(
                        EkoMenuItem(
                                EkoConstants.ID_REMOVE_USER,
                                context.getString(R.string.amity_remove_user)
                        )
                )
            }
            val manager = (context as AppCompatActivity).supportFragmentManager
            val fragment = EkoBottomSheetListFragment.newInstance(itemList)
            fragment.show(manager, EkoBottomSheetListFragment.toString())
            fragment.setMenuItemClickListener(object : IEkoMenuItemClickListener {
                override fun onMenuItemClicked(menuItem: EkoMenuItem) {
                    fragment.dismiss()
                    when (menuItem.id) {
                        EkoConstants.ID_REPORT_USER -> sendReportUser(ekoUser, true)
                        EkoConstants.ID_UN_REPORT_USER -> sendReportUser(ekoUser, false)
                        EkoConstants.ID_PROMOTE_MODERATOR -> promoteModerator(ekoUser)
                        EkoConstants.ID_REMOVE_USER -> showRemoveUserDialog(ekoUser)
                    }
                }
            })
        }

        private fun promoteModerator(ekoUser: EkoUser) {
            itemViewModel.assignRole(EkoConstants.MODERATOR_ROLE, listOf(ekoUser.getUserId()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {

                    }.doOnError {
                        handleNoPermissionError(it)
                    }.subscribe()

        }
    }

    companion object {
        private val diffCallBack = object : DiffUtil.ItemCallback<EkoCommunityMembership>() {
            override fun areItemsTheSame(
                    oldItem: EkoCommunityMembership,
                    newItem: EkoCommunityMembership
            ): Boolean = oldItem.getUserId() == newItem.getUserId()

            override fun areContentsTheSame(
                    oldItem: EkoCommunityMembership,
                    newItem: EkoCommunityMembership
            ): Boolean = oldItem == newItem
        }
    }
}