package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.base.SpacesItemDecoration
import com.ekoapp.ekosdk.uikit.common.FileUtils
import com.ekoapp.ekosdk.uikit.common.views.dialog.EkoAlertDialogFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.domain.model.FileAttachment
import com.ekoapp.ekosdk.uikit.community.explore.activity.EXTRA_PARAM_COMMUNITY
import com.ekoapp.ekosdk.uikit.community.newsfeed.adapter.EkoCreatePostFileAdapter
import com.ekoapp.ekosdk.uikit.community.newsfeed.adapter.EkoCreatePostImageAdapter
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.ICreatePostFileActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.ICreatePostImageActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FeedImage
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FileUploadState
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoCreatePostViewModel
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.EkoCameraUtil
import com.ekoapp.ekosdk.uikit.utils.EkoConstants
import com.ekoapp.ekosdk.uikit.utils.EkoOptionMenuColorUtil
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_activity_create_post.*
import java.io.File
import java.util.*


const val MAX_IMAGE_SELECTABLE = 10
const val MAX_FILE_SELECTABLE = 10
const val REQUEST_STORAGE_PERMISSION_IMAGE_UPLOAD = 100
const val REQUEST_STORAGE_PERMISSION_FILE_UPLOAD = 101
const val IMAGE_COUNT_SINGLE = 1
const val IMAGE_COUNT_DOUBLE = 2

