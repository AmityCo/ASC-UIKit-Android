package com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoFeedRepository
import com.ekoapp.ekosdk.EkoFileRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.file.EkoFile
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.file.upload.EkoUploadResult
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.common.FileUtils
import com.ekoapp.ekosdk.uikit.community.domain.model.FileAttachment
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FeedImage
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FileUploadState
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import kotlin.collections.HashMap

class EkoCreatePostViewModel : EkoBaseViewModel() {
    private val TAG = EkoCreatePostViewModel::class.java.canonicalName
    private val feedRepository: EkoFeedRepository = EkoClient.newFeedRepository()
    private val fileRepository: EkoFileRepository = EkoClient.newFileRepository()

    var postId: String? = null
    private var newsFeed: EkoPost? = null
    var community: EkoCommunity? = null
    var postText: CharSequence? = null

    private val liveDataImage = MutableLiveData<MutableList<FeedImage>>()
    private val imageMap = HashMap<String, FeedImage>()
    private val uploadedImageMap = HashMap<String, EkoImage>()
    private val deletedImageIds = mutableListOf<String>()
    private val uploadFailedImages = HashMap<String, Boolean>()

    private val liveDataFiles = MutableLiveData<MutableList<FileAttachment>>()
    private val filesMap = HashMap<String, FileAttachment>()
    private val uploadedFilesMap = HashMap<String, EkoFile>()
    private val deletedFileIds = mutableListOf<String>()
    private val uploadFailedFile = HashMap<String, Boolean>()

    fun setNewsFeed(newsFeed: EkoPost) {
        this.newsFeed = newsFeed

        when (newsFeed.getData()) {
            is EkoPost.Data.TEXT -> {
                postText = getPostText(newsFeed)
                setUpPostTextWithImagesOrFiles(newsFeed)

            }
            is EkoPost.Data.IMAGE -> {
                getPostImage(newsFeed)?.let {
                    val feedImage = mapEkoImageToFeedImage(it)
                    imageMap[feedImage.url.toString()] = feedImage
                }
                liveDataImage.value = imageMap.values.toMutableList()
            }
            is EkoPost.Data.FILE -> {
                getPostFile(newsFeed)?.let {
                    val attachment = mapEkoFileToFileAttachment(it)
                    filesMap[attachment.uri.toString()] = attachment
                }
                liveDataFiles.value = filesMap.values.toMutableList()
            }
        }
    }

    private fun setUpPostTextWithImagesOrFiles(ekoPost: EkoPost) {
        val postChildren = ekoPost.getChildren()
        if (postChildren.isNotEmpty()) {
            when (postChildren.first().getData()) {
                is EkoPost.Data.IMAGE -> {
                    postChildren
                            .map { getPostImage(it) }
                            .forEach { image ->
                                image?.let {
                                    val feedImage = mapEkoImageToFeedImage(it)
                                    imageMap[feedImage.url.toString()] = feedImage
                                }
                            }
                    liveDataImage.value = imageMap.values.toMutableList()
                }
                is EkoPost.Data.FILE -> {
                    postChildren
                            .map { getPostFile(it) }
                            .forEach { file ->
                                file?.let {
                                    val attachment = mapEkoFileToFileAttachment(it)
                                    filesMap[attachment.uri.toString()] = attachment
                                }
                            }

                    liveDataFiles.value = filesMap.values.toMutableList()
                }
            }
        }
    }

    private fun mapEkoFileToFileAttachment(ekoFile: EkoFile): FileAttachment {
        val fileSize = ekoFile.getFileSize()?.toLong() ?: 0L
        return FileAttachment(
                ekoFile.getFileId(),
                null,
                ekoFile.getFileName() ?: "",
                fileSize,
                Uri.parse(ekoFile.getUrl()),
                FileUtils.humanReadableByteCount(fileSize, true)!!,
                ekoFile.getMimeType() ?: "",
                FileUploadState.COMPLETE,
                100
        )
    }

