package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedItemActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType

class NewsFeedItemTextViewHolder(
        itemView: View,
        itemActionLister: INewsFeedItemActionListener,
        timelineType: EkoTimelineType
) : NewsFeedViewHolder(itemView, itemActionLister, timelineType) {

    override fun bind(data: EkoPost?, position: Int) {
        super.bind(data, position)
        if (data != null) {
            if (data.getData() is EkoPost.Data.TEXT) {
                val textData = data.getData() as EkoPost.Data.TEXT
                feed.text = textData.getText()
            } else {
                feed.text = ""
            }
            feed.visibility = View.VISIBLE
        }
    }
}