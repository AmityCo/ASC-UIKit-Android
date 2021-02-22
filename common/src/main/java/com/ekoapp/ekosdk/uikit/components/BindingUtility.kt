package com.ekoapp.ekosdk.uikit.components

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.common.views.ColorPaletteUtil
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.utils.ScaleErrorImageViewTarget
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import java.io.File


@BindingAdapter(value = ["textColorShade", "textColorHintShade"], requireAll = false)
fun setEkoTextColor(
        view: TextView,
        colorShade: ColorShade?,
        textColorHintShade: ColorShade?
) {
    colorShade?.let {
        view.setTextColor(
                ColorPaletteUtil.getColor(
                        view.currentTextColor,
                        colorShade
                )
        )
    }
    textColorHintShade?.let {
        view.setHintTextColor(
                ColorPaletteUtil.getColor(
                        view.currentHintTextColor,
                        textColorHintShade
                )
        )
    }
}

@BindingAdapter(
        value = ["buttonEnabledTextColor", "buttonEnabledTextColorShade", "buttonDisabledTextColor", "buttonDisabledTextColorShade"],
        requireAll = false
)
fun setEkoButtonTextColor(
        view: Button,
        buttonEnabledTextColor: Int?,
        buttonEnabledTextColorShade: ColorShade?,
        buttonDisabledTextColor: Int?,
        buttonDisabledTextColorShade: ColorShade?
) {

    val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled)
    )
    val colorEnabled = ColorPaletteUtil.getColor(
            buttonEnabledTextColor!!,
            buttonEnabledTextColorShade ?: ColorShade.DEFAULT
    )

    val colorDefault = ColorPaletteUtil.getColor(
            buttonDisabledTextColor!!,
            buttonDisabledTextColorShade ?: ColorShade.DEFAULT
    )
    val colors = intArrayOf(
            colorEnabled, colorDefault
    )

    val colorStateList = ColorStateList(states, colors)
    view.setTextColor(colorStateList)
}

@BindingAdapter(value = ["ekoBackgroundColorAlpha"], requireAll = true)
fun setBackgroundAlpha(view: ShapeableImageView, ekoBackgroundColorAlpha: Int) {
    view.background.alpha = ekoBackgroundColorAlpha
}

@BindingAdapter(value = ["ekoButtonStrokeShade"], requireAll = true)
fun setBackgroundAlpha(view: MaterialButton, shade: ColorShade) {
    val strokeColor = view.strokeColor.defaultColor

    val colorDefault = ColorPaletteUtil.getColor(strokeColor, ColorShade.SHADE3)
    val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled)
    )
    val colors = intArrayOf(
            colorDefault, colorDefault
    )
    val colorStateList = ColorStateList(states, colors)
    view.strokeColor = colorStateList
}

@BindingAdapter(value = ["drawableTintColor", "drawableTintShade"], requireAll = false)
fun setDrawableTint(
        view: TextView,
        drawableTintColor: Int?,
        drawableTintShade: ColorShade?
) {
    if (drawableTintColor != null) {
        val tintColor: Int = ColorPaletteUtil.getColor(
                drawableTintColor,
                drawableTintShade ?: ColorShade.DEFAULT
        )
        if (drawableTintShade != null) {
            for (drawable in view.compoundDrawables) {
                drawable?.colorFilter = PorterDuffColorFilter(
                        tintColor, PorterDuff.Mode.SRC_IN
                )

            }
        }
    }
}

@BindingAdapter(value = ["ekoTintColor", "ekoTintShade"], requireAll = false)
fun setImageViewTint(imageView: ImageView, tintColor: Int, tintShade: ColorShade?) {
    val shade = tintShade ?: ColorShade.SHADE2
    ImageViewCompat.setImageTintList(
            imageView,
            ColorStateList.valueOf(ColorPaletteUtil.getColor(tintColor, shade))
    )
}