    private fun mapEkoFileToFileAttachment(
            fileAttachment: FileAttachment,
            ekoFile: EkoFile
    ): FileAttachment {
        val fileSize = ekoFile.getFileSize()?.toLong() ?: 0L
        return FileAttachment(
                ekoFile.getFileId(),
                fileAttachment.uploadId,
                ekoFile.getFileName() ?: "",
                fileSize,
                fileAttachment.uri,
                FileUtils.humanReadableByteCount(fileSize, true)!!,
                ekoFile.getMimeType() ?: "",
                FileUploadState.COMPLETE,
                100
        )
    }

    fun getNewsFeed(): EkoPost? {
        return newsFeed
    }

    fun getPostDetails(id: String): Flowable<EkoPost> {
        return feedRepository.getPost(id)
    }

    private fun getPostText(newsFeed: EkoPost): CharSequence? {
        val textData = newsFeed.getData() as? EkoPost.Data.TEXT
        return textData?.getText()
    }

    private fun getPostImage(newsFeed: EkoPost): EkoImage? {
        val imageData = newsFeed.getData() as? EkoPost.Data.IMAGE
        return imageData?.getImage()
    }

    private fun getPostFile(newsFeed: EkoPost): EkoFile? {
        val fileData = newsFeed.getData() as? EkoPost.Data.FILE
        return fileData?.getFile()
    }

    fun getImages(): MutableLiveData<MutableList<FeedImage>> {
        return liveDataImage
    }


    fun updatePostText(postText: String): Completable {
        val textData = newsFeed!!.getData() as EkoPost.Data.TEXT
        return textData.edit()
                .text(postText)
                .build()
                .apply()
    }

    private fun createPostText(postText: String): Single<EkoPost> {
        return if (community != null) {
            feedRepository.createPost().targetCommunity(community!!.getCommunityId()).text(postText)
                    .build().post()
        } else {
            feedRepository.createPost().targetMe().text(postText).build().post()
        }
    }

    private fun createPostTextAndImages(postText: String, images: List<EkoImage>): Single<EkoPost> {
        val imageArray = images.toTypedArray()
        return if (community != null) {
            feedRepository.createPost()
                    .targetCommunity(community!!.getCommunityId())
                    .image(*imageArray)
                    .text(postText).build().post()
        } else {
            feedRepository.createPost()
                    .targetMe()
                    .image(*imageArray)
                    .text(postText).build().post()
        }
    }

    private fun createPostTextAndFiles(postText: String, files: List<EkoFile>): Single<EkoPost> {
        val fileArray = files.toTypedArray()
        return if (community != null) {
            feedRepository.createPost()
                    .targetCommunity(community!!.getCommunityId())
                    .file(*fileArray)
                    .text(postText).build().post()
        } else {
            feedRepository.createPost()
                    .targetMe()
                    .file(*fileArray)
                    .text(postText).build().post()
        }
    }

    fun uploadImage(feedImage: FeedImage): Flowable<EkoUploadResult<EkoImage>> {
        return fileRepository
                .uploadImage(feedImage.url)
                .uploadId(feedImage.uploadId!!)
                .isFullImage(true).build().transfer()
    }

    fun uploadFile(attachment: FileAttachment): Flowable<EkoUploadResult<EkoFile>> {
        return fileRepository
                .uploadFile(attachment.uri)
                .uploadId(attachment.uploadId!!)
                .build().transfer()
    }

