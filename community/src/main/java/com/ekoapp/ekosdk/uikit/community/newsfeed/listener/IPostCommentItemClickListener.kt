package com.ekoapp.ekosdk.uikit.community.newsfeed.listener

import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.user.EkoUser

interface IPostCommentItemClickListener {
    fun onClickItem(comment: EkoComment, position: Int)
    fun onClickAvatar(user: EkoUser)
}