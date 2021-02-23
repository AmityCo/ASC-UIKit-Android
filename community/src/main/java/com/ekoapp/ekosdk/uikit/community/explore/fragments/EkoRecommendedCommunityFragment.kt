package com.ekoapp.ekosdk.uikit.community.explore.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.detailpage.EkoCommunityPageActivity
import com.ekoapp.ekosdk.uikit.community.explore.adapter.EkoRCommunityItemDecoration
import com.ekoapp.ekosdk.uikit.community.explore.adapter.EkoRecommendedCommunitiesAdapter
import com.ekoapp.ekosdk.uikit.community.explore.viewmodel.EkoExploreCommunityViewModel
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_recommended_community.*

class EkoRecommendedCommunityFragment : EkoBaseFragment(), IMyCommunityItemClickListener {

    lateinit var mViewModel: EkoExploreCommunityViewModel
    private lateinit var mAdapter: EkoRecommendedCommunitiesAdapter
    private val TAG = EkoRecommendedCommunityFragment::class.java.canonicalName

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoExploreCommunityViewModel::class.java)
        return inflater.inflate(R.layout.amity_fragment_recommended_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        addItemTouchListener()
    }

    internal fun refresh() {
        getRecommendedCommunity()
    }

    private fun initializeRecyclerView() {
        mAdapter = EkoRecommendedCommunitiesAdapter(this)
        rvRecommCommunity.layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        rvRecommCommunity.adapter = mAdapter
        rvRecommCommunity.addItemDecoration(
                EkoRCommunityItemDecoration(
                        requireContext().resources.getDimensionPixelSize(R.dimen.ten),
                        requireContext().resources.getDimensionPixelSize(R.dimen.eight),
                        requireContext().resources.getDimensionPixelSize(R.dimen.eighteen),
                        requireContext().resources.getDimensionPixelSize(R.dimen.eight)
                )
        )
        rvRecommCommunity.setHasFixedSize(true)

        getRecommendedCommunity()
    }

    private fun getRecommendedCommunity() {
        disposable.add(mViewModel.getRecommendedCommunity().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    mViewModel.emptyRecommendedList.set(it.size == 0)
                    mAdapter.submitList(it)
                }.doOnError {
                    Log.e(TAG, "getRecommendedCommunity: ${it.localizedMessage}")
                }.subscribe()
        )
    }

    private fun addItemTouchListener() {
        val touchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                return if (rvRecommCommunity.canScrollHorizontally(RecyclerView.FOCUS_FORWARD)) {
                    when (action) {
                        MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    false
                } else {
                    when (action) {
                        MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(
                                false
                        )
                    }
                    false
                }
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        }
        rvRecommCommunity.addOnItemTouchListener(touchListener)
    }

    override fun onCommunitySelected(ekoCommunity: EkoCommunity?) {
        if (mViewModel.recommendedCommunityItemClickListener != null) {
            mViewModel.recommendedCommunityItemClickListener!!.onCommunitySelected(ekoCommunity)
        } else {
            navigateToCommunityDetails(ekoCommunity)
        }
    }

    private fun navigateToCommunityDetails(ekoCommunity: EkoCommunity?) {
        ekoCommunity?.let {
            navigateToCommunityDetails(communityId = ekoCommunity.getCommunityId())
        }
    }

    private fun navigateToCommunityDetails(communityId: String) {
        val intent = EkoCommunityPageActivity.newIntent(
                requireContext(),
                communityId
        )
        startActivity(intent)
    }

    class Builder {
        private var communityItemClickListener: IMyCommunityItemClickListener? = null

        fun build(activity: AppCompatActivity): EkoRecommendedCommunityFragment {
            val fragment = EkoRecommendedCommunityFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoExploreCommunityViewModel::class.java)
            fragment.mViewModel.recommendedCommunityItemClickListener = communityItemClickListener
            return fragment
        }

        private fun communityItemClickListener(listener: IMyCommunityItemClickListener) {
            this.communityItemClickListener = listener
        }

    }
}
