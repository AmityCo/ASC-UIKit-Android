package com.ekoapp.ekosdk.uikit.chat.messages.viewHolder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.ekoapp.ekosdk.message.EkoMessage
import com.ekoapp.ekosdk.uikit.chat.R
import com.ekoapp.ekosdk.uikit.chat.databinding.ItemImageMsgReceiverBinding
import com.ekoapp.ekosdk.uikit.chat.databinding.MsgReportPopupBinding
import com.ekoapp.ekosdk.uikit.chat.messages.popUp.EkoPopUp
import com.ekoapp.ekosdk.uikit.chat.messages.viewModel.EkoImageMsgViewModel
import com.ekoapp.ekosdk.uikit.common.isNotEmptyOrBlank
import com.ekoapp.ekosdk.uikit.common.setShape
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.imagepreview.*
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.google.android.material.shape.CornerFamily

class EkoImageMsgReceiverViewHolder(itemView: View,
                                    private val itemViewModel: EkoImageMsgViewModel,
                                    private  val context: Context
): EkoSelectableMessageViewHolder(itemView, itemViewModel, context) {

    private val binding: ItemImageMsgReceiverBinding? = DataBindingUtil.bind(itemView)
    private var popUp: EkoPopUp? = null

    init {
        binding?.vmImageMessage = itemViewModel
        addViewModelListeners()
    }

    private fun addViewModelListeners() {
        itemViewModel.onEventReceived += {event->
            when(event.type) {
                EventIdentifier.DISMISS_POPUP -> popUp?.dismiss()
                else -> {}
            }
        }
    }

    override fun setMessageData(item: EkoMessage) {
        val imageData = item.getData() as EkoMessage.Data.IMAGE
        itemViewModel.imageUrl.set(imageData.getUrl())
        if (itemViewModel.imageUrl.get() != null && itemViewModel.imageUrl.get()!!.isNotEmptyOrBlank()) {
            val radius = context.resources.getDimension(com.ekoapp.ekosdk.uikit.R.dimen.six)
            binding?.ivImageIncoming?.shapeAppearanceModel = binding?.ivImageIncoming?.shapeAppearanceModel
                ?.toBuilder()
                ?.setTopLeftCorner(CornerFamily.ROUNDED, radius)
                ?.setBottomLeftCorner(CornerFamily.ROUNDED, radius)
                ?.setBottomRightCorner(CornerFamily.ROUNDED, radius)
                ?.build()!!
        }else {
            binding?.ivImageIncoming?.setShape(null, null,
                itemView.context.resources.getDimension(R.dimen.zero),
                null, R.color.upstraColorBase, null, ColorShade.SHADE4)
        }

        binding?.ivImageIncoming?.setOnClickListener {
            navigateToImagePreview(imageData.getUrl())
        }

    }

    private fun navigateToImagePreview(imageUrl: String) {
        val imageList = mutableListOf(PreviewImage(imageUrl))
        val intent = EkoImagePreviewActivity.newIntent(context, 0, false, ArrayList(imageList))
        context.startActivity(intent)
    }

    override fun showPopUp() {
        popUp = EkoPopUp()
        val anchor: View = itemView.findViewById(R.id.ivImageIncoming)
        val inflater: LayoutInflater = LayoutInflater.from(anchor.context)
        val binding: MsgReportPopupBinding = DataBindingUtil.inflate(inflater,
            R.layout.msg_report_popup, null, true)
        binding.viewModel = itemViewModel
        popUp?.showPopUp(binding.root, anchor, itemViewModel, EkoPopUp.PopUpGravity.START)
    }
}