package com.ekoapp.ekosdk.uikit.community.explore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.category.EkoCommunityCategory
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentCategoryCommunityListBinding
import com.ekoapp.ekosdk.uikit.community.detailpage.EkoCommunityPageActivity
import com.ekoapp.ekosdk.uikit.community.explore.adapter.EkoCategoryCommunityListAdapter
import com.ekoapp.ekosdk.uikit.community.explore.listener.IEkoCommunityItemClickListener
import com.ekoapp.ekosdk.uikit.community.explore.viewmodel.EkoCategoryCommunityListViewModel
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_category_community_list.*

const val ARG_CATEGORY_ID = "Category_id"
const val ARG_CATEGORY_NAME = "Category_name"

class EkoCategoryCommunityListFragment internal constructor() : EkoBaseFragment(),
        IEkoCommunityItemClickListener {
    private lateinit var mViewModel: EkoCategoryCommunityListViewModel
    private lateinit var adapter: EkoCategoryCommunityListAdapter
    private var categoryId: String? = null
    private var categoryName: String? = null
    lateinit var mBinding: AmityFragmentCategoryCommunityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = requireArguments().getString(ARG_CATEGORY_ID)
        categoryName = requireArguments().getString(ARG_CATEGORY_NAME)

        adapter = EkoCategoryCommunityListAdapter(
                EkoCategoryCommunityListAdapter.EkoCommunityDiffUtil(),
                this
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoCategoryCommunityListViewModel::class.java)
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.amity_fragment_category_community_list,
                container,
                false
        )
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolBar()
        initView()
        getCategories()
    }

    private fun setupToolBar() {
        categoryName?.let {
            (activity as AppCompatActivity).title = it
        }
    }

    private fun initView() {
        val itemDecorSpace =
                EkoRecyclerViewItemDecoration(resources.getDimensionPixelSize(R.dimen.eight))
        rvCommunity.layoutManager = LinearLayoutManager(requireContext())
        rvCommunity.adapter = adapter
        rvCommunity.addItemDecoration(itemDecorSpace)
    }

    private fun getCategories() {
        disposable.add(mViewModel.getCommunityByCategory(categoryId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    run {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }

                }
                .subscribe { result ->
                    run {
                        adapter.submitList(result)
                        handleListVisibility()
                    }
                })
    }

    private fun handleListVisibility() {
        rvCommunity.visibility = if (adapter.itemCount > 0) View.VISIBLE else View.GONE
        emptyView.visibility = if (adapter.itemCount > 0) View.GONE else View.VISIBLE
    }


    override fun onClick(community: EkoCommunity, position: Int) {
        if (mViewModel.communityItemClickListener != null) {
            mViewModel.communityItemClickListener?.onClick(community, position)
        } else {
            val detailIntent = EkoCommunityPageActivity
                    .newIntent(requireContext(), community.getCommunityId())
            startActivity(detailIntent)
        }
    }

    class Builder() {
        private var categoryId: String? = null
        private var category: EkoCommunityCategory? = null

        private var communityItemClickListener: IEkoCommunityItemClickListener? = null

        private fun communityItemClickListener(listener: IEkoCommunityItemClickListener): Builder {
            this.communityItemClickListener = listener
            return this
        }

        fun categoryId(categoryId: String): Builder {
            this.categoryId = categoryId
            return this
        }

        fun category(category: EkoCommunityCategory): Builder {
            this.category = category
            return this
        }

        fun build(activity: AppCompatActivity): EkoCategoryCommunityListFragment {
            if (categoryId == null && category == null)
                throw IllegalArgumentException("categoryId or category is required")
            val fragment = EkoCategoryCommunityListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId ?: category!!.getCategoryId())
                    putString(ARG_CATEGORY_NAME, category?.getName())
                }
            }
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoCategoryCommunityListViewModel::class.java)
            fragment.mViewModel.communityItemClickListener = communityItemClickListener
            return fragment
        }

    }

}
