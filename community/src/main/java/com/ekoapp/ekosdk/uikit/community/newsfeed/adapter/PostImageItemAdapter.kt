package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.common.views.ColorPaletteUtil
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.IPostImageItemClickListener
import com.ekoapp.ekosdk.uikit.utils.ScreenUtils
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.textview.MaterialTextView


class PostImageItemAdapter(private val itemClickListener: IPostImageItemClickListener) :
        EkoBaseRecyclerViewAdapter<EkoImage>() {

    override fun getLayoutId(position: Int, obj: EkoImage?): Int {
        return R.layout.amity_item_post_image
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return PostImageItemViewHolder(view, list.size, itemClickListener)
    }

    fun submitList(newList: List<EkoImage>) {
        setItems(newList, DiffCallback(list, newList))
    }

    override fun getItemCount(): Int {
        if (list.size > 4)
            return 4
        else
            return super.getItemCount()
    }

    class DiffCallback(
            private val oldList: List<EkoImage>,
            private val newList: List<EkoImage>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].getFileId() == newList[newItemPosition].getFileId()
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }


    class PostImageItemViewHolder(
            itemView: View,
            private val itemCount: Int,
            private val itemClickListener: IPostImageItemClickListener
    ) :
            RecyclerView.ViewHolder(itemView),
            IBinder<EkoImage> {

        var image: ShapeableImageView = itemView.findViewById(R.id.ivFeedImage)
        var imageCount: MaterialTextView = itemView.findViewById(R.id.tvImageCount)
        val container: ConstraintLayout = itemView.findViewById(R.id.imageContainerLayout)
        private val radius: Float = itemView.context.resources.getDimension(R.dimen.four)
        override fun bind(data: EkoImage?, position: Int) {
            if (data != null) {
                val containerTag = position.toString().plus("-").plus(itemCount)
                if (container.tag != containerTag) {
                    setupImageCount(position)
                    setupShape(position, itemCount)
                    container.layoutParams.apply {
                        width = getWidth(position)
                        height = getHeight(position)
                    }
                    container.tag = containerTag
                }
                val imageUrl = data.getUrl(EkoImage.Size.MEDIUM)
                if (image.tag != imageUrl) {
                    Glide.with(itemView)
                            .load(imageUrl)
                            .into(image)
                    image.tag = imageUrl
                }

                itemView.setOnClickListener {
                    itemClickListener.onClickItem(position)
                }
            }
        }

        private fun setupImageCount(position: Int) {
            if (itemCount > 4 && position == 3) {
                imageCount.text =
                        String.format(itemView.resources.getString(R.string.amity_image_count), itemCount - 3)
                imageCount.visibility = View.VISIBLE

            } else {
                imageCount.visibility = View.GONE
            }
        }

        private fun setupShape(position: Int, itemCount: Int) {
            val imageBuilder = image.shapeAppearanceModel
                    .toBuilder()
            var topLeft: Float = 0.0F
            var topRight: Float = 0.0F
            var bottomLeft: Float = 0.0F
            var bottomRight: Float = 0.0F
            when (itemCount) {
                1 -> {
                    topLeft = radius
                    topRight = radius
                    bottomLeft = radius
                    bottomRight = radius
                }
                2 -> {
                    if (position == 0) {
                        topLeft = radius
                        bottomLeft = radius
                    } else {
                        topRight = radius
                        bottomRight = radius
                    }
                }
                3 -> {
                    if (position == 0) {
                        topLeft = radius
                        topRight = radius
                    } else if (position == 1) {
                        bottomLeft = radius

                    } else {
                        bottomRight = radius
                    }
                }
                else -> {
                    if (position == 0) {
                        topLeft = radius
                        topRight = radius
                    } else if (position == 1) {
                        bottomLeft = radius
                    } else if (position == 3) {
                        bottomRight = radius
                    }

                }
            }

            image.shapeAppearanceModel = imageBuilder
                    .setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
                    .setTopRightCorner(CornerFamily.ROUNDED, topRight)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
                    .setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
                    .build()

            image.setBackgroundColor(
                    ColorPaletteUtil.getColor(
                            ContextCompat.getColor(
                                    itemView.context,
                                    R.color.amityColorBase
                            ), ColorShade.SHADE4
                    )
            )
        }

        private fun getHeight(position: Int): Int {
            var dimenRes: Int = -1
            dimenRes = when (itemCount) {
                1, 2 -> R.dimen.three_hundred_twenty_eight
                3 -> R.dimen.one_hundred_sixty
                else -> {
                    if (position == 0) {
                        R.dimen.two_hundred_twenty
                    } else R.dimen.one_hundred_four
                }
            }
            return itemView.context.resources.getDimensionPixelSize(dimenRes)
        }

        private fun getWidth(position: Int): Int {
            val margin: Int = itemView.context.resources.getDimensionPixelSize(R.dimen.sixteen)
            val dimenRes: Int = itemView.context.resources.getDimensionPixelSize(R.dimen.eight)
            when (itemCount) {
                1 -> return ScreenUtils.getScreenWidth(itemView.context) - margin * 2 - dimenRes
                2 -> {
                    return ScreenUtils.getHalfScreenWidth(itemView.context) - margin - dimenRes
                }
                3 -> {
                    return if (position == 0) {
                        ScreenUtils.getScreenWidth(itemView.context) - margin * 2 - dimenRes
                    } else ScreenUtils.getHalfScreenWidth(itemView.context) - margin - dimenRes
                }
                else -> {
                    return if (position == 0) {
                        ScreenUtils.getScreenWidth(itemView.context) - margin * 2 - dimenRes
                    } else ScreenUtils.getOneThirdScreenWidth(itemView.context) - margin - dimenRes / 2
                }
            }
        }

    }
}