    fun deleteImageOrFileInPost(
    ): Completable {
        return newsFeed?.getChildren()?.let { ekoPostItems ->
            Observable.fromIterable(ekoPostItems)
                    .map { ekoPostItem ->
                        when (val postData = ekoPostItem.getData()) {
                            is EkoPost.Data.IMAGE -> {
                                if (postData.getImage()?.getFileId() in deletedImageIds) {
                                    ekoPostItem.delete()
                                } else {
                                    Completable.complete()
                                }
                            }
                            is EkoPost.Data.FILE -> {
                                if (postData.getFile()?.getFileId() in deletedFileIds) {
                                    ekoPostItem.delete()
                                } else {
                                    Completable.complete()
                                }
                            }
                            else -> {
                                Completable.complete()
                            }
                        }
                    }.ignoreElements()
        } ?: kotlin.run {
            Completable.complete()
        }
    }

    fun hasAdminAccess(): Boolean {
        return (community != null && community!!.getUserId() == EkoClient.getUserId())
    }

    fun addImages(images: List<Uri>): List<FeedImage> {
        images.forEach { uriItem ->
            if (!imageMap.containsKey(uriItem.toString())) {
                imageMap[uriItem.toString()] =
                        FeedImage(null, UUID.randomUUID().toString(), uriItem)
            }
        }
        val currentImages = imageMap.values.toMutableList()
        liveDataImage.value = currentImages
        return currentImages.filter { it.id == null }
    }

    fun removeImage(feedImage: FeedImage) {
        imageMap.remove(feedImage.url.toString())
        uploadFailedImages.remove(feedImage.url.toString())
        cancelUpload(feedImage.uploadId)


        if (feedImage.id != null) {
            //In case update post we want to keep track of the images to delete
            if (newsFeed != null) {
                deletedImageIds.add(feedImage.id!!)
            } else {
                uploadedImageMap.remove(feedImage.id!!)
            }
        }
        liveDataImage.value = imageMap.values.toMutableList()
        triggerImageRemovedEvent()

    }

    private fun cancelUpload(uploadId: String?) {
        if (uploadId != null) {
            Log.d(TAG, "cancel file upload $uploadId")
            fileRepository.cancelUpload(uploadId)
        }
    }

    private fun triggerImageRemovedEvent() {
        triggerEvent(EventIdentifier.CREATE_POST_IMAGE_REMOVED, liveDataImage.value?.size ?: 0)
    }

    fun updateImageUploadStatus(feedImage: FeedImage, ekoImageUpload: EkoUploadResult<EkoImage>) {
        when (ekoImageUpload) {
            is EkoUploadResult.PROGRESS -> {
                val updatedFeedImage = FeedImage(
                        feedImage.id,
                        feedImage.uploadId,
                        feedImage.url,
                        FileUploadState.UPLOADING,
                        ekoImageUpload.getUploadInfo().getProgressPercentage()
                )
                updateList(updatedFeedImage)
            }
            is EkoUploadResult.COMPLETE -> {
                uploadFailedImages.remove(feedImage.url.toString())
                uploadedImageMap[ekoImageUpload.getFile().getFileId()] = ekoImageUpload.getFile()
                val updatedFeedImage = FeedImage(
                        ekoImageUpload.getFile().getFileId(),
                        feedImage.uploadId,
                        feedImage.url,
                        FileUploadState.COMPLETE,
                        100
                )
                updateList(updatedFeedImage)
                if (!hasPendingImageToUpload() && hasFirstTimeFailedToUploadImages()) {
                    triggerImageUploadFailedEvent()
                }
            }
            is EkoUploadResult.ERROR, EkoUploadResult.CANCELLED -> {
                Log.d(TAG, "Image upload error " + feedImage.url)
                if (imageMap.containsKey(feedImage.url.toString())) {

                    val updatedFeedImage = FeedImage(
                            feedImage.id,
                            feedImage.uploadId,
                            feedImage.url,
                            FileUploadState.FAILED,
                            0
                    )
                    var firstTimeFailedToUpload =
                            !uploadFailedImages.containsKey(updatedFeedImage.url.toString())
                    uploadFailedImages[updatedFeedImage.url.toString()] = firstTimeFailedToUpload
                    updateList(updatedFeedImage)
                    if (!hasPendingImageToUpload() && hasFirstTimeFailedToUploadImages()) {
                        triggerImageUploadFailedEvent()
                    }
                }

            }
        }
    }

