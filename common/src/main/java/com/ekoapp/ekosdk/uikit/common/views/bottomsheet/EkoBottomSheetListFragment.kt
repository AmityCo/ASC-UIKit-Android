package com.ekoapp.ekosdk.uikit.common.views.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.model.EkoMenuItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.amity_bottom_sheet.*

class EkoBottomSheetListFragment private constructor() : BottomSheetDialogFragment() {

    private lateinit var itemList: ArrayList<EkoMenuItem>
    private var mListener: IEkoMenuItemClickListener? = null
    private lateinit var mAdapter: EkoBottomSheetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemList = requireArguments().getParcelableArrayList<EkoMenuItem>(ARG_LIST) ?: arrayListOf()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.amity_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = EkoBottomSheetAdapter(itemList, mListener)
        rvBottomSheet.layoutManager = LinearLayoutManager(requireContext())
        rvBottomSheet.adapter = mAdapter
    }

    fun setMenuItemClickListener(listener: IEkoMenuItemClickListener) {
        mListener = listener
    }

    companion object {
        private const val ARG_LIST = "ARG_LIST"
        fun newInstance(list: ArrayList<EkoMenuItem>): EkoBottomSheetListFragment =
                EkoBottomSheetListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(ARG_LIST, list)
                    }
                }
    }
}