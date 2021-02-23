package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.file.EkoFile
import com.ekoapp.ekosdk.uikit.base.SpacesItemDecoration
import com.ekoapp.ekosdk.uikit.common.FileUtils
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.domain.model.FileAttachment
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedItemActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.IPostFileItemClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FileUploadState
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType

class NewsFeedItemAttachmentViewHolder(
        itemView: View,
        itemActionLister: INewsFeedItemActionListener,
        private val loadMoreFilesClickListener: EkoPostViewFileAdapter.ILoadMoreFilesClickListener?,
        private val fileItemClickListener: IPostFileItemClickListener?,
        timelineType: EkoTimelineType
) : NewsFeedViewHolder(itemView, itemActionLister, timelineType) {
    private val rvAttachment = itemView.findViewById<RecyclerView>(R.id.rvAttachment)
    val space = itemView.context.resources.getDimensionPixelSize(R.dimen.eight)
    val itemDecor = SpacesItemDecoration(0, 0, 0, space)

    override fun bind(data: EkoPost?, position: Int) {
        super.bind(data, position)
        data?.let { ekoPost ->
            val files = mutableListOf<EkoFile>()
            if (!ekoPost.getChildren().isNullOrEmpty()) {
                ekoPost.getChildren().forEach {
                    when (val postData = it.getData()) {
                        is EkoPost.Data.FILE -> {
                            postData.getFile()?.let { ekoFile ->
                                files.add(ekoFile)
                            }
                        }
                    }
                }
                initAttachments(ekoPost, files)
            }
        }
    }

    private fun initAttachments(data: EkoPost, files: List<EkoFile>) {
        val adapter =
                EkoPostViewFileAdapter(loadMoreFilesClickListener, fileItemClickListener, data, true)
        rvAttachment.removeItemDecoration(itemDecor)
        rvAttachment.addItemDecoration(itemDecor)
        rvAttachment.layoutManager = LinearLayoutManager(itemView.context)
        rvAttachment.adapter = adapter
        adapter.submitList(mapEkoFilesToFileAttachments(files))
    }

    private fun mapEkoFilesToFileAttachments(ekoFile: List<EkoFile>): List<FileAttachment> {
        return ekoFile.map {
            val fileSize = it.getFileSize()?.toLong() ?: 0L
            FileAttachment(
                    it.getFileId(),
                    null,
                    it.getFileName() ?: "",
                    fileSize,
                    Uri.parse(it.getUrl()),
                    FileUtils.humanReadableByteCount(fileSize, true)!!,
                    it.getMimeType() ?: "",
                    FileUploadState.COMPLETE,
                    100
            )
        }
    }
}