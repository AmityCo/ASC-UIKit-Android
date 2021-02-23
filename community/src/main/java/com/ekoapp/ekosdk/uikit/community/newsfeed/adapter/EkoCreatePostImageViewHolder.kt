package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.ICreatePostImageActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FeedImage
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FileUploadState
import com.ekoapp.ekosdk.uikit.utils.ScreenUtils
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily


class EkoCreatePostImageViewHolder(
        itemView: View,
        val listener: ICreatePostImageActionListener,
        private var itemChangeListener: IListItemChangeListener
) :
        RecyclerView.ViewHolder(itemView), EkoBaseRecyclerViewAdapter.IBinder<FeedImage> {
    val container: ConstraintLayout = itemView.findViewById(R.id.container)
    var photo: ShapeableImageView = itemView.findViewById(R.id.ivPhoto)
    var removePhoto: ShapeableImageView = itemView.findViewById(R.id.ivCross)
    var errorPhoto: ShapeableImageView = itemView.findViewById(R.id.ivError)
    private val progrssBar: ProgressBar = itemView.findViewById(R.id.pbImageUpload)
    private val radius: Float = itemView.context.resources.getDimension(R.dimen.four)
    override fun bind(data: FeedImage?, position: Int) {
        setupShape()
        if (container.tag != itemChangeListener.itemCount().toString()) {

            var containerHeight = getHeight(itemChangeListener.itemCount())
            var containerWidth = getWidth(itemChangeListener.itemCount())
            if (container.width != containerWidth || containerHeight != container.height) {
                container.layoutParams.apply {
                    width = containerWidth
                    height = containerHeight
                }
            }
            container.tag = itemChangeListener.itemCount().toString()
        }

        if (photo.tag != data!!.url.toString()) {
            Glide.with(itemView.context)
                    .load(data.url)
                    .into(photo)
            photo.tag = data.url.toString()
        }

        progrssBar.visibility =
                if (data.uploadState == FileUploadState.UPLOADING) View.VISIBLE else View.GONE

        if (data.uploadState == FileUploadState.UPLOADING) {
            if (progrssBar.visibility != View.VISIBLE)
                progrssBar.visibility = View.VISIBLE

        } else {
            progrssBar.visibility = View.GONE
        }
        progrssBar.progress = data.currentProgress
        if (data.uploadState == FileUploadState.COMPLETE) {
            photo.alpha = 1F
        } else if (photo.alpha != 0.6F) {
            photo.alpha = 0.6F
        }

        errorPhoto.visibility =
                if (data.uploadState == FileUploadState.FAILED) View.VISIBLE else View.GONE

        removePhoto.setOnClickListener {
            listener.onRemoveImage(data, position)
        }
    }

    private fun setupShape() {
        val shapeAppearanceModel = photo.shapeAppearanceModel
                .toBuilder()
        photo.shapeAppearanceModel = shapeAppearanceModel
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

    }

    private fun getHeight(itemCount: Int): Int {
        var dimenRes: Int = -1
        dimenRes = when (itemCount) {
            1, 2 -> R.dimen.three_hundred_twenty_eight
            else -> {
                R.dimen.one_hundred_twenty
            }
        }
        return itemView.context.resources.getDimensionPixelSize(dimenRes)
    }

    private fun getWidth(itemCount: Int): Int {
        val margin: Int = itemView.context.resources.getDimensionPixelSize(R.dimen.sixteen)
        var dimenRes: Int = itemView.context.resources.getDimensionPixelSize(R.dimen.eight)
        dimenRes = when (itemCount) {
            1 -> return ScreenUtils.getScreenWidth(itemView.context) - margin * 2
            2 -> {
                return ScreenUtils.getHalfScreenWidth(itemView.context) - margin - dimenRes / 2
            }
            else -> {
                return ScreenUtils.getOneThirdScreenWidth(itemView.context) - margin - dimenRes / 2
            }
        }
        return itemView.context.resources.getDimensionPixelSize(dimenRes)
    }
}