@BindingAdapter(
        value = ["checkBoxDefaultColor", "checkBoxDefaultShade", "checkBoxCheckedColor", "checkBoxCheckedShade"],
        requireAll = false
)
fun setCheckboxSelectorColor(
        view: MaterialCheckBox,
        checkBoxDefaultColor: Int?,
        checkBoxDefaultShade: ColorShade?,
        checkBoxCheckedColor: Int?,
        checkBoxCheckedShade: ColorShade?
) {

    val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
    )
    val tintColorChecked = ColorPaletteUtil.getColor(
            checkBoxCheckedColor!!,
            checkBoxCheckedShade ?: ColorShade.DEFAULT
    )

    val tintColorDefault = ColorPaletteUtil.getColor(
            checkBoxDefaultColor!!,
            checkBoxDefaultShade ?: ColorShade.DEFAULT
    )
    val colors = intArrayOf(
            tintColorChecked, tintColorDefault
    )

    val colorStateList = ColorStateList(states, colors)
    view.buttonTintList = colorStateList
    view.setTextColor(colorStateList)

}

@BindingAdapter(value = ["message", "isSender"], requireAll = true)
fun setText(view: EkoReadMoreTextView, message: String?, isSender: Boolean) {
    view.isSender(isSender)
    view.setText(message)
}


@BindingAdapter(value = ["longPress", "maxLines"], requireAll = false)
fun setListener(view: EkoReadMoreTextView, listener: ILongPressListener?, maxLines: Int?) {
    if (listener != null) {
        view.setReadMoreListener(listener)
    }
    if (maxLines != null) {
        view.setMaxLines(maxLines)
    }
}

@BindingAdapter(value = ["ekoBackgroundColor", "backgroundColorShade"], requireAll = false)
fun setEkoViewBackgroundColor(
        view: View,
        color: Int?,
        colorShade: ColorShade?
) {
    val shade = colorShade ?: ColorShade.DEFAULT
    val bgColor = color ?: ContextCompat.getColor(view.context, R.color.amityColorPrimary)
    view.setBackgroundColor(ColorPaletteUtil.getColor(bgColor, shade))
}


@BindingAdapter(
        value = ["avatarViewImage", "avatarViewPlaceHolder", "avatarViewSignature",
            "isCircular"], requireAll = false
)
fun setAvatarViewImage(
        view: EkoAvatarView, imagePath: String?, placeholder: Drawable?, signature: String? = "",
        isCircular: Boolean = true
) {
    view.setIsCircular(isCircular)
    view.setAvatarUrl(imagePath, placeholder, signature ?: "")
}

@BindingAdapter("showCameraIcon")
fun setShowCameraIcon(view: EkoAvatarView, showCameraIcon: Boolean = false) {
    view.showCameraIcon(showCameraIcon)
}