abstract class EkoBaseCreatePostFragment : EkoBaseFragment(),
        ICreatePostImageActionListener, ICreatePostFileActionListener,
        EkoAlertDialogFragment.IAlertDialogActionListener {

    private val ID_MENU_ITEM_POST: Int = 133
    private var menuItemPost: MenuItem? = null
    protected val mViewModel: EkoCreatePostViewModel by activityViewModels()
    private val TAG = EkoPostCreateFragment::class.java.canonicalName
    protected var compositeDisposable: CompositeDisposable = CompositeDisposable()


    private var imageAdapter: EkoCreatePostImageAdapter? = null
    private var itemDecor: RecyclerView.ItemDecoration? = null
    private var fileAdapter: EkoCreatePostFileAdapter? = null

    private var photoFile: File? = null
    protected var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeBackPress = true
        mViewModel.community = arguments?.getParcelable(EXTRA_PARAM_COMMUNITY)
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.amity_fragment_post_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        addPostEditTextListener()
        setupComposeBar()
        observeImageData()
        observeFileAttachments()
        addViewModelListener()

        layoutPostAsCommunity.visibility = if (showPostAsCommunity()) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuItemPost =
                menu.add(Menu.NONE, ID_MENU_ITEM_POST, Menu.NONE, getString(R.string.amity_save))
        menuItemPost?.setTitle(getPostMenuText())
                ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        updatePostMenu(isRightButtonActive())
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == ID_MENU_ITEM_POST) {
            handlePostMenuItemClick()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    abstract fun handlePostMenuItemClick()

    private fun observeImageData() {
        mViewModel.getImages().observe(viewLifecycleOwner, Observer {
            setupImageAdapter()
            val imageCount = imageAdapter?.itemCount ?: 0
            imageAdapter!!.submitList(it.toMutableList())
            if (imageCount != it.size)
                imageAdapter?.notifyItemRangeChanged(0, it.size)
            handleButtonActiveInactiveBehavior()
        })
    }

    private fun observeFileAttachments() {
        mViewModel.getFiles().observe(viewLifecycleOwner, Observer {
            setupFileAttachmentAdapter()
            fileAdapter!!.submitList(it)
            handleButtonActiveInactiveBehavior()
        })
    }

    private fun showPostAsCommunity(): Boolean {
        //FIXME update logic
        return false
        //return !isEditMode() && mViewModel.hasAdminAccess()
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
    }

    protected fun showErrorMessage(error: String?) {
        Toast.makeText(activity?.applicationContext, error, Toast.LENGTH_LONG).show()
    }

    private fun setupComposeBar() {
        btnUploadPhotos.setOnClickListener {
            handleAddPhotos()
        }

        btnUploadAttachment.setOnClickListener {
            handleAddFiles()
        }

        btnTakePhoto.setOnClickListener {
            takePicture()
        }
    }

    private fun handleAddFiles() {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            openFilePicker()
        } else {
            requestPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    REQUEST_STORAGE_PERMISSION_IMAGE_UPLOAD
            )
        }
    }

    private fun openFilePicker() {
        val filesIntent = Intent(Intent.ACTION_GET_CONTENT)
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE)
        filesIntent.type = "*/*"

        startActivityForResult(filesIntent, EkoConstants.PICK_FILES)
    }

    private fun handleAddPhotos() {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            openImagePicker()
        } else {
            requestPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    REQUEST_STORAGE_PERMISSION_IMAGE_UPLOAD
            )
        }
    }

    protected fun hideComposeBar() {
        composeBar.visibility = View.GONE
    }

    private fun hidePostAsCommunity() {
        layoutPostAsCommunity.visibility = View.GONE
    }

    private fun setupImageAdapter() {
        if (imageAdapter == null || imageAdapter!!.itemCount == 0) {
            if (itemDecor != null) {
                rvAttachment.removeItemDecoration(itemDecor!!)
            }

            val space = resources.getDimensionPixelSize(R.dimen.eight)
            itemDecor = SpacesItemDecoration(0, 0, 0, space)

            imageAdapter = EkoCreatePostImageAdapter(this)
            rvAttachment.addItemDecoration(itemDecor!!)
            val layoutManager = GridLayoutManager(context, 6)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (imageAdapter?.itemCount()) {
                        IMAGE_COUNT_SINGLE -> 6 //in case single image it takes full row
                        IMAGE_COUNT_DOUBLE -> {
                            3 //in case two image it takes each item take half of the row
                        }
                        else -> 2
                    }
                }
            }

            rvAttachment.layoutManager = layoutManager
            rvAttachment.adapter = imageAdapter
            rvAttachment.itemAnimator?.changeDuration = 0

        }


    }

    private fun setupFileAttachmentAdapter() {
        if (fileAdapter == null || fileAdapter!!.itemCount == 0) {
            fileAdapter = EkoCreatePostFileAdapter(this)
            if (itemDecor != null) {
                rvAttachment.removeItemDecoration(itemDecor!!)
            }
            val space = resources.getDimensionPixelSize(R.dimen.eight)

            itemDecor = SpacesItemDecoration(0, 0, 0, space)
            rvAttachment.addItemDecoration(itemDecor!!)
            rvAttachment.layoutManager = LinearLayoutManager(context)
            rvAttachment.adapter = fileAdapter
            rvAttachment.itemAnimator?.changeDuration = 0
        }
    }

    private fun addPostEditTextListener() {
        etPost.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {
            }

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                handleButtonActiveInactiveBehavior()
            }
        })

    }

    abstract fun setToolBarText()

    abstract fun getPostMenuText(): String


    fun isEditMode(): Boolean {
        return mViewModel.postId != null
    }

    open fun isRightButtonActive(): Boolean {
        if (mViewModel.hasPendingImageToUpload() || mViewModel.hasFailedToUploadImages()) {
            return false
        } else if (mViewModel.hasPendingFileToUpload() || mViewModel.hasFailedToUploadFiles()) {
            return false
        } else if (isEmptyPostTest() && isEmptyFileAttachments() && isEmptyImages()) {
            return false
        }
        return true
    }

    private fun handleButtonActiveInactiveBehavior() {
        //TODO move to viewmodel with a observable field
        updatePostMenu(isRightButtonActive())
        btnUploadPhotos.isEnabled = !mViewModel.hasAttachments()
        btnUploadAttachment.isEnabled = !mViewModel.hasImages()
        btnTakePhoto.isEnabled = !mViewModel.hasAttachments()
    }

    fun updatePostMenu(enabled: Boolean) {
        menuItemPost?.isEnabled = enabled
        val title = menuItemPost?.title
        val spannableString = SpannableString(title)
        spannableString.setSpan(
                ForegroundColorSpan(
                        EkoOptionMenuColorUtil.getColor(
                                menuItemPost?.isEnabled
                                        ?: false, requireContext()
                        )
                ), 0, spannableString.length, 0
        )
        menuItemPost?.title = spannableString
    }

    private fun isEmptyFileAttachments(): Boolean {
        return fileAdapter == null || fileAdapter!!.itemCount == 0
    }

    private fun isEmptyImages(): Boolean {
        return imageAdapter == null || imageAdapter!!.itemCount == 0
    }

    private fun isEmptyPostTest(): Boolean {
        return etPost.text.toString().trim().isEmpty()
    }

    override fun handleBackPress() {
        handleCancelPost()
    }

    private fun handleCancelPost() {
        if (hasDraft()) {
            showExitConfirmationDialog()
        } else {
            backPressFragment()
        }
    }

    private fun showExitConfirmationDialog() {
        val exitConfirmationDialogFragment = EkoAlertDialogFragment
                .newInstance(
                        R.string.amity_discard_post_title,
                        R.string.amity_discard_post_message,
                        R.string.amity_discard,
                        R.string.amity_cancel
                )
        exitConfirmationDialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        exitConfirmationDialogFragment.listener = this
    }

    private fun hasDraft(): Boolean {
        return if (isEditMode()) {
            mViewModel.hasUpdateOnPost(etPost.text.toString().trim())
        } else {
            !(isEmptyFileAttachments() && isEmptyImages() && isEmptyPostTest())

        }
    }

    private fun openImagePicker() {
        val selectedImageCount = mViewModel.getImages().value?.size ?: 0
        if (selectedImageCount == MAX_IMAGE_SELECTABLE) {
            Toast.makeText(
                    context,
                    getString(R.string.amity_create_post_max_image_selected_warning),
                    Toast.LENGTH_LONG
            ).show()
        } else {
            Matisse.from(this)
                    .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))
                    .countable(true)
                    .maxSelectable(MAX_IMAGE_SELECTABLE - selectedImageCount)
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .imageEngine(GlideEngine())
                    .theme(R.style.ImagePickerTheme)
                    .forResult(EkoConstants.PICK_IMAGES)
        }
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION_IMAGE_UPLOAD -> openImagePicker()
            REQUEST_STORAGE_PERMISSION_FILE_UPLOAD -> openFilePicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            EkoConstants.PICK_IMAGES -> {
                data?.let {
                    addImages(it)
                }
            }
            EkoConstants.PICK_FILES -> {
                if (data != null)
                    addFileAttachments(data)

            }
            EkoConstants.CAPTURE_IMAGE -> {
                photoFile?.also {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(photoFile)
                    mediaScanIntent.data = contentUri
                    activity?.sendBroadcast(mediaScanIntent)
                    val images = mViewModel.addImages(listOf(contentUri))
                    uploadImage(images[0])
                }

            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun addViewModelListener() {
        mViewModel.onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.FAILED_TO_UPLOAD_IMAGE -> {
                    showImageUploadFailedDialog()
                }
                EventIdentifier.FAILED_TO_UPLOAD_FILES -> {
                    showAttachmentUploadFailedDialog()
                }
                EventIdentifier.FILE_UPLOAD_MAX_LIMIT_EXCEED -> {
                    showErrorMessage(R.string.amity_attachment_count_limit_exceed)
                }
                EventIdentifier.CREATE_POST_IMAGE_REMOVED -> {
                    if (event.dataObj as Int > 0)
                        imageAdapter?.notifyItemRangeChanged(0, event.dataObj as Int)
                }
                else -> {
                }
            }
        }
    }

    private fun showImageUploadFailedDialog() {
        val dialogFragment = EkoAlertDialogFragment
                .newInstance(
                        R.string.amity_upload_incomplete, R.string.amity_image_upload_failed_message,
                        null, R.string.amity_ok
                )
        dialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        dialogFragment.setAlertDialogActionListener(object :
                EkoAlertDialogFragment.IAlertDialogActionListener {
            override fun onClickPositiveButton() {
                dialogFragment.dismiss()
            }

            override fun onClickNegativeButton() {

            }

        })
    }

    private fun showAttachmentUploadFailedDialog() {
        val dialogFragment = EkoAlertDialogFragment
                .newInstance(
                        R.string.amity_upload_incomplete, R.string.amity_attachment_upload_failed_message,
                        null, R.string.amity_ok
                )
        dialogFragment.show(childFragmentManager, EkoAlertDialogFragment.TAG);
        dialogFragment.setAlertDialogActionListener(object :
                EkoAlertDialogFragment.IAlertDialogActionListener {
            override fun onClickPositiveButton() {
                dialogFragment.dismiss()
            }

            override fun onClickNegativeButton() {

            }

        })
    }

    private fun maxAttachmentCountExceed(data: Intent): Boolean {
        val currentAttachmentCount = mViewModel.getFiles().value?.size ?: 0
        return (null != data.clipData && (currentAttachmentCount + data.clipData!!.itemCount) > MAX_FILE_SELECTABLE)
                || (currentAttachmentCount + 1) > MAX_FILE_SELECTABLE
    }

    private fun addImages(it: Intent) {
        val images = Matisse.obtainResult(it)
        val feedImages = mViewModel.addImages(images)
        feedImages.forEach { image ->
            uploadImage(image)
        }
    }

    private fun uploadImage(image: FeedImage) {
        val disposable = mViewModel.uploadImage(image)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    mViewModel.updateImageUploadStatus(image, it)
                }.doOnError {
                    Log.d(TAG, it.message ?: "")
                }.subscribe()
        compositeDisposable.add(disposable)
    }

    private fun addFileAttachments(data: Intent) {
        setupFileAttachmentAdapter()
        if (maxAttachmentCountExceed(data)) {
            showErrorMessage(R.string.amity_attachment_count_limit_exceed)
        } else {
            var maxLimitExceedError = false
            val fileUriList = mutableListOf<Uri>()
            var addedFiles: MutableList<FileAttachment> = arrayListOf()
            if (null != data.clipData) {
                for (i in 0 until data.clipData!!.itemCount) {
                    val fileUri = data.clipData!!.getItemAt(i).uri
                    val fileAttachment = getFileAttachment(fileUri)
                    if (exceedMaxFileSize(fileAttachment.size)) {
                        maxLimitExceedError = true
                    } else {
                        fileUriList.add(fileUri)
                        addedFiles.add(fileAttachment)
                    }
                }
            } else {
                val fileUri = data.data!!
                val fileAttachment = getFileAttachment(fileUri)
                if (exceedMaxFileSize(fileAttachment.size)) {
                    maxLimitExceedError = true
                } else {
                    fileUriList.add(fileUri)
                    addedFiles.add(fileAttachment)
                }
            }
            if (maxLimitExceedError) {
                showMaxLimitExceedError(addedFiles)
            } else {
                uploadFileAttachments(addedFiles)
            }
        }
    }

    private fun showMaxLimitExceedError(addedFiles: MutableList<FileAttachment>) {
        val dialogFragment = EkoAlertDialogFragment
                .newInstance(
                        R.string.amity_file_max_limit_exceed_title, R.string.amity_file_max_limit_exceed_message,
                        null, R.string.amity_ok
                )
        dialogFragment.setAlertDialogActionListener(object :
                EkoAlertDialogFragment.IAlertDialogActionListener {
            override fun onClickPositiveButton() {
                dialogFragment.dismiss()
                uploadFileAttachments(addedFiles)
            }

            override fun onClickNegativeButton() {
                uploadFileAttachments(addedFiles)
            }

        })
        val fragmentTransaction = childFragmentManager.beginTransaction();
        fragmentTransaction.add(dialogFragment, EkoAlertDialogFragment.TAG)
                .commitAllowingStateLoss()

    }

    private fun uploadFileAttachments(addedFiles: MutableList<FileAttachment>) {
        val files = mViewModel.addFiles(addedFiles)
        if (files.size != addedFiles.size) {
            showDuplicateFilesMessage()
        }
        files.forEach { fileAttachment ->
            val disposable = mViewModel.uploadFile(fileAttachment)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        mViewModel.updateFileUploadStatus(fileAttachment, it)
                    }.doOnError {
                        Log.d(TAG, it.message ?: "")
                    }.subscribe()
            compositeDisposable.add(disposable)
        }

    }

    private fun showDuplicateFilesMessage() {
        Toast.makeText(context, getString(R.string.amity_duplicate_files), Toast.LENGTH_LONG).show()
    }


    private fun showErrorMessage(@StringRes error: Int) {
        Toast.makeText(context, getString(error), Toast.LENGTH_LONG).show()
    }

    private fun exceedMaxFileSize(size: Long): Boolean {
        return (size > EkoConstants.FILE_SIZE_GB)
    }

    private fun takePicture() {
        val cameraPermission =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    var permissionGranted = false
                    permissions.entries.forEach {
                        permissionGranted = it.value
                    }
                    if (permissionGranted) {
                        dispatchTakePictureIntent()
                    }
                }

        cameraPermission.launch(
                arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        )
    }

    private fun dispatchTakePictureIntent() {
        if (activity?.applicationContext != null) {
            photoFile = EkoCameraUtil.createImageFile(requireActivity().applicationContext)
            photoFile?.also {
                val photoUri =
                        EkoCameraUtil.createPhotoUri(requireActivity().applicationContext, it)
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // Ensure that there's a camera activity to handle the intent
                    takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                        takePictureIntent.putExtra(
                                MediaStore.EXTRA_OUTPUT, photoUri

                        )
                        startActivityForResult(takePictureIntent, EkoConstants.CAPTURE_IMAGE)
                    }
                }
            }
        }

    }


    private fun getFileAttachment(uri: Uri): FileAttachment {
        val fileName: String = FileUtils.getName(requireActivity().contentResolver, uri)!!
        val fileSize: Long = FileUtils.getSize(requireActivity().contentResolver, uri).toLong()
        val mimeType = FileUtils.getMimeType(requireActivity().contentResolver, uri)
        return FileAttachment(
                null,
                UUID.randomUUID().toString(),
                fileName,
                fileSize,
                uri,
                FileUtils.humanReadableByteCount(fileSize, true)!!,
                mimeType!!,
                FileUploadState.PENDING, 0
        )
    }

    override fun onRemoveImage(feedImage: FeedImage, position: Int) {
        mViewModel.removeImage(feedImage)
        handleButtonActiveInactiveBehavior()
    }

    override fun onRemoveFile(file: FileAttachment, position: Int) {
        mViewModel.removeFile(file)
    }


    override fun onClickPositiveButton() {
        //TODO change adapter to feedimage item adapter
        consumeBackPress = false
        mViewModel.discardPost()
        activity?.onBackPressed()
    }


    internal fun refresh() {
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


    private fun isModerator(): Boolean {
        //TODO : need to discuss moderator logic
        return mViewModel.hasAdminAccess()
    }


    override fun onClickNegativeButton() {

    }


}