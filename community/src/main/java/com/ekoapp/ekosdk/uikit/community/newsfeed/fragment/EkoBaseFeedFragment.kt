package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.internal.api.socket.request.EkoSocketException
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.base.EkoCustomToast
import com.ekoapp.ekosdk.uikit.common.FileManager
import com.ekoapp.ekosdk.uikit.common.views.dialog.EkoAlertDialogFragment
import com.ekoapp.ekosdk.uikit.common.views.dialog.EkoBottomSheetDialogFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.FragmentEkoBaseFeedBinding
import com.ekoapp.ekosdk.uikit.community.domain.model.FileAttachment
import com.ekoapp.ekosdk.uikit.community.newsfeed.activity.EkoEditCommentActivity
import com.ekoapp.ekosdk.uikit.community.newsfeed.adapter.EkoNewsFeedAdapter
import com.ekoapp.ekosdk.uikit.community.newsfeed.adapter.EkoPostViewFileAdapter
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedImageClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedItemActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.IPostFileItemClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.NewsFeedEvents
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoBaseFeedViewModel
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoNewsFeedViewModel
import com.ekoapp.ekosdk.uikit.community.utils.EkoCommunityNavigation
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_eko_base_feed.*

abstract class EkoBaseFeedFragment : EkoBaseFragment(), INewsFeedImageClickListener,
    INewsFeedItemActionListener,
    EkoPostViewFileAdapter.ILoadMoreFilesClickListener,
    IPostFileItemClickListener {
    private val TAG = EkoBaseFeedFragment::class.java.canonicalName
    private lateinit var adapter: EkoNewsFeedAdapter
    private lateinit var itemDecorSpace: EkoRecyclerViewItemDecoration
    private lateinit var mBinding: FragmentEkoBaseFeedBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemDecorSpace =
            EkoRecyclerViewItemDecoration(resources.getDimensionPixelSize(R.dimen.twenty_four))
        initNewsFeedAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_eko_base_feed, container, false)
        return mBinding.root
    }

    fun getRootView(): ViewGroup = mBinding.emptyViewContainer

    abstract fun getFeedType(): EkoTimelineType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNewsFeedRecyclerView()
        initCreateFeed()
        subscribeUiEvent()
    }

    private fun subscribeUiEvent() {
        getViewModel().onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.SHOW_COMMENT_ACTION_BY_COMMENT_OWNER -> {
                    showCommentActionCommentOwner(event.dataObj as EkoComment)
                }
                EventIdentifier.SHOW_COMMENT_ACTION_BY_ADMIN -> {
                    showCommentActionAdmin(event.dataObj as EkoComment)
                }
                EventIdentifier.SHOW_COMMENT_ACTION_BY_OTHER_USER -> {
                    showCommentActionByOtherUser(event.dataObj as EkoComment)
                }
                EventIdentifier.SHOW_FEED_ACTION_BY_OTHER_USER -> {
                    showFeedActionByOtherUser(event.dataObj as EkoPost)

                }
                EventIdentifier.SHOW_FEED_ACTION_BY_FEED_OWNER -> {
                    showFeedActionByOwner(event.dataObj as EkoPost)

                }
                EventIdentifier.SHOW_FEED_ACTION_BY_ADMIN -> {
                    showFeedActionByAdmin(event.dataObj as EkoPost)
                }
                else -> {

                }
            }
        }
    }

    abstract fun getViewModel(): EkoBaseFeedViewModel

    private fun getFeeds() {
        val disposable = getViewModel().getFeed()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnError {
                showGetFeedErrorMessage(it.message)
            }
            ?.subscribe { result ->
                Log.d(TAG, "submit list")
                adapter.submitList(result)
                handleEmptyList(result.isEmpty())
                if (NewsFeedEvents.newPostCreated) {
                    Log.d(TAG, "scroll to top")
                    Handler().postDelayed(Runnable {
                        rvNewsFeed.smoothScrollToPosition(0)
                        NewsFeedEvents.newPostCreated = false
                    }, 200)
                }

            }


        disposable?.let {
            this.disposable.add(it)
        }
    }

    private fun showGetFeedErrorMessage(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    open fun handleEmptyList(isListEmpty: Boolean) {
        if (isListEmpty) {
            if (context != null) {
                if (mBinding.emptyViewContainer.childCount == 0) {
                    mBinding.emptyViewContainer.addView(getEmptyView())
                }
                mBinding.emptyViewContainer.visibility = View.VISIBLE
            }
        } else {
            mBinding.emptyViewContainer.visibility = View.GONE
        }
    }


    abstract fun getEmptyView(): View

    private fun initCreateFeed() {
        /*fabCreatePost.visibility = if (showCreatePost()) View.VISIBLE else View.GONE
        fabCreatePost.setOnClickListener {
            onClickCreatePost()
        }*/
    }


    private fun initNewsFeedAdapter() {
        adapter =
            EkoNewsFeedAdapter(getFeedType(), this, this, this, this)
        adapter.setHasStableIds(true)

    }

    private fun initNewsFeedRecyclerView() {
        rvNewsFeed.layoutManager = LinearLayoutManager(requireContext())
        rvNewsFeed.adapter = adapter
        rvNewsFeed.addItemDecoration(itemDecorSpace)
        rvNewsFeed.setHasFixedSize(true)
        getFeeds()
    }

    override fun loadMoreFiles(post: EkoPost) {
        EkoCommunityNavigation.navigateToPostDetails(
            requireContext(),
            post.getPostId(),
            getFeedType()
        )
    }

    override fun onClickImage(images: List<EkoImage>, position: Int) {
        EkoCommunityNavigation.navigateToImagePreview(requireContext(), images, position)
    }

    override fun onFeedAction(feed: EkoPost, position: Int) {
        if (getViewModel().postOptionClickListener != null) {
            getViewModel().postOptionClickListener!!.onClickPostOption(feed)
        } else {
            getViewModel().feedShowMoreActionClicked(feed)
        }

    }

    override fun onCommentAction(feed: EkoPost, comment: EkoComment, position: Int) {
        getViewModel().commentShowMoreActionClicked(feed, comment)
    }

    override fun showAllReply(feed: EkoPost, comment: EkoComment, position: Int) {
        EkoCommunityNavigation.navigateToPostDetails(requireContext(), feed, comment, getFeedType())
    }

    override fun onClickItem(post: EkoPost, position: Int) {
        if (getViewModel().postItemClickListener != null) {
            getViewModel().postItemClickListener!!.onClickPostItem(post)
        } else
            EkoCommunityNavigation.navigateToPostDetails(
                requireContext(),
                post.getPostId(),
                getFeedType()
            )
    }

    override fun onClickFileItem(file: FileAttachment) {
        handleOpenFile(file)
    }

    override fun onClickCommunity(community: EkoCommunity) {
        if (context != null)
            EkoCommunityNavigation.navigateToCommunityDetails(requireContext(), community)
    }

    private fun showFeedActionByOwner(feed: EkoPost) {
        if (isAdded) {
            val fragment =
                EkoBottomSheetDialogFragment.newInstance(R.menu.eko_feed_action_menu_owner)
            fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
            fragment.setOnNavigationItemSelectedListener(object :
                EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
                override fun onItemSelected(item: MenuItem) {
                    handleFeedActionItemClick(item, feed)
                }

            })
        }
    }

    private fun showFeedActionByOtherUser(feed: EkoPost) {
        if (isAdded) {
            val menu =
                if (feed.isFlaggedByMe) R.menu.eko_feed_action_menu_already_reported else R.menu.eko_feed_action_menu_report

            val fragment =
                EkoBottomSheetDialogFragment.newInstance(menu)

            fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
            fragment.setOnNavigationItemSelectedListener(object :
                EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
                override fun onItemSelected(item: MenuItem) {
                    handleFeedActionItemClick(item, feed)
                }

            })

        }
    }

    private fun showFeedActionByAdmin(feed: EkoPost) {
        var menu =
            if (feed.isFlaggedByMe) R.menu.eko_feed_action_menu_admin_with_already_reported else R.menu.eko_feed_action_menu_admin
        val fragment = EkoBottomSheetDialogFragment.newInstance(menu)
        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
            EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleFeedActionItemClick(item, feed)
            }

        })
    }

    private fun handleFeedActionItemClick(
        item: MenuItem,
        feed: EkoPost
    ) {
        if (item.itemId == R.id.actionEditPost) {
            EkoCommunityNavigation.navigateToEditPost(requireContext(), feed)
        } else if (item.itemId == R.id.actionDeletePost) {
            showDeletePostWarning(feed)

        } else if (item.itemId == R.id.actionReportPost) {
            sendReport(feed)
        }
    }

    private fun sendReport(feed: EkoPost) {
        disposable.add(
            getViewModel()
                .reportPost(feed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.d(TAG, it.message)
                }
                .doOnComplete {
                    showReportSentMessage()
                }
                .subscribe())
    }

    private fun showReportSentMessage() {
        if (activity?.applicationContext != null) {
            view?.let {
                EkoCustomToast.showMessage(
                    it,
                    requireActivity().applicationContext,
                    layoutInflater,
                    getString(R.string.report_sent)
                )
            }

        }
    }

    private fun deletePost(post: EkoPost) {
        disposable.add(
            getViewModel().deletePost(post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.d(TAG, it.message)
                    if (it is EkoSocketException)
                        showConnectivityIssue()
                    else
                        showFailedToDeleteMessage(it.message)
                }
                .doOnComplete {
                    refreshGlobalFeed()
                }
                .subscribe()
        )
    }

    private fun showConnectivityIssue() {
        Toast.makeText(activity, getString(R.string.connectivity_issue), Toast.LENGTH_LONG).show()
    }

    private fun refreshGlobalFeed() {
        EkoClient.newFeedRepository()
            .getGlobalFeed()
            .build()
            .query()
            .ignoreElements()
            .subscribeOn(Schedulers.io())
            .doOnError {
                // ignore error
            }
            .subscribe()
    }

    private fun showFailedToDeleteMessage(message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    private fun showDeletePostWarning(
        post: EkoPost
    ) {

        val exitConfirmationDialogFragment = EkoAlertDialogFragment
            .newInstance(
                R.string.delete_post_title, R.string.delete_post_warning_message,
                R.string.delete, R.string.cancel
            )
        exitConfirmationDialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        exitConfirmationDialogFragment.listener =
            object : EkoAlertDialogFragment.IAlertDialogActionListener {
                override fun onClickPositiveButton() {
                    deletePost(post)
                }

                override fun onClickNegativeButton() {

                }

            }

    }

    private fun showCommentActionCommentOwner(ekoComment: EkoComment) {
        if (!isAdded)
            return
        val fragment =
            EkoBottomSheetDialogFragment.newInstance(R.menu.eko_commnet_action_menu_comment_owner)
        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
            EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleCommentActionItemClick(item, ekoComment)
            }

        })

    }

    private fun showCommentActionByOtherUser(ekoComment: EkoComment) {
        if (!isAdded)
            return
        val menu =
            if (ekoComment.isFlaggedByMe()) R.menu.eko_comment_action_menu_already_reported
            else R.menu.eko_comment_action_menu_report
        val fragment =
            EkoBottomSheetDialogFragment.newInstance(menu)

        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
            EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleCommentActionItemClick(item, ekoComment)
            }

        })

    }

    private fun showCommentActionAdmin(ekoComment: EkoComment) {
        if (!isAdded)
            return
        val menu =
            if (ekoComment.isFlaggedByMe()) R.menu.eko_commnet_action_menu_admin_with_already_reported
            else R.menu.eko_commnet_action_menu_admin
        val fragment =
            EkoBottomSheetDialogFragment.newInstance(menu)

        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
            EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleCommentActionItemClick(item, ekoComment)
            }

        })
    }

    private fun handleCommentActionItemClick(item: MenuItem, ekoComment: EkoComment) {
        when (item.itemId) {
            R.id.actionEditComment -> {
                editCommentContact.launch(ekoComment)
            }
            R.id.actionDeleteComment -> {
                showDeleteCommentWarning(ekoComment)
            }
            R.id.actionReportComment -> {
                sendReport(ekoComment)
            }
        }
    }

    private fun sendReport(comment: EkoComment) {
        disposable.add(
            getViewModel()
                .reportComment(comment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.d(TAG, it.message)
                }
                .doOnComplete {
                    showReportSentMessage()
                }
                .subscribe())
    }

    private fun showDeleteCommentWarning(comment: EkoComment) {

        val exitConfirmationDialogFragment = EkoAlertDialogFragment
            .newInstance(
                R.string.delete_comment_title, R.string.delete_comment_warning_message,
                R.string.delete, R.string.cancel
            )
        exitConfirmationDialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        exitConfirmationDialogFragment.listener =
            object : EkoAlertDialogFragment.IAlertDialogActionListener {
                override fun onClickPositiveButton() {
                    deleteComment(comment)
                }

                override fun onClickNegativeButton() {

                }
            }

    }

    private fun deleteComment(comment: EkoComment) {
        disposable.add(
            getViewModel()
                .deleteComment(comment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    // to be handled
                }
                .subscribe()
        )
    }

    private fun handleOpenFile(file: FileAttachment) {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            context?.let {
                FileManager.saveFile(it, file.uri.toString(), file.name, file.mimeType)
            }
        } else {
            this.requestPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_STORAGE_PERMISSION_IMAGE_UPLOAD
            )
        }
    }

    override fun onLikeAction(liked: Boolean, ekoPost: EkoPost, position: Int) {
        disposable.add(getViewModel().postReaction(liked, ekoPost).doOnError {
            Log.d(TAG, it.message)
        }.subscribe())
    }


    private var editCommentContact =
        registerForActivityResult(EkoEditCommentActivity.EkoEditCommentActivityContract()) {
            Log.d(TAG, "comment edited")
        }

}