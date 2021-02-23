package com.ekoapp.ekosdk.uikit.community.mycommunity.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.detailpage.EkoCommunityPageActivity
import com.ekoapp.ekosdk.uikit.community.mycommunity.activity.EkoMyCommunityActivity
import com.ekoapp.ekosdk.uikit.community.mycommunity.adapter.EkoMyCommunityListAdapter
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener
import com.ekoapp.ekosdk.uikit.community.mycommunity.viewmodel.EkoMyCommunityListViewModel
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoNewsFeedViewModel
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_my_community_list.*

class EkoMyCommunityPreviewFragment internal constructor() : EkoBaseFragment(),
        IMyCommunityItemClickListener {
    lateinit var mViewModel: EkoMyCommunityListViewModel
    private val newFeedViewModel: EkoNewsFeedViewModel by activityViewModels()
    private lateinit var mAdapter: EkoMyCommunityListAdapter
    private val TAG = EkoMyCommunityPreviewFragment::class.java.canonicalName

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoMyCommunityListViewModel::class.java)
        return inflater.inflate(R.layout.amity_fragment_my_community_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        ivMore.setOnClickListener {
            val myCommunityIntent = Intent(requireContext(), EkoMyCommunityActivity::class.java)
            startActivity(myCommunityIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        mAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        addItemTouchListener()
    }

    internal fun refresh() {
        getCommunityList()
        rvMyCommunity.smoothScrollToPosition(0)
    }

    private fun initRecyclerView() {
        mAdapter = EkoMyCommunityListAdapter(this, true)
        rvMyCommunity.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMyCommunity.adapter = mAdapter
        rvMyCommunity.itemAnimator = null
        rvMyCommunity.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        0, resources.getDimensionPixelSize(R.dimen.sixteen), 0,
                        resources.getDimensionPixelSize(R.dimen.sixteen)
                )
        )
        rvMyCommunity.setHasFixedSize(true)
        getCommunityList()
    }

    private fun getCommunityList() {
        disposable.add(mViewModel.getCommunityList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { list ->
                    mAdapter.submitList(list)
                    newFeedViewModel.triggerEvent(
                            EventIdentifier.EMPTY_MY_COMMUNITY,
                            list.size == 0
                    )
                }
                .doOnError {
                    Log.e(TAG, "initRecyclerView: ${it.localizedMessage}")
                }
                .subscribe()
        )
    }

    private fun addItemTouchListener() {
        val touchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                return if (rvMyCommunity.canScrollHorizontally(RecyclerView.FOCUS_FORWARD)) {
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
        rvMyCommunity.addOnItemTouchListener(touchListener)
    }

    override fun onCommunitySelected(ekoCommunity: EkoCommunity?) {
        if (ekoCommunity != null) {
            val detailIntent = EkoCommunityPageActivity.newIntent(
                    requireContext(),
                    ekoCommunity.getCommunityId()
            )
            startActivity(detailIntent)
        } else {
            val myCommunityIntent = Intent(requireContext(), EkoMyCommunityActivity::class.java)
            startActivity(myCommunityIntent)
        }


    }


    class Builder {
        private var myCommunityItemClickListener: IMyCommunityItemClickListener? = null

        fun build(activity: AppCompatActivity): EkoMyCommunityPreviewFragment {
            val fragment = EkoMyCommunityPreviewFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoMyCommunityListViewModel::class.java)
            fragment.mViewModel.myCommunityItemClickListener = myCommunityItemClickListener
            return fragment
        }

        private fun myCommunityItemClickListener(listener: IMyCommunityItemClickListener): Builder {
            this.myCommunityItemClickListener = listener
            return this
        }
    }


}