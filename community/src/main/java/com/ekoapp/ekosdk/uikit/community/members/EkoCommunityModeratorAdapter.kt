package com.ekoapp.ekosdk.uikit.community.members

import android.content.Context
import android.util.Log
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
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.databinding.LayoutCommunityMembershipItemBinding
import com.ekoapp.ekosdk.uikit.model.EkoMenuItem
import com.ekoapp.ekosdk.uikit.utils.EkoConstants
import com.ekoapp.ekosdk.user.EkoUser
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EkoCommunityModeratorAdapter(
    private val context: Context,
    private val listener: IMemberClickListener,
    private val communityMemberViewModel: EkoCommunityMembersViewModel
) : EkoBaseRecyclerViewPagedAdapter<EkoCommunityMembership>(diffCallBack) {

    override fun getLayoutId(position: Int, obj: EkoCommunityMembership?): Int =
        R.layout.layout_community_membership_item

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        val itemViewModel = EkoMembershipItemViewModel()
        itemViewModel.communityId = communityMemberViewModel.communityId
        itemViewModel.isModerator = communityMemberViewModel.isModerator.get()
        return EkoModeratorViewHolder(view, context, listener, itemViewModel)
    }

    inner class EkoModeratorViewHolder(
        itemView: View, private val context: Context,
        private val listener: IMemberClickListener,
        private val itemViewModel: EkoMembershipItemViewModel
    ) : RecyclerView.ViewHolder(itemView), Binder<EkoCommunityMembership> {

        private val binding: LayoutCommunityMembershipItemBinding? = DataBindingUtil.bind(itemView)

        override fun bind(data: EkoCommunityMembership?, position: Int) {
            binding?.avatarUrl = data?.getUser()?.getAvatar()?.getUrl(EkoImage.Size.MEDIUM)
            binding?.communityMemberShip = data
            binding?.listener = listener
            binding?.isMyUser = data?.getUserId() == EkoClient.getUserId()
            binding?.isJoined = communityMemberViewModel.isJoined.get()

            binding?.ivMore?.setOnClickListener {
                if (!data?.getUserId().isNullOrEmpty()) {
                    getUser(data?.getUserId()!!)
                } else {
                    //TODO Handle when user id is null or empty
                }
            }

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
            if (itemViewModel.isModerator) {
                itemList.add(
                    EkoMenuItem(
                        EkoConstants.ID_REMOVE_MODERATOR,
                        context.getString(R.string.remove_moderator)
                    )
                )
            }
            if (ekoUser.isFlaggedByMe()) {
                itemList.add(
                    EkoMenuItem(
                        EkoConstants.ID_UN_REPORT_USER,
                        context.getString(R.string.unreport_user)
                    )
                )
            } else {
                itemList.add(
                    EkoMenuItem(
                        EkoConstants.ID_REPORT_USER,
                        context.getString(R.string.report_user)
                    )
                )
            }
            if (itemViewModel.isModerator) {
                itemList.add(
                    EkoMenuItem(
                        EkoConstants.ID_REMOVE_USER,
                        context.getString(R.string.remove_user)
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
                        EkoConstants.ID_REMOVE_USER -> removeUser(ekoUser)
                        EkoConstants.ID_REMOVE_MODERATOR -> removeModerator(ekoUser)
                    }
                }
            })
        }


        private fun sendReportUser(ekoUser: EkoUser, isReport: Boolean) {
            val viewModel = if (isReport) {
                itemViewModel.reportUser(ekoUser)
            } else {
                itemViewModel.unReportUser(ekoUser)
            }
            viewModel.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    showDialogSentReportMessage(isReport)
                }.doOnError {

                }.subscribe()
        }

        private fun showDialogSentReportMessage(isReport: Boolean) {
            val messageSent = if (isReport) {
                R.string.report_sent
            } else {
                R.string.unreport_sent
            }
            Snackbar.make(
                itemView,
                context.getString(messageSent),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        private fun removeUser(ekoUser: EkoUser) {
            val list = listOf(ekoUser.getUserId())
            itemViewModel.removeUser(list).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    communityMemberViewModel.updateSelectedMembersList(
                        SelectMemberItem(
                            ekoUser.getUserId(),
                            ekoUser.getAvatar()?.getUrl(EkoImage.Size.MEDIUM) ?: "",
                            ekoUser.getDisplayName() ?: context.getString(R.string.anonymous),
                            ekoUser.getDescription(),
                            false
                        )
                    )
                }.doOnError {

                }.subscribe()
        }

        private fun removeModerator(ekoUser: EkoUser) {
            itemViewModel.removeRole(EkoConstants.MODERATOR_ROLE, listOf(ekoUser.getUserId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {

                }.doOnError {
                    Log.e("ModeratorAdapter", "removeModerator: $it")
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