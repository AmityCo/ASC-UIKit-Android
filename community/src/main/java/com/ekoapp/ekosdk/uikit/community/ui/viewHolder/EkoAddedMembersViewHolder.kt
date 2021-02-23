package com.ekoapp.ekosdk.uikit.community.ui.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.common.loadImage
import com.ekoapp.ekosdk.uikit.common.setShape
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoAddedMemberClickListener
import com.google.android.material.imageview.ShapeableImageView

open class EkoAddedMembersViewHolder(
        itemView: View,
        private val mClickListener: EkoAddedMemberClickListener
) : RecyclerView.ViewHolder(itemView),
        EkoBaseRecyclerViewAdapter.IBinder<SelectMemberItem> {

    private val binding: ViewDataBinding? = DataBindingUtil.bind(itemView)
    private val name: TextView = itemView.findViewById(R.id.amName)
    private val avatar: ShapeableImageView = itemView.findViewById(R.id.amAvatar)
    private val cancel: ImageView = itemView.findViewById(R.id.amCross)
    private val layout: ConstraintLayout? = itemView.findViewById(R.id.lAddedMemberItem)

    init {
        val radius = itemView.context.resources.getDimensionPixelSize(R.dimen.twenty_four).toFloat()
        layout?.setShape(
                radius,
                radius, 
                radius, 
                radius, 
                R.color.amityColorBase,
                R.color.amityColorBase, 
                ColorShade.SHADE4
        )
    }

    override fun bind(data: SelectMemberItem?, position: Int) {
        if (data != null) {
            name.text = data.name
            avatar.loadImage(data.avatarUrl)

            cancel.setOnClickListener {
                mClickListener.onMemberRemoved(data)
            }
        }
    }


}