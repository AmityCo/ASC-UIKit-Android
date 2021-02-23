package com.ekoapp.ekosdk.uikit.community.explore.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.detailpage.EkoCommunityPageActivity
import com.ekoapp.ekosdk.uikit.community.explore.adapter.EkoTrendingCommunityAdapter
import com.ekoapp.ekosdk.uikit.community.explore.viewmodel.EkoExploreCommunityViewModel
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_trending_community.*

class EkoTrendingCommunityFragment : EkoBaseFragment(), IMyCommunityItemClickListener {

    private lateinit var adapter: EkoTrendingCommunityAdapter
    lateinit var mViewModel: EkoExploreCommunityViewModel
    private val TAG = EkoTrendingCommunityFragment::class.java.canonicalName

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoExploreCommunityViewModel::class.java)
        return inflater.inflate(R.layout.amity_fragment_trending_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
    }

    internal fun refresh() {
        getTrendingCommunity()
    }

    private fun initializeRecyclerView() {
        adapter = EkoTrendingCommunityAdapter(this)
        rvTrendingCommunity.layoutManager = LinearLayoutManager(requireContext())
        rvTrendingCommunity.adapter = adapter
        rvTrendingCommunity.itemAnimator = null
        rvTrendingCommunity.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        requireContext().resources.getDimensionPixelSize(R.dimen.sixteen),
                        requireContext().resources.getDimensionPixelSize(R.dimen.zero),
                        requireContext().resources.getDimensionPixelSize(R.dimen.eight)
                )
        )
        getTrendingCommunity()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun getTrendingCommunity() {
        disposable.add(mViewModel.getTrendingCommunity().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    mViewModel.emptyTrendingList.set(it.size == 0)
                    adapter.submitList(it)
                }.doOnError {
                    Log.e(TAG, "getTrendingCommunity: ${it.localizedMessage}")
                }.subscribe()
        )
    }

    override fun onCommunitySelected(ekoCommunity: EkoCommunity?) {
        if (mViewModel.trendingCommunityItemClickListener != null)
            mViewModel.trendingCommunityItemClickListener!!.onCommunitySelected(ekoCommunity)
        else {
            if (ekoCommunity != null) {
                val intent = EkoCommunityPageActivity.newIntent(
                        requireContext(),
                        ekoCommunity.getCommunityId()
                )
                startActivity(intent)
            }
        }
    }

    class Builder {
        private var communityItemClickListener: IMyCommunityItemClickListener? = null

        fun build(activity: AppCompatActivity): EkoTrendingCommunityFragment {
            val fragment = EkoTrendingCommunityFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoExploreCommunityViewModel::class.java)
            fragment.mViewModel.trendingCommunityItemClickListener = communityItemClickListener
            return fragment
        }

        private fun communityItemClickListener(listener: IMyCommunityItemClickListener) {
            this.communityItemClickListener = listener
        }

    }

}
