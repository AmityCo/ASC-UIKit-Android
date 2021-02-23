package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentPostTargetSelectionBinding
import com.ekoapp.ekosdk.uikit.community.newsfeed.activity.EkoCreatePostActivity
import com.ekoapp.ekosdk.uikit.community.newsfeed.adapter.EkoCreatePostCommunitySelectionAdapter
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.ICreatePostCommunitySelectionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoCreatePostRoleSelectionViewModel
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_post_target_selection.*

class EkoPostTargetSelectionFragment internal constructor() : EkoBaseFragment(),
        ICreatePostCommunitySelectionListener {
    private val mViewModel: EkoCreatePostRoleSelectionViewModel by activityViewModels()
    private lateinit var mAdapter: EkoCreatePostCommunitySelectionAdapter
    private lateinit var mBinding: AmityFragmentPostTargetSelectionBinding
    private val TAG = EkoPostTargetSelectionFragment::class.java.canonicalName

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.amity_fragment_post_target_selection,
                container,
                false
        )
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initProfileImage()
        clMyTimeline.setOnClickListener {
            navigateToCreatePost(null)
        }
    }

    private fun initProfileImage() {
        val user = mViewModel.getUser()
        val imageURL = user.getAvatar()?.getUrl(EkoImage.Size.MEDIUM)
        Glide.with(this)
                .load(imageURL)
                .placeholder(R.drawable.amity_ic_default_profile_large)
                .centerCrop()
                .into(avProfile)
    }

    private fun initRecyclerView() {
        mAdapter = EkoCreatePostCommunitySelectionAdapter(this)
        rvCommunity.layoutManager = LinearLayoutManager(requireContext())
        rvCommunity.adapter = mAdapter
        rvCommunity.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.eight),
                        0, resources.getDimensionPixelSize(R.dimen.eight)
                )
        )
        rvCommunity.hasFixedSize()
        mViewModel.getCommunityList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    mAdapter.submitList(it)
                    handleCommunitySectionVisibility()
                }.doOnError {
                    Log.e(TAG, "initRecyclerView: ${it.localizedMessage}")
                }.subscribe()
    }

    private fun handleCommunitySectionVisibility() {
        val communitySectionVisibility = if (mAdapter.itemCount > 0) View.VISIBLE else View.GONE
        mBinding.separator.visibility = communitySectionVisibility
        mBinding.tvCommunityLabel.visibility = communitySectionVisibility
    }


    override fun onClickCommunity(community: EkoCommunity, position: Int) {
        navigateToCreatePost(community)
        // EkoCommunityNavigation.navigateToCreatePost(requireContext(), community)
    }

    private fun navigateToCreatePost(community: EkoCommunity?) {
        val createPost =
                registerForActivityResult(EkoCreatePostActivity.EkoCreateCommunityPostActivityContract()) { data ->
                    activity?.finish()
                }
        createPost.launch(community)
    }

    class Builder() {
        fun build(activity: AppCompatActivity): EkoPostTargetSelectionFragment {
            return EkoPostTargetSelectionFragment()
        }
    }
}