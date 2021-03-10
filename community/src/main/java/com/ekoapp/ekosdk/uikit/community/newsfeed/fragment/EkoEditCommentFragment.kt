package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.exception.EkoError
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.common.views.dialog.EkoAlertDialogFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentEditCommentBinding
import com.ekoapp.ekosdk.uikit.community.newsfeed.activity.EkoEditCommentActivity
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoEditCommentViewModel
import com.ekoapp.ekosdk.uikit.utils.EkoOptionMenuColorUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.bson.types.ObjectId

class EkoEditCommentFragment internal constructor(
    private val ekoPost: EkoPost?,
    private val ekoComment: EkoComment?,
    private val reply: EkoComment?,
    private val commentText: String?
) : EkoBaseFragment(), EkoAlertDialogFragment.IAlertDialogActionListener {
    private val ID_MENU_ITEM_ADD_COMMENT: Int = 144
    private var menuItemComment: MenuItem? = null
    private val TAG = EkoEditCommentActivity::class.java.canonicalName

    private val mViewModel: EkoEditCommentViewModel by activityViewModels()
    lateinit var mBinding: AmityFragmentEditCommentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeBackPress = true
        setupInitialData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.amity_fragment_edit_comment,
                container,
                false
            )
        mBinding.lifecycleOwner = viewLifecycleOwner
        mBinding.vm = mViewModel
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupToolbar()
        addEditCommentViewTextWatcher()
    }

    private fun setupInitialData() {
        mViewModel.setComment(ekoComment)
        mViewModel.setPost(ekoPost)
        mViewModel.setReplyTo(reply)

        if (!commentText.isNullOrEmpty())
            mViewModel.setCommentData(commentText)
    }

    private fun addEditCommentViewTextWatcher() {
        mViewModel.commentText.observe(viewLifecycleOwner, Observer {
            mViewModel.checkForCommentUpdate()
        })

        mViewModel.hasCommentUpdate.observe(viewLifecycleOwner, Observer {
            updateCommentMenu(it)
        })
    }


    private fun setupToolbar() {
        mBinding.showReplyingTo = false
        if (mViewModel.editMode()) {
            if (mViewModel.getReply() != null) {
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.amity_edit_reply)
            } else {
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.amity_edit_comment)
            }
        } else {
            if (mViewModel.getReply() != null) {
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.amity_reply_to)
                mBinding.replyingToUser = mViewModel.getReply()?.getUser()?.getDisplayName()
                mBinding.showReplyingTo = true
            } else {
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.amity_add_comment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuItemComment =
            menu.add(Menu.NONE, ID_MENU_ITEM_ADD_COMMENT, Menu.NONE, getMenuItemCommentTitle())
        menuItemComment?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        updateCommentMenu(mViewModel.hasCommentUpdate.value ?: false)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getMenuItemCommentTitle(): String {
        return if (mViewModel.editMode())
            getString(R.string.amity_save)
        else
            getString(R.string.amity_post_caps)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == ID_MENU_ITEM_ADD_COMMENT) {
            updateCommentMenu(false)
            if (mViewModel.editMode()) {
                updateComment()
            } else {
                addComment()
            }
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateCommentMenu(enabled: Boolean) {
        if (menuItemComment != null) {
            menuItemComment?.isEnabled = enabled
            val s = SpannableString(menuItemComment?.title)
            s.setSpan(
                ForegroundColorSpan(
                    EkoOptionMenuColorUtil.getColor(
                        menuItemComment?.isEnabled ?: false,
                        requireContext()
                    )
                ), 0, s.length, 0
            )
            menuItemComment?.title = s
        }
    }


    override fun handleBackPress() {
        handleCancelPost()
    }

    private fun handleCancelPost() {
        if (mViewModel.hasCommentUpdate.value!!) {
            showExitConfirmationDialog()
        } else {
            backPressFragment()
        }
    }

    private fun showExitConfirmationDialog() {
        val isReply = mViewModel.getComment()?.getParentId()?.isNotEmpty() == true
        val exitConfirmationDialogFragment = if (mViewModel.getReply() != null || isReply) {
            EkoAlertDialogFragment
                .newInstance(
                    R.string.amity_discard_reply_title, R.string.amity_discard_reply_message,
                    R.string.amity_discard, R.string.amity_cancel
                )
        } else {
            EkoAlertDialogFragment
                .newInstance(
                    R.string.amity_discard_comment_title, R.string.amity_discard_comment_message,
                    R.string.amity_discard, R.string.amity_cancel
                )
        }
        exitConfirmationDialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        exitConfirmationDialogFragment.listener = this
    }

    private fun updateComment() {
        mViewModel.updateComment()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnSuccess {
                activity?.setResult(AppCompatActivity.RESULT_OK)
                backPressFragment()
            }
            ?.doOnError {
                updateCommentMenu(true)
                Log.d(TAG, it.message ?: "")
                val isReply = mViewModel.getComment()?.getParentId()?.isNotEmpty() == true
                if (isReply) {
                    Toast.makeText(
                        activity,
                        getString(R.string.amity_update_reply_error_message),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.amity_update_comment_error_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            ?.subscribe()
    }

    private fun addComment() {
        val commentId = ObjectId.get().toHexString()
        val addComment = mViewModel.addComment(commentId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ comment ->
                activity?.setResult(AppCompatActivity.RESULT_OK)
                backPressFragment()
            }, {
                if (EkoError.from(it) == EkoError.BAN_WORD_FOUND) {
                    mViewModel.deleteComment(commentId).subscribe()
                }
                updateCommentMenu(true)
                Log.d(TAG, it.message ?: "")
                if (mViewModel.getReply() != null) {
                    Toast.makeText(
                        activity,
                        getString(R.string.amity_add_reply_error_message),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.amity_add_comment_error_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        addComment?.let { disposable.add(addComment) }
    }

    override fun onClickPositiveButton() {
        backPressFragment()
    }

    override fun onClickNegativeButton() {
        Log.d(TAG, " Cancel discard comment")
    }

    class Builder {
        private var ekoComment: EkoComment? = null
        private var ekoPost: EkoPost? = null
        private var commentText: String? = null
        private var reply: EkoComment? = null

        fun build(activity: AppCompatActivity): EkoEditCommentFragment {
            return EkoEditCommentFragment(ekoPost, ekoComment, reply, commentText)
        }

        fun setNewsFeed(post: EkoPost?): Builder {
            this.ekoPost = post
            return this
        }

        fun setComment(comment: EkoComment?): Builder {
            this.ekoComment = comment
            return this
        }

        fun setReplyTo(reply: EkoComment?): Builder {
            this.reply = reply
            return this
        }

        fun setCommentText(comment: String?): Builder {
            this.commentText = comment
            return this
        }
    }
}