    private fun triggerImageUploadFailedEvent() {
        uploadFailedImages.keys.forEach {
            uploadFailedImages[it] = false
        }
        triggerEvent(EventIdentifier.FAILED_TO_UPLOAD_IMAGE)
    }

    private fun updateList(feedImage: FeedImage) {
        if (imageMap.containsKey(feedImage.url.toString())) {
            imageMap[feedImage.url.toString()] = feedImage
            liveDataImage.value = imageMap.values.toMutableList()
        }
    }

    private fun updateList(fileAttachment: FileAttachment) {
        if (filesMap.containsKey(fileAttachment.uri.toString())) {
            filesMap[fileAttachment.uri.toString()] = fileAttachment
            liveDataFiles.value = filesMap.values.toMutableList()
        }
    }

    fun createPost(postText: String): Single<EkoPost> {
        return when {
            uploadedImageMap.isNotEmpty() -> {
                createPostTextAndImages(
                        postText,
                        uploadedImageMap.values.toList()
                )
            }
            uploadedFilesMap.isNotEmpty() -> {
                createPostTextAndFiles(postText, uploadedFilesMap.values.toList())
            }
            else -> {
                createPostText(postText)
            }
        }
    }

    private fun mapEkoImageToFeedImage(ekoImage: EkoImage): FeedImage {
        return FeedImage(
                ekoImage.getFileId(),
                null,
                Uri.parse(ekoImage.getUrl(EkoImage.Size.LARGE)),
                FileUploadState.COMPLETE,
                100
        )
    }

    fun hasPendingImageToUpload(): Boolean {
        val totalImageProcessed = uploadedImageMap.size + uploadFailedImages.size
        return if (newsFeed == null)
            liveDataImage.value != null && totalImageProcessed < liveDataImage.value!!.size
        else
            false
    }

    fun hasPendingFileToUpload(): Boolean {
        val totalFileProcessed = uploadedFilesMap.size + uploadFailedFile.size
        return if (newsFeed == null)
            return liveDataFiles.value != null && liveDataFiles.value!!.size != totalFileProcessed
        else
            false
    }


    fun hasUpdateOnPost(postText: String): Boolean {
        if (newsFeed == null)
            return false
        if (postText.isEmpty() && liveDataImage.value.isNullOrEmpty() && liveDataFiles.value.isNullOrEmpty()) {
            return false
        }
        if (postText != getPostText(newsFeed!!))
            return true
        if (deletedImageIds.size > 0)
            return true
        if (deletedFileIds.size > 0)
            return true


        return false
    }

    fun hasFailedToUploadImages(): Boolean {
        return uploadFailedImages.size > 0
    }

    fun hasFailedToUploadFiles(): Boolean {
        return uploadFailedFile.size > 0
    }

    fun getFiles(): MutableLiveData<MutableList<FileAttachment>> {
        return liveDataFiles
    }


    fun addFiles(fileAttachments: MutableList<FileAttachment>): MutableList<FileAttachment> {
        val addedFiles = mutableListOf<FileAttachment>()
        fileAttachments.forEach {
            val key = it.uri.toString()
            if (!isDuplicateFile(it)) {
                filesMap[key] = it
                uploadedFilesMap.remove(key)
                addedFiles.add(it)
            }
        }
        liveDataFiles.value = filesMap.values.toMutableList()
        return addedFiles
    }

    private fun isDuplicateFile(fileAttachment: FileAttachment): Boolean {
        val result = filesMap.values.filter {
            it.uri == fileAttachment.uri && it.uploadState != FileUploadState.FAILED
        }
        return result.isNotEmpty()
    }


