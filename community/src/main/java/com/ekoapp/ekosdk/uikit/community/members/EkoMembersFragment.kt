package com.ekoapp.ekosdk.uikit.community.members

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.community.membership.EkoCommunityMembership
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.common.setShape
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.profile.activity.EkoUserProfileActivity
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_eko_members.*

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
        return inflater.inflate(R.layout.fragment_eko_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
        initRecyclerView()

        etSearch.setShape(null, null, null, null,
            R.color.upstraColorBase, null, ColorShade.SHADE4)
    }

    private fun subscribeObservers() {
        mViewModel.setPropertyChangeCallback()

        mViewModel.onEventReceived += {event->
            when(event.type) {
                EventIdentifier.SEARCH_STRING_CHANGED -> searchMembers()
                else -> {}
            }
        }
    }

    private fun initRecyclerView() {
        mAdapter = EkoCommunityMembersAdapter(requireContext(), this)
        rvCommunityMembers.layoutManager = LinearLayoutManager(requireContext())
        rvCommunityMembers.adapter = mAdapter
        rvCommunityMembers.addItemDecoration(EkoRecyclerViewItemDecoration(
            requireContext().resources.getDimensionPixelSize(R.dimen.sixteen)
        ))

        disposable.add(mViewModel.getCommunityMembers().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                mViewModel.emptyMembersList.set(it.size == 0)
                mAdapter.submitList(it)
            }.doOnError {
                Log.e(TAG, "getCommunityMembers: ${it.localizedMessage}")
            }.subscribe()
        )
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