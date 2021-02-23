package com.ekoapp.ekosdk.uikit.community.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.common.setShape
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentSelectMembersListBinding
import com.ekoapp.ekosdk.uikit.community.ui.adapter.EkoMembersListAdapter
import com.ekoapp.ekosdk.uikit.community.ui.adapter.EkoSearchResultAdapter
import com.ekoapp.ekosdk.uikit.community.ui.adapter.EkoSelectedMemberAdapter
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoSelectMemberListener
import com.ekoapp.ekosdk.uikit.community.ui.clickListener.EkoSelectedMemberListener
import com.ekoapp.ekosdk.uikit.community.ui.viewModel.EkoSelectMembersViewModel
import com.ekoapp.ekosdk.uikit.community.utils.SelectMembersItemDecoration
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.EkoConstants
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import com.ekoapp.ekosdk.user.EkoUser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_select_members_list.*

private const val ARG_MEMBERS_LIST = "ARG_MEMBERS_LIST"

class EkoSelectMembersListFragment internal constructor() : Fragment(), EkoSelectMemberListener,
        EkoSelectedMemberListener {

    private val mViewModel: EkoSelectMembersViewModel by activityViewModels()
    private lateinit var mSelectedMembersAdapter: EkoSelectedMemberAdapter
    private lateinit var mMemberListAdapter: EkoMembersListAdapter
    private lateinit var mSearchResultAdapter: EkoSearchResultAdapter
    private val receivedMembersList = arrayListOf<SelectMemberItem>()
    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }
    private lateinit var mBinding: AmityFragmentSelectMembersListBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.amity_fragment_select_members_list,
                container,
                false
        )
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        setUpSelectedMemberRecyclerView()
        setUpSearchRecyclerView()

        handleSelectedMembers()
        setUpMembersListRecyclerView()

        etSearch.setShape(null, null, null, null, R.color.amityColorBase, null, ColorShade.SHADE4)
    }


    private fun handleSelectedMembers() {
        val list = arguments?.getParcelableArrayList<SelectMemberItem>(ARG_MEMBERS_LIST)
        if (list != null && list.isNotEmpty()) {
            if (list[list.size - 1].name == "ADD") {
                list.removeAt(list.size - 1)
            }
            mViewModel.selectedMembersList.clear()
            receivedMembersList.addAll(list)
            for (item in list) {
                mViewModel.selectedMemberSet.add(item.id)
                mViewModel.prepareSelectedMembersList(item, true)
            }
            mSelectedMembersAdapter.submitList(mViewModel.selectedMembersList)
        }
        setToolBarState()
    }

    private fun setToolBarState() {
        if (mViewModel.selectedMembersList.size != 0) {
            mViewModel.leftString.value =
                    "${mViewModel.selectedMembersList.size} ${getString(R.string.amity_selected)}"
            mViewModel.rightStringActive.value = true
        } else {
            mViewModel.leftString.value = getString(R.string.amity_select_members)
            mViewModel.rightStringActive.value = false
        }
    }

    private fun subscribeObservers() {
        mViewModel.setPropertyChangeCallback()

        mViewModel.onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.SEARCH_STRING_CHANGED -> loadFilteredList()
                else -> {

                }
            }
        }
    }

    private fun setUpSelectedMemberRecyclerView() {
        mSelectedMembersAdapter = EkoSelectedMemberAdapter(this)
        val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvSelectedMembers.layoutManager = layoutManager
        rvSelectedMembers.adapter = mSelectedMembersAdapter
        rvSelectedMembers.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        0,
                        resources.getDimensionPixelSize(R.dimen.nineteen),
                        0,
                        resources.getDimensionPixelSize(R.dimen.four)
                )
        )
    }

    private fun setUpMembersListRecyclerView() {
        mMemberListAdapter = EkoMembersListAdapter(this, mViewModel)
        rvMembersList.layoutManager = LinearLayoutManager(requireContext())
        rvMembersList.adapter = mMemberListAdapter
        rvMembersList.addItemDecoration(
                SelectMembersItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.eighteen),
                        resources.getDimensionPixelSize(R.dimen.sixteen)
                )
        )
        disposable.add(mViewModel.getAllUsers().doOnError {
            Log.e(
                    "EkoSelectMemberActivity",
                    "setUpMembersListRecyclerView: ${it.localizedMessage}"
            )
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list ->
                    mMemberListAdapter.submitPagedList(list, mViewModel.selectedMemberSet)
                })
    }

    private fun setUpSearchRecyclerView() {
        mSearchResultAdapter = EkoSearchResultAdapter(this)
        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.adapter = mSearchResultAdapter
        rvSearchResults.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.sixteen)
                )
        )
        (rvSearchResults.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun onMemberClicked(member: EkoUser, position: Int) {
        val selectMemberItem = SelectMemberItem(
                member.getUserId(),
                member.getAvatar()?.getUrl(EkoImage.Size.MEDIUM) ?: "",
                member.getDisplayName()
                        ?: getString(R.string.amity_anonymous), member.getDescription(), false
        )
        if (mViewModel.selectedMemberSet.contains(member.getUserId())) {
            mViewModel.prepareSelectedMembersList(selectMemberItem, false)
            mViewModel.selectedMemberSet.remove(member.getUserId())
            updateListOnSelection(member.getUserId())
        } else {
            mViewModel.selectedMemberSet.add(member.getUserId())
            mViewModel.prepareSelectedMembersList(selectMemberItem, true)
            updateListOnSelection(member.getUserId())
            rvSelectedMembers.scrollToPosition(mViewModel.selectedMembersList.size - 1)
        }
    }

    override fun onMemberRemoved(member: SelectMemberItem) {
        mViewModel.prepareSelectedMembersList(member, false)
        mViewModel.selectedMemberSet.remove(member.id)
        updateListOnSelection(member.id)
    }

    private fun updateListOnSelection(id: String) {
        mSelectedMembersAdapter.submitList(mViewModel.selectedMembersList)
        val position = mViewModel.memberMap[id]
        if (position != null) {
            mMemberListAdapter.notifyChange(position, mViewModel.selectedMemberSet)
        }

        if (mSearchResultAdapter.itemCount > 0 && mViewModel.searchMemberMap[id] != null) {
            mSearchResultAdapter.notifyChange(
                    mViewModel.searchMemberMap[id]!!,
                    mViewModel.selectedMemberSet
            )
        }
        setToolBarState()
    }

    private fun loadFilteredList() {
        disposable.add(mViewModel.searchUser().subscribe { list ->
            mSearchResultAdapter.submitPagedList(list, mViewModel.selectedMemberSet)
            mViewModel.isSearchUser.set(list.size > 0)
            prepareSearchMap(list)
        })
    }

    private fun prepareSearchMap(list: PagedList<EkoUser>) {
        for (i in 0 until list.size) {
            mViewModel.searchMemberMap[list[i]!!.getUserId()] = i
        }
    }

    fun finishActivity(isCancel: Boolean) {
        val finishIntent = Intent()
        if (isCancel) {
            finishIntent.putParcelableArrayListExtra(EkoConstants.MEMBERS_LIST, receivedMembersList)
            requireActivity().setResult(Activity.RESULT_OK, finishIntent)
        } else {
            finishIntent.putParcelableArrayListExtra(
                    EkoConstants.MEMBERS_LIST,
                    mViewModel.selectedMembersList
            )
            requireActivity().setResult(Activity.RESULT_OK, finishIntent)
        }
        requireActivity().finish()
    }

    override fun onDestroy() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        super.onDestroy()
    }

    class Builder {
        private var selectedMembersList: ArrayList<SelectMemberItem>? = null

        fun build(activity: AppCompatActivity): EkoSelectMembersListFragment {
            return EkoSelectMembersListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_MEMBERS_LIST, selectedMembersList)
                }
            }
        }

        fun selectedMembers(list: ArrayList<SelectMemberItem>?): Builder {
            selectedMembersList = list
            return this
        }
    }
}