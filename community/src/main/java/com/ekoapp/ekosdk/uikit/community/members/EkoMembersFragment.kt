package com.ekoapp.ekosdk.uikit.community.members

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.membership.EkoCommunityMembership
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.common.setShape
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.community.profile.activity.EkoUserProfileActivity
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_members.*

/**
 * A simple [Fragment] subclass.
 * Use the [EkoMembersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EkoMembersFragment : EkoBaseFragment(), IMemberClickListener {

    private val TAG = EkoMembersFragment::class.java.canonicalName
    private val mViewModel: EkoCommunityMembersViewModel by activityViewModels()
    private lateinit var mAdapter: EkoCommunityMembersAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.amity_fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.membersSet.clear()
        mViewModel.selectMembersList.clear()
        subscribeObservers()
        initRecyclerView()

        etSearch.setShape(
                null, null, null, null,
                R.color.amityColorBase, null, ColorShade.SHADE4
        )
    }

    private fun subscribeObservers() {
        mViewModel.setPropertyChangeCallback()

        mViewModel.onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.SEARCH_STRING_CHANGED -> searchMembers()
                else -> {
                }
            }
        }
    }

    private fun initRecyclerView() {
        mAdapter = EkoCommunityMembersAdapter(requireContext(), this, mViewModel)
        rvCommunityMembers.layoutManager = LinearLayoutManager(requireContext())
        rvCommunityMembers.adapter = mAdapter
        rvCommunityMembers.addItemDecoration(
                EkoRecyclerViewItemDecoration(requireContext().resources.getDimensionPixelSize(R.dimen.sixteen))
        )
    }

    override fun onResume() {
        super.onResume()
        mViewModel.community?.let { community ->
            getCommunityMembers(community)
        } ?: kotlin.run {
            getCommunityDetail()
        }
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    private fun getCommunityDetail() {
        disposable.add(mViewModel.getCommunityDetail()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { community ->
                    getCommunityMembers(community)
                }.doOnError {
                    Log.e(TAG, "getCommunityDetail: ${it.localizedMessage}")
                }.subscribe()
        )
    }

    private fun getCommunityMembers(community: EkoCommunity) {
        mAdapter.setUserIsJoined(community.isJoined())
        mViewModel.isJoined.set(community.isJoined())
        mViewModel.communityId = community.getCommunityId()
        mViewModel.getCommunityMembers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    mViewModel.emptyMembersList.set(it.size == 0)
                    mAdapter.submitList(it)
                    if (!mViewModel.emptyMembersList.get()) {
                        prepareSelectedMembersList(it)
                    }
                }.doOnError {
                    Log.e(TAG, "getCommunityMembers: ${it.localizedMessage}")
                }.subscribe()
    }

    private fun prepareSelectedMembersList(list: PagedList<EkoCommunityMembership>) {
        list.forEach {
            val ekoUser = it.getUser()
            if (ekoUser != null) {
                val selectMemberItem = SelectMemberItem(
                        ekoUser.getUserId(),
                        ekoUser.getAvatar()?.getUrl(EkoImage.Size.MEDIUM) ?: "",
                        ekoUser.getDisplayName() ?: getString(R.string.amity_anonymous),
                        ekoUser.getDescription(),
                        false
                )
                if (!mViewModel.membersSet.contains(selectMemberItem.id)) {
                    mViewModel.selectMembersList.add(selectMemberItem)
                    mViewModel.membersSet.add(selectMemberItem.id)
                }
            }

        }
    }

    private fun searchMembers() {

    }

    override fun onCommunityMembershipSelected(membership: EkoCommunityMembership) {
        val intent = EkoUserProfileActivity.newIntent(requireContext(), membership.getUserId())
        startActivity(intent)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment EkoMembersFragment.
         */
        @JvmStatic
        fun newInstance() = EkoMembersFragment()

    }
}