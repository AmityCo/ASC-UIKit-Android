package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.exception.EkoError
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.file.EkoFile
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.base.EkoCustomToast
import com.ekoapp.ekosdk.uikit.base.SpacesItemDecoration
import com.ekoapp.ekosdk.uikit.common.FileManager
import com.ekoapp.ekosdk.uikit.common.FileUtils
import com.ekoapp.ekosdk.uikit.common.views.dialog.EkoAlertDialogFragment
import com.ekoapp.ekosdk.uikit.common.views.dialog.EkoBottomSheetDialogFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentPostDetailBinding
import com.ekoapp.ekosdk.uikit.community.domain.model.FileAttachment
import com.ekoapp.ekosdk.uikit.community.newsfeed.activity.*
import com.ekoapp.ekosdk.uikit.community.newsfeed.adapter.EkoPostViewFileAdapter
import com.ekoapp.ekosdk.uikit.community.newsfeed.adapter.PostImageItemAdapter
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.*
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FileUploadState
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoPostDetailsViewModel
import com.ekoapp.ekosdk.uikit.community.utils.EXTRA_PARAM_NEWS_FEED
import com.ekoapp.ekosdk.uikit.community.utils.EXTRA_PARAM_NEWS_FEED_ID
import com.ekoapp.ekosdk.uikit.community.utils.EkoCommunityNavigation
import com.ekoapp.ekosdk.uikit.community.utils.EkoSharePostBottomSheetDialog
import com.ekoapp.ekosdk.uikit.feed.settings.IPostShareClickListener
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.user.EkoUser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_comment_compose_bar.*
import kotlinx.android.synthetic.main.amity_fragment_post_detail.*
import kotlinx.android.synthetic.main.amity_item_footer_news_feed.*
import kotlinx.android.synthetic.main.amity_item_header_news_feed.*
import org.bson.types.ObjectId
import java.util.concurrent.TimeUnit