@BindingAdapter("required")
fun setRequiredInLabel(view: TextView, required: Boolean = false) {
    if (required) {
        val required: Spannable = SpannableString("*")

        required.setSpan(
                ForegroundColorSpan(Color.RED),
                0,
                required.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.append(required)
    }
}

@BindingAdapter(
        value = ["roundedCorner", "topLeftRadius", "bottomLeftRadius", "topRightRadius",
            "bottomRightRadius", "fillColor", "StrokeColor", "colorShade"], requireAll = false
)
fun setRoundedCorner(
        view: View,
        roundedCorner: Boolean,
        topLeft: Float?,
        bottomLeft: Float?,
        topRight: Float?,
        bottomRight: Float?,
        @ColorRes fillColor: Int?,
        @ColorRes strokeColor: Int?,
        colorShade: ColorShade?
) {
    if (roundedCorner) {
        val radius = view.context.resources.getDimension(R.dimen.six)
        val modal = ShapeAppearanceModel()
                .toBuilder()
        if (topLeft == null) {
            modal.setTopLeftCorner(CornerFamily.ROUNDED, radius)
        } else {
            modal.setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
        }

        if (bottomLeft == null) {
            modal.setBottomLeftCorner(CornerFamily.ROUNDED, radius)
        } else {
            modal.setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
        }

        if (topRight == null) {
            modal.setTopRightCorner(CornerFamily.ROUNDED, radius)
        } else {
            modal.setTopRightCorner(CornerFamily.ROUNDED, topRight)
        }

        if (bottomRight == null) {
            modal.setBottomRightCorner(CornerFamily.ROUNDED, radius)
        } else {
            modal.setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
        }

        val shapeDrawable = MaterialShapeDrawable(modal.build())
        if (fillColor == null) {
            shapeDrawable.fillColor = ContextCompat.getColorStateList(view.context, R.color.amityColorWhite)
        } else {
            if (colorShade == null)
                shapeDrawable.fillColor = ContextCompat.getColorStateList(view.context, fillColor)
            else
                shapeDrawable.setTint(
                        ColorPaletteUtil.getColor(
                                ContextCompat.getColor(
                                        view.context,
                                        fillColor
                                ), colorShade
                        )
                )

        }

        if (strokeColor == null) {
            if (fillColor == null) {
                shapeDrawable.setStroke(2F, ContextCompat.getColor(view.context, R.color.amityColorWhite))
            } else {
                if (colorShade == null)
                    shapeDrawable.setStroke(2F, ContextCompat.getColor(view.context, fillColor))
                else
                    shapeDrawable.setStroke(
                            2F,
                            ColorPaletteUtil.getColor(
                                    ContextCompat.getColor(view.context, fillColor),
                                    colorShade
                            )
                    )
            }
        } else {
            shapeDrawable.setStroke(2F, ContextCompat.getColor(view.context, strokeColor))
        }

        ViewCompat.setBackground(view, shapeDrawable)
    }


}

@BindingAdapter("imageUrl", "placeHolder", requireAll = false)
fun setImageUrl(view: ImageView, imageUrl: String?, placeholder: Drawable?) {
    var glideImageUrl = imageUrl
    var mPlaceholder = placeholder
    var imageUri: Uri = Uri.EMPTY
    if (imageUrl == null) {
        glideImageUrl = ""
    }
    if (placeholder == null) {
        mPlaceholder = ContextCompat.getDrawable(view.context, R.drawable.amity_ic_user)
    }
    val imageSynced = if (glideImageUrl!!.startsWith("https")) {
        true
    } else {
        imageUri = Uri.fromFile(File(glideImageUrl))
        false
    }

    Glide.with(view.context)
            .load(if (imageSynced) glideImageUrl else imageUri)
            .centerCrop()
            .placeholder(mPlaceholder)
            .error(mPlaceholder)
            .dontAnimate()
            .into(ScaleErrorImageViewTarget(view).error())
}

@BindingAdapter(value = ["onScrolled", "onScrollStateChanged"], requireAll = false)
fun setOnRVScroll(
        rv: RecyclerView,
        onScroll: OnScroll?,
        onScrollStateChanged: OnScrollStateChanged?
) {
    rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            onScroll?.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            onScrollStateChanged?.onScrollStateChanged(recyclerView, newState)
        }
    })
}

interface OnScroll {

    fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
}

interface OnScrollStateChanged {

    fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
}

@BindingAdapter(value = ["ekoFillColor", "ekoFillShade"])
fun setRoundedImageView(imageView: ImageView, fillColor: Int, shade: ColorShade) {
    val modal = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(
                    CornerFamily.ROUNDED,
                    imageView.context.resources.getDimensionPixelSize(R.dimen.thirty_two).toFloat()
            )
    val shapeDrawable = MaterialShapeDrawable(modal.build())
    shapeDrawable.setTint(
            ColorPaletteUtil.getColor(fillColor, shade)
    )

    ViewCompat.setBackground(imageView, shapeDrawable)
}

@BindingAdapter("safeText")
fun setText(textView: TextView, input: CharSequence?) {
    if (input != null && input.isNotEmpty()) {
        textView.text = input
    } else {
        textView.text = textView.context.getString(R.string.amity_anonymous)
    }
}
