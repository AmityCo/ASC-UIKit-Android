package com.ekoapp.ekosdk.uikit.community.ui.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.ui.adapter.EkoAddedMembersAdapter
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoAddedMemberClickListener
import com.ekoapp.ekosdk.uikit.community.utils.AddedMemberItemDecoration
import com.ekoapp.ekosdk.uikit.community.utils.EkoSelectMemberContract


class EkoCommunityCreateFragment internal constructor() : EkoCommunityCreateBaseFragment(),
        EkoAddedMemberClickListener {

    private lateinit var mAdapter: EkoAddedMembersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addMembers()
        setAddedMemberRecyclerView()
    }

    private fun addMembers() {
        getBindingVariable().ivAdd.setOnClickListener {
            goToAddMembersActivity()
        }
    }

    private fun setAddedMemberRecyclerView() {
        mAdapter = EkoAddedMembersAdapter(this)
        getBindingVariable().rvAddedMembers.layoutManager = GridLayoutManager(requireContext(), 2)
        getBindingVariable().rvAddedMembers.adapter = mAdapter
        getBindingVariable().rvAddedMembers.addItemDecoration(
                AddedMemberItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.eight),
                        resources.getDimensionPixelSize(R.dimen.twelve)
                )
        )
        mAdapter.submitList(mViewModel.selectedMembersList)
    }

    override fun onAddButtonClicked() {
        goToAddMembersActivity()
    }

    override fun onMemberCountClicked() {
        goToAddMembersActivity()
    }

    override fun onMemberRemoved(item: SelectMemberItem) {
        val lastItem = mViewModel.selectedMembersList[mViewModel.selectedMembersList.lastIndex]
        if (lastItem.name == "ADD") {
            mViewModel.selectedMembersList.remove(lastItem)
        }
        mViewModel.selectedMembersList.remove(item)
        setAddMemberVisibility()
        setCount()
        mAdapter.submitList(mViewModel.selectedMembersList)
    }

    private fun setCount() {
        if (mViewModel.selectedMembersList.size < 8) {
            if (mViewModel.selectedMembersList.isNotEmpty()) {
                mViewModel.selectedMembersList.add(SelectMemberItem("", "", "ADD"))
            }
        } else {
            mViewModel.selectedMembersList[7].subTitle =
                    "${mViewModel.selectedMembersList.size - 8}"
        }
    }

    private fun goToAddMembersActivity() {
        val selectMembers = registerForActivityResult(EkoSelectMemberContract()) { list ->
            mViewModel.selectedMembersList.clear()
            mViewModel.selectedMembersList.addAll(list ?: arrayListOf())
            setAddMemberVisibility()
            setCount()
            mAdapter.submitList(mViewModel.selectedMembersList)
        }
        selectMembers.launch(mViewModel.selectedMembersList)
    }

    private fun setAddMemberVisibility() {
        mViewModel.addMemberVisible.set(mViewModel.selectedMembersList.isEmpty())
    }

    class Builder {
        fun build(activity: AppCompatActivity): EkoCommunityCreateFragment {
            return EkoCommunityCreateFragment()
        }
    }
}