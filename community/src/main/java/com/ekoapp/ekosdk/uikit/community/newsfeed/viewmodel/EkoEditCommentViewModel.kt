package com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel

import androidx.lifecycle.MutableLiveData
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommentRepository
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import io.reactivex.Completable
import io.reactivex.Single

class EkoEditCommentViewModel : EkoBaseViewModel() {
    private var ekoComment: EkoComment? = null
    private var ekoPost: EkoPost? = null

    val commentText = MutableLiveData<String>().apply { value = "" }
    val hasCommentUpdate = MutableLiveData<Boolean>(false)

    private val commentRepository: EkoCommentRepository = EkoClient.newCommentRepository()

    fun updateComment(): Single<EkoComment>? {
        if (ekoComment != null) {
            return (ekoComment?.getData() as? EkoComment.Data.TEXT)
                    ?.edit()
                    ?.text(commentText.value!!)
                    ?.build()
                    ?.apply()
        }
        return null
    }


    fun addComment(commentId: String): Single<EkoComment>? {
        if (ekoPost == null)
            return null

        return EkoClient.newCommentRepository().createComment(commentId)
                .post(ekoPost!!.getPostId())
                .with()
                .text(commentText.value!!)
                .build()
                .send()
                .map {
                    EkoClient.newFeedRepository().getPost(ekoPost!!.getPostId()).ignoreElements()
                            .onErrorComplete()
                    it
                }
    }

    fun deleteComment(commentId: String): Completable {
        return commentRepository.deleteComment(commentId)
    }

    fun checkForCommentUpdate() {
        val commentData = (ekoComment?.getData() as? EkoComment.Data.TEXT)?.getText()
        val updateAvailable = !commentText.value.isNullOrEmpty() && commentData != commentText.value
        hasCommentUpdate.value = updateAvailable
    }

    fun setPost(ekoPost: EkoPost?) {
        this.ekoPost = ekoPost
    }

    fun setComment(comment: EkoComment?) {
        this.ekoComment = comment
        val commentData = (comment?.getData() as? EkoComment.Data.TEXT)?.getText()
        if (commentData != null)
            commentText.value = commentData

    }

    fun editMode(): Boolean {
        return ekoComment != null
    }

    fun getComment(): EkoComment? {
        return ekoComment
    }

    fun setCommentData(commentText: String?) {
        this.commentText.value = commentText ?: ""
    }

}