class EkoPostDetailFragment internal constructor() : EkoBaseFragment(),
        IPostImageItemClickListener,
        INewsFeedCommentShowMoreActionListener,
        INewsFeedActionShareListener,
        INewsFeedCommentItemClickListener,
        INewsFeedActionLikeListener,
        IPostFileItemClickListener {

    private val TAG = EkoPostDetailsActivity::class.java.canonicalName
    private val ID_MENU_ITEM = 222
    private lateinit var newsFeed: EkoPost
    private var attachmentAdapter: EkoPostViewFileAdapter? = null
    lateinit var mViewModel: EkoPostDetailsViewModel
    lateinit var mBinding: AmityFragmentPostDetailBinding
    private var disposal: CompositeDisposable = CompositeDisposable()
    private var commentToExpand: EkoComment? = null
    private var feedId: String? = null

    private var commentActionIndex: Int? = null
    private lateinit var itemDecor: SpacesItemDecoration
    lateinit var imageAdapter: PostImageItemAdapter
    private var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedId = arguments?.getString(EXTRA_PARAM_NEWS_FEED_ID)
        commentToExpand = arguments?.getParcelable(EXTRA_PARAM_COMMENT)
        itemDecor = SpacesItemDecoration(0, 0, 0, resources.getDimensionPixelSize(R.dimen.eight))
        imageAdapter = PostImageItemAdapter(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(requireActivity()).get(EkoPostDetailsViewModel::class.java)
        mBinding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.amity_fragment_post_detail,
                        container,
                        false
                )
        mBinding.lifecycleOwner = viewLifecycleOwner

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        getPostDetails(feedId)
        subscribeUiEvent()
    }

    private fun initEkoPostCommentRecyclerview() {
        newsFeedFooter.setCommentActionListener(this, null, this)
        commentToExpand?.getCommentId()?.let(
                newsFeedFooter::setPreExpandComment
        )

        feedId?.let {
            val disposable = mViewModel.getComments(it)
                    .distinctUntilChanged()
                    .throttleLatest(1, TimeUnit.SECONDS, true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ commentList ->
                        newsFeedFooter.submitComments(commentList)
                    }, { })
            disposal.add(disposable)
        }
    }

    override fun onDestroy() {
        if (!disposal.isDisposed) {
            disposal.dispose()
        }
        super.onDestroy()
    }

    private fun initAttachmentsAdapter(attachments: List<EkoFile>) {
        attachmentAdapter = EkoPostViewFileAdapter(this)
        rvNewsFeed.removeItemDecoration(itemDecor)
        rvNewsFeed.addItemDecoration(itemDecor)

        rvNewsFeed.layoutManager = LinearLayoutManager(requireContext())
        rvNewsFeed.adapter = attachmentAdapter

        attachmentAdapter?.setItems(mapEkoFilesToFileAttachments(attachments))
    }

    private fun initImageAdapter(images: List<EkoImage>) {
        rvNewsFeed.removeItemDecoration(itemDecor)
        rvNewsFeed.addItemDecoration(itemDecor)

        val layoutManager = GridLayoutManager(requireContext(), 12)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (images.size) {
                    1 -> 12 //in case single image it takes full row
                    2 -> 6 //in case two image it takes each item take half of the row
                    3 -> if (position == 0) 12 else 6
                    else -> if (position == 0) 12 else 4
                }
            }
        }
        rvNewsFeed.layoutManager = layoutManager
        rvNewsFeed.adapter = imageAdapter
        imageAdapter.setItems(images)
    }

    private fun getPostDetails(id: String?) {
        id?.let {
            initEkoPostCommentRecyclerview()
            disposal.add(mViewModel.getPostDetails(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { showErrorMessage(it.message) }
                    .subscribe { result ->
                        run {
                            newsFeed = result
                            mViewModel.newsFeed = newsFeed
                            initView()
                        }
                    })
        }
    }

    private fun showErrorMessage(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun initView() {
        val postChildren = newsFeed.getChildren()
        if (postChildren.isNotEmpty()) {
            when (postChildren.first().getData()) {
                is EkoPost.Data.IMAGE -> {
                    postChildren.mapNotNull { (it.getData() as? EkoPost.Data.IMAGE)?.getImage() }
                            .also(this::initImageAdapter)
                }
                is EkoPost.Data.FILE -> {
                    postChildren.mapNotNull { (it.getData() as? EkoPost.Data.FILE)?.getFile() }
                            .also(this::initAttachmentsAdapter)
                }
            }
        }

        btnPost.setOnClickListener {
            feedId?.let { postId ->
                val commentId = ObjectId.get().toHexString()
                val disposable =
                        mViewModel.addComment(commentId, postId, etPostComment.text.toString())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSuccess {
                                    //TODO remove after sdk (core) fix bug for fetch post data
                                    mViewModel.fetchPostData(postId)
                                }
                                .doOnError {
                                    if (EkoError.from(it) == EkoError.BAN_WORD_FOUND) {
                                        mViewModel.deleteComment(commentId).subscribe()
                                    }
                                }
                                .subscribe()
                disposal.add(disposable)
            }

            etPostComment.text?.clear()
            newsFeedFooter.notifyCommentAdded()
            hideKeyboard()
            topLayout.fullScroll(View.FOCUS_DOWN)
        }

        mainLayout.setOnClickListener {
            hideKeyboard()
        }

        newsFeedHeader.setNewsFeedActionAvatarClickListener(object :
                INewsFeedActionAvatarClickListener {
            override fun onClickUserAvatar(user: EkoUser) {
                if (mViewModel.avatarClickListener != null) {
                    mViewModel.avatarClickListener?.onClickUserAvatar(user)
                } else {
                    EkoCommunityNavigation.navigateToUserProfile(requireContext(), user.getUserId())
                }
            }
        })

        initFeedDetails()
        initPostDetailsViewButton()
        initUserData()
        //TODO check other condition
        if (mViewModel.isReadOnlyPage()) {
            setupViewReadOnlyMode()
        }

        parentLayout.visibility = View.VISIBLE
        commentComposeBar.setCommentExpandClickListener(View.OnClickListener {
            hideKeyboard()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_PARAM_NEWS_FEED, newsFeed)
            bundle.putString(EXTRA_PARAM_COMMENT_TEXT, etPostComment.text.toString())
            ekoAddCommentContract.launch(bundle)
        })

    }

    private fun setupViewReadOnlyMode() {
        composeBarDivider.visibility = View.GONE
        commentComposeBar.visibility = View.GONE
    }

    private fun initUserData() {
        commentComposeBar.setImageUrl(mViewModel.getProfilePicture())
    }

    private fun initPostDetailsViewButton() {
        btnFeedAction.visibility = View.GONE

        cbComment.setOnClickListener {
            openKeyBoardToAddComment()
        }

        tvNumberOfComments.setOnClickListener {
            openKeyBoardToAddComment()
        }
    }

    private fun initFeedDetails() {
        newsFeedHeader.setFeed(newsFeed, null)
        newsFeedHeader.showFeedAction(false)
        newsFeedFooter.setFeed(newsFeed)
        newsFeedFooter.setFeedLikeActionListener(this)
        newsFeedFooter.setFeedShareActionListener(this)
        tvFeed.text = mViewModel.getPostText(newsFeed)
        tvFeed.visibility = if (tvFeed.text.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openKeyBoardToAddComment() {
        if (mViewModel.isReadOnlyPage())
            return
        etPostComment.requestFocus()
        showKeyboard()
    }

    private fun showKeyboard() {
        if (context == null)
            return
        val imm: InputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etPostComment, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        activity?.currentFocus?.let {
            val inputMethodManager =
                    ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun showFeedAction() {
        mViewModel.feedShowMoreActionClicked(newsFeed)
    }

    private fun handleFeedActionItemClick(item: MenuItem) {
        when (item.itemId) {
            R.id.actionEditPost -> {
                postEditContact.launch(newsFeed)
            }
            R.id.actionDeletePost -> {
                showDeletePostWarning()
            }
            R.id.actionReportPost -> {
                sendReportPost(newsFeed, true)
            }
            R.id.actionUnreportPost -> {
                sendReportPost(newsFeed, false)
            }
        }
    }

    private fun showDeletePostWarning() {
        val deleteConfirmationDialogFragment = EkoAlertDialogFragment
                .newInstance(
                        R.string.amity_delete_post_title,
                        R.string.amity_delete_post_warning_message,
                        R.string.amity_delete,
                        R.string.amity_cancel
                )
        deleteConfirmationDialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        deleteConfirmationDialogFragment.listener =
                object : EkoAlertDialogFragment.IAlertDialogActionListener {
                    override fun onClickPositiveButton() {
                        deletePost()
                    }

                    override fun onClickNegativeButton() {

                    }
                }
    }

    private fun showDeleteCommentWarning(comment: EkoComment) {
        val deleteConfirmationDialogFragment = EkoAlertDialogFragment
                .newInstance(
                        R.string.amity_delete_comment_title,
                        R.string.amity_delete_comment_warning_message,
                        R.string.amity_delete,
                        R.string.amity_cancel)
        deleteConfirmationDialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        deleteConfirmationDialogFragment.listener =
                object : EkoAlertDialogFragment.IAlertDialogActionListener {
                    override fun onClickPositiveButton() {
                        deleteComment(comment)
                    }

                    override fun onClickNegativeButton() {
                        commentActionIndex = null
                    }
                }
    }

    private fun deleteComment(comment: EkoComment) {
        disposal.add(mViewModel.deleteComment(comment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //TODO remove after sdk (core) fix bug for fetch post data
                    feedId?.let(mViewModel::fetchPostData)
                }
                .doOnError {}
                .subscribe { commentDeleted() })
    }

    private fun deletePost() {
        disposal.add(mViewModel
                .deletePost(newsFeed)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnComplete {
                    run {
                        backPressFragment()
                    }
                }.doOnError {
                    showErrorMessage(it.message)
                }.subscribe()
        )
    }

    private fun sendReportComment(comment: EkoComment, isReport: Boolean) {
        val viewModel = if (isReport) {
            mViewModel.reportComment(comment)
        } else {
            mViewModel.unreportComment(comment)
        }
        disposal.add(viewModel
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.d(TAG, it.message ?: "")
                }
                .doOnComplete {
                    showReportSentMessage(isReport)
                }
                .subscribe())
    }

    private fun sendReportPost(feed: EkoPost, isReport: Boolean) {
        val viewModel = if (isReport) {
            mViewModel.reportPost(feed)
        } else {
            mViewModel.unreportPost(feed)
        }
        disposal.add(viewModel
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.d(TAG, it.message ?: "")
                }
                .doOnComplete {
                    showReportSentMessage(isReport)
                }
                .subscribe())
    }

    private fun showReportSentMessage(isReport: Boolean) {
        val messageSent = if (isReport) {
            R.string.amity_report_sent
        } else {
            R.string.amity_unreport_sent
        }
        EkoCustomToast.showMessage(
                parentLayout,
                requireContext(),
                layoutInflater,
                getString(messageSent)
        )
    }

    /* override fun leftIconClick() {
         //TODO look for Better solution
         activity?.setResult(AppCompatActivity.RESULT_OK)
         activity?.finish()
     }*/

    override fun onClickItem(position: Int) {
        if (context == null)
            return
        val images =
                newsFeed.getChildren()
                        .map { it.getData() as? EkoPost.Data.IMAGE }
                        .mapNotNull { it?.getImage() }
        EkoCommunityNavigation.navigateToImagePreview(requireContext(), images, position)
    }

    override fun onClickItem(comment: EkoComment, position: Int) {
        hideKeyboard()
    }

    override fun onClickAvatar(user: EkoUser) {
        EkoCommunityNavigation.navigateToUserProfile(requireContext(), user.getUserId())
    }

    override fun onClickNewsFeedCommentShowMoreAction(comment: EkoComment, position: Int) {
        commentActionIndex = position
        mViewModel.commentShowMoreActionClicked(newsFeed, comment)
    }

    private fun subscribeUiEvent() {
        mViewModel.onEventReceived += { event ->
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

    private fun showCommentActionAdmin(ekoComment: EkoComment) {
        val menu =
                if (ekoComment.isFlaggedByMe()) {
                    R.menu.eko_commnet_action_menu_admin_with_unreport
                } else {
                    R.menu.eko_commnet_action_menu_admin
                }

        val fragment = EkoBottomSheetDialogFragment.newInstance(menu)
        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
                EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleCommentActionItemClick(item, ekoComment)
            }
        })
    }

    private fun showFeedActionByOwner(feed: EkoPost) {
        val fragment = EkoBottomSheetDialogFragment.newInstance(R.menu.eko_feed_action_menu_owner)
        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
                EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleFeedActionItemClick(item)
            }
        })
    }

    private fun showFeedActionByOtherUser(feed: EkoPost) {
        val menu =
                if (feed.isFlaggedByMe) {
                    R.menu.eko_feed_action_menu_unreport_post
                } else {
                    R.menu.eko_feed_action_menu_report_post
                }

        val fragment = EkoBottomSheetDialogFragment.newInstance(menu)
        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
                EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleFeedActionItemClick(item)
            }
        })
    }

    private fun showFeedActionByAdmin(feed: EkoPost) {
        //TODO better solution for menu
        val menu =
                if (feed.isFlaggedByMe) {
                    R.menu.eko_feed_action_menu_admin_with_unreport
                } else {
                    R.menu.eko_feed_action_menu_admin
                }

        val fragment = EkoBottomSheetDialogFragment.newInstance(menu)
        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
                EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleFeedActionItemClick(item)
            }
        })
    }

    private fun showCommentActionCommentOwner(ekoComment: EkoComment) {
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
        val menu =
                if (ekoComment.isFlaggedByMe()) {
                    R.menu.eko_comment_action_menu_unreport
                } else {
                    R.menu.eko_comment_action_menu_report
                }

        val fragment = EkoBottomSheetDialogFragment.newInstance(menu)
        fragment.show(childFragmentManager, EkoBottomSheetDialogFragment.toString())
        fragment.setOnNavigationItemSelectedListener(object :
                EkoBottomSheetDialogFragment.OnNavigationItemSelectedListener {
            override fun onItemSelected(item: MenuItem) {
                handleCommentActionItemClick(item, ekoComment)
            }
        })
    }

    private fun mapEkoFilesToFileAttachments(ekoFile: List<EkoFile>): List<FileAttachment> {
        return ekoFile.map {
            val fileSize = it.getFileSize()?.toLong() ?: 0L
            FileAttachment(
                    id = it.getFileId(),
                    uploadId = null,
                    name = it.getFileName() ?: "",
                    size = fileSize,
                    uri = Uri.parse(it.getUrl()),
                    readableSize = FileUtils.humanReadableByteCount(fileSize, true)!!,
                    mimeType = it.getMimeType() ?: "",
                    uploadState = FileUploadState.COMPLETE,
                    progress = 100
            )
        }
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
                sendReportComment(ekoComment, true)
            }
            R.id.actionUnreportComment -> {
                sendReportComment(ekoComment, false)
            }
        }
    }

    private fun commentUpdated() {
        newsFeedFooter.notifyCommentUpdated(commentActionIndex!!)
        commentActionIndex = null

    }

    private fun commentDeleted() {
        newsFeedFooter.notifyCommentDeleted(commentActionIndex!!)
        commentActionIndex = null
    }

    private var postEditContact =
            registerForActivityResult(EkoEditPostActivity.EkoEditPostActivityContract()) {
                if (it != null) {
                    getPostDetails(it)
                }
            }

    private var editCommentContact =
            registerForActivityResult(EkoEditCommentActivity.EkoEditCommentActivityContract()) {
                commentActionIndex?.let {
                    commentUpdated()
                }
            }

    private var ekoAddCommentContract =
            registerForActivityResult(EkoEditCommentActivity.EkoAddCommentActivityContract()) {
                if (it != null && it == true) {
                    etPostComment.setText("")
                    Handler(Looper.getMainLooper()).postDelayed({
                        newsFeedFooter.scrollToBottomComments()
                        topLayout.fullScroll(View.FOCUS_DOWN)
                    }, 200)
                }
            }

    override fun onLikeAction(liked: Boolean) {
        disposal.add(mViewModel.postReaction(liked, newsFeed).doOnError {
            Log.d(TAG, it.message ?: "")
        }.subscribe())
    }

    override fun onShareAction() {
        EkoSharePostBottomSheetDialog(newsFeed)
                .setNavigationListener(mViewModel)
                .observeShareToMyTimeline(this) {
                    mViewModel.postShareClickListener?.shareToMyTimeline(requireContext(), it)
                }
                .observeShareToGroup(this) {
                    mViewModel.postShareClickListener?.shareToGroup(requireContext(), it)
                }
                .observeShareToExternalApp(this) {
                    mViewModel.postShareClickListener?.shareToExternal(requireContext(), it)
                }
                .show(childFragmentManager)
    }

    override fun onClickFileItem(file: FileAttachment) {
        if (context == null) {
            return
        }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!mViewModel.isReadOnlyPage()) {
            val drawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.amity_ic_more_horiz)
            drawable?.mutate()
            drawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    R.color.amityColorBlack, BlendModeCompat.SRC_ATOP
            )
            menuItem = menu.add(
                    Menu.NONE,
                    ID_MENU_ITEM,
                    Menu.NONE,
                    getString(R.string.amity_cancel)
            )
            menuItem?.setIcon(drawable)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == ID_MENU_ITEM) {
            showFeedAction()
        }
        return super.onOptionsItemSelected(item)
    }


    class Builder {
        private var postId: String? = null
        private var comment: EkoComment? = null
        private var avatarClickListener: IAvatarClickListener? = null
        private var postShareClickListener: IPostShareClickListener? = null

        fun build(activity: AppCompatActivity): EkoPostDetailFragment {
            if (postId == null) {
                throw IllegalArgumentException("Post id is required")
            }

            val fragment = EkoPostDetailFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoPostDetailsViewModel::class.java)
            fragment.mViewModel.avatarClickListener = avatarClickListener

            if (postShareClickListener != null) {
                fragment.mViewModel.postShareClickListener = postShareClickListener
            }

            fragment.arguments = Bundle().apply {
                putString(EXTRA_PARAM_NEWS_FEED_ID, this@Builder.postId)
                putParcelable(EXTRA_PARAM_COMMENT, this@Builder.comment)
            }
            return fragment
        }

        fun postId(postId: String): Builder {
            this.postId = postId
            return this
        }

        fun post(post: EkoPost): Builder {
            this.postId = post.getPostId()
            return this
        }

        fun onClickUserAvatar(onAvatarClickListener: IAvatarClickListener): Builder {
            return apply { this.avatarClickListener = onAvatarClickListener }
        }

        fun postShareClickListener(onPostShareClickListener: IPostShareClickListener): Builder {
            return apply { this.postShareClickListener = onPostShareClickListener }
        }

        //TODO uncomment after reply integration
        private fun commentToExpand(comment: EkoComment): Builder {
            this.comment = comment
            return this
        }
    }
}