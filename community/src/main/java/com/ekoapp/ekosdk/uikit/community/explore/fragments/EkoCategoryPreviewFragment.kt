package com.ekoapp.ekosdk.uikit.community.explore.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ekoapp.ekosdk.community.category.EkoCommunityCategory
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentCategoryPreviewBinding
import com.ekoapp.ekosdk.uikit.community.explore.activity.EkoCategoryCommunityListActivity
import com.ekoapp.ekosdk.uikit.community.explore.activity.EkoCategoryListActivity
import com.ekoapp.ekosdk.uikit.community.explore.adapter.EkoCommunityCategoryAdapter
import com.ekoapp.ekosdk.uikit.community.explore.listener.IEkoCategoryItemClickListener
import com.ekoapp.ekosdk.uikit.community.explore.viewmodel.EkoExploreCommunityViewModel
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_category_preview.*

class EkoCategoryPreviewFragment internal constructor() : EkoBaseFragment(),
        IEkoCategoryItemClickListener {

    private val TAG = EkoCategoryPreviewFragment::class.java.canonicalName
    private lateinit var mViewModel: EkoExploreCommunityViewModel
    private lateinit var mAdapter: EkoCommunityCategoryAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoExploreCommunityViewModel::class.java)
        val binding: AmityFragmentCategoryPreviewBinding = DataBindingUtil.inflate(
                inflater, R.layout.amity_fragment_category_preview, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
        ivMore.setOnClickListener {
            val intent = Intent(requireContext(), EkoCategoryListActivity::class.java)
            startActivity(intent)
        }
    }

    internal fun refresh() {
        getCategories()
    }

    private fun initializeRecyclerView() {
        mAdapter = EkoCommunityCategoryAdapter(this)
        rvCommunityCategory.layoutManager = GridLayoutManager(requireContext(), 2)
        rvCommunityCategory.adapter = mAdapter
        rvCommunityCategory.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        requireContext().resources.getDimensionPixelSize(R.dimen.twelve)
                )
        )
        rvCommunityCategory.itemAnimator = null
        rvCommunityCategory.hasFixedSize()
        getCategories()
    }

    private fun getCategories() {
        disposable.add(mViewModel.getCommunityCategory().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    mViewModel.emptyCategoryList.set(it.size == 0)
                    mAdapter.submitList(it)
                }.doOnError {
                    Log.e(TAG, "getCommunityCategory: ${it.localizedMessage}")
                }.subscribe()
        )
    }

    override fun onCategorySelected(category: EkoCommunityCategory) {
        if (mViewModel.categoryItemClickListener != null) {
            mViewModel.categoryItemClickListener!!.onCategorySelected(category)
        } else {
            val intent = EkoCategoryCommunityListActivity.newIntent(requireContext(), category)
            startActivity(intent)
        }
    }

    class Builder {
        private var categoryItemClickListener: IEkoCategoryItemClickListener? = null

        fun build(activity: AppCompatActivity): EkoCategoryPreviewFragment {
            val fragment = EkoCategoryPreviewFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoExploreCommunityViewModel::class.java)
            fragment.mViewModel.categoryItemClickListener = categoryItemClickListener
            return fragment
        }

        private fun categoryItemClickListener(listener: IEkoCategoryItemClickListener) {
            this.categoryItemClickListener = listener
        }

    }

}
