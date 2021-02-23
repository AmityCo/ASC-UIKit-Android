package com.ekoapp.ekosdk.uikit.community.members

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.exception.EkoException
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.home.activity.EkoCommunityHomePageActivity
import com.ekoapp.ekosdk.uikit.utils.AlertDialogUtil
import com.ekoapp.ekosdk.uikit.utils.EkoConstants
import com.ekoapp.ekosdk.user.EkoUser
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class EkoCommunityMembersBaseViewHolder(
        itemView: View, private val context: Context,
        private val itemViewModel: EkoMembershipItemViewModel,
        private val communityMemberViewModel: EkoCommunityMembersViewModel
) : RecyclerView.ViewHolder(itemView) {

    fun sendReportUser(ekoUser: EkoUser, isReport: Boolean) {
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
                    handleNoPermissionError(it)
                }.subscribe()
    }

    private fun showDialogSentReportMessage(isReport: Boolean) {
        val messageSent = if (isReport) {
            R.string.amity_report_sent
        } else {
            R.string.amity_unreport_sent
        }
        Snackbar.make(
                itemView,
                context.getString(messageSent),
                Snackbar.LENGTH_SHORT
        ).show()
    }

    fun showRemoveUserDialog(ekoUser: EkoUser) {
        AlertDialogUtil.showDialog(context, context.getString(R.string.amity_remove_user),
                context.getString(R.string.amity_remove_user_msg), context.getString(R.string.amity_remove),
                context.getString(R.string.amity_cancel),
                DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        removeUser(ekoUser)
                    } else {
                        dialog?.cancel()
                    }
                })
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
                                    ekoUser.getDisplayName()
                                            ?: context.getString(R.string.amity_anonymous),
                                    ekoUser.getDescription(),
                                    false
                            )
                    )
                }.doOnError {
                    handleNoPermissionError(it)
                }.subscribe()

        removeModerator(ekoUser)
    }

    fun removeModerator(ekoUser: EkoUser) {
        itemViewModel.removeRole(EkoConstants.MODERATOR_ROLE, listOf(ekoUser.getUserId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {

                }.doOnError {
                    handleNoPermissionError(it)
                }.subscribe()

    }

    fun handleNoPermissionError(exception: Throwable) {
        if (exception is EkoException) {
            if (exception.code == EkoConstants.NO_PERMISSION_ERROR_CODE) {
                AlertDialogUtil.showNoPermissionDialog(context,
                        DialogInterface.OnClickListener { dialog, _ ->
                            dialog?.dismiss()
                            checkUserRole()
                        })
            } else {
                Log.e("EkoCommBaseViewHolder", "${exception.message}")
            }
        } else {
            Log.e("EkoCommBaseViewHolder", "${exception.message}")
        }
    }

    private fun checkUserRole() {
        itemViewModel.getCommunityDetail().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .firstOrError()
                .doOnSuccess {
                    if (it.isJoined()) {
                        itemViewModel.isModerator.set(false)
                    } else {
                        val intent = Intent(
                                context,
                                EkoCommunityHomePageActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        (context as AppCompatActivity).startActivity(intent)
                    }
                }.doOnError {
                    Log.e("EkoCommBaseViewHolder", "checkUserRole: ${it.localizedMessage}")
                }.subscribe()
    }
}