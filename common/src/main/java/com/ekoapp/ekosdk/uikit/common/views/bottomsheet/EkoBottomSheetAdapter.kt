package com.ekoapp.ekosdk.uikit.common.views.bottomsheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.databinding.AmityItemBottomSheetBinding
import com.ekoapp.ekosdk.uikit.model.EkoMenuItem

class EkoBottomSheetAdapter(
        private val list: List<EkoMenuItem>,
        private val listener: IEkoMenuItemClickListener?
) : RecyclerView.Adapter<EkoBottomSheetAdapter.BottomSheetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AmityItemBottomSheetBinding =
                DataBindingUtil.inflate(inflater, R.layout.amity_item_bottom_sheet, parent, false)
        return BottomSheetViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: BottomSheetViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class BottomSheetViewHolder(
            private val binding: AmityItemBottomSheetBinding,
            private val listener: IEkoMenuItemClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EkoMenuItem?) {
            if (item != null) {
                binding.menuItem = item
                binding.listener = listener
                binding.alertColor = isTitleEqualRemove(item, itemView)
            }
        }
    }

    private fun isTitleEqualRemove(item: EkoMenuItem, itemView: View): Boolean {
        return item.title == itemView.context.getString(R.string.amity_remove_user)
    }
}