    fun removeFile(file: FileAttachment) {
        filesMap.remove(file.uri.toString())
        uploadFailedFile.remove(file.uri.toString())
        cancelUpload(file.uploadId)

        if (file.id != null) {
            //In case update post we want to keep track of the images to delete
            if (newsFeed != null) {
                deletedFileIds.add(file.id)
            } else {
                uploadedFilesMap.remove(file.id)
            }
        }
        liveDataFiles.value = filesMap.values.toMutableList()
    }

    fun updateFileUploadStatus(
            fileAttachment: FileAttachment,
            fileUpload: EkoUploadResult<EkoFile>
    ) {
        when (fileUpload) {
            is EkoUploadResult.PROGRESS -> {
                Log.d(
                        TAG,
                        "File upload progress " + fileAttachment.name + fileUpload.getUploadInfo()
                                .getProgressPercentage()
                )
                val updatedFileAttachment = FileAttachment(
                        fileAttachment.id,
                        fileAttachment.uploadId,
                        fileAttachment.name,
                        fileAttachment.size,
                        fileAttachment.uri,
                        fileAttachment.readableSize,
                        fileAttachment.mimeType,
                        FileUploadState.UPLOADING,
                        fileUpload.getUploadInfo().getProgressPercentage()
                )
                updateList(updatedFileAttachment)
            }
            is EkoUploadResult.COMPLETE -> {
                Log.d(TAG, "File upload Complete " + fileAttachment.name)
                uploadedFilesMap[fileUpload.getFile().getFileId()] = fileUpload.getFile()
                val updatedFileAttachment =
                        mapEkoFileToFileAttachment(fileAttachment, fileUpload.getFile())
                updateList(updatedFileAttachment)
                if (!hasPendingFileToUpload() && hasFirstTimeFailedToUploadFiles()) {
                    triggerFileUploadFailedEvent()
                }
            }
            is EkoUploadResult.ERROR, EkoUploadResult.CANCELLED -> {
                Log.d(TAG, "File upload error " + fileAttachment.name)
                if (filesMap.containsKey(fileAttachment.uri.toString())) {
                    val updatedFileAttachment = FileAttachment(
                            fileAttachment.id,
                            fileAttachment.uploadId,
                            fileAttachment.name,
                            fileAttachment.size,
                            fileAttachment.uri,
                            fileAttachment.readableSize,
                            fileAttachment.mimeType,
                            FileUploadState.FAILED,
                            0
                    )
                    val firstTimeFailedToUpload =
                            !uploadFailedFile.containsKey(fileAttachment.uri.toString())

                    uploadFailedFile[updatedFileAttachment.uri.toString()] = firstTimeFailedToUpload
                    updateList(updatedFileAttachment)
                    if (!hasPendingFileToUpload() && hasFirstTimeFailedToUploadFiles()) {
                        triggerFileUploadFailedEvent()
                    }
                }

            }
        }
    }

    private fun hasFirstTimeFailedToUploadFiles(): Boolean {
        return uploadFailedFile.values.contains(true)
    }

    private fun hasFirstTimeFailedToUploadImages(): Boolean {
        return uploadFailedImages.values.contains(true)
    }


    private fun triggerFileUploadFailedEvent() {
        uploadFailedFile.keys.forEach {
            uploadFailedFile[it] = false
        }
        triggerEvent(EventIdentifier.FAILED_TO_UPLOAD_FILES)
    }

    fun hasAttachments(): Boolean {
        if (liveDataFiles.value != null)
            return liveDataFiles.value!!.size > 0
        return false
    }

    fun hasImages(): Boolean {
        if (liveDataImage.value != null)
            return liveDataImage.value!!.size > 0
        return false
    }

    fun discardPost() {
        liveDataImage.value?.forEach {
            if (it.id == null && it.uploadId != null)
                cancelUpload(it.uploadId)
        }
        liveDataFiles.value?.forEach {
            if (it.id == null && it.uploadId != null) {
                cancelUpload(it.uploadId)
            }
        }
    }
}