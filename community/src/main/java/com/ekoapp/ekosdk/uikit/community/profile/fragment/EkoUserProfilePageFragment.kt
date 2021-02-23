package com.ekoapp.ekosdk.uikit.community.profile.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.base.EkoFragmentStateAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentUserProfilePageBinding
import com.ekoapp.ekosdk.uikit.community.newsfeed.fragment.EkoMyFeedFragment
import com.ekoapp.ekosdk.uikit.community.newsfeed.fragment.EkoUserFeedFragment
import com.ekoapp.ekosdk.uikit.community.profile.activity.EkoEditUserProfileActivity
import com.ekoapp.ekosdk.uikit.community.profile.listener.IEditUserProfileClickListener
import com.ekoapp.ekosdk.uikit.community.profile.listener.IFeedFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.profile.viewmodel.EkoUserProfileViewModel
import com.ekoapp.ekosdk.uikit.community.utils.EkoCommunityNavigation
import com.ekoapp.ekosdk.user.EkoUser
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_community_home_page.tabLayout
import kotlinx.android.synthetic.main.amity_fragment_user_profile_page.appBar
import kotlinx.android.synthetic.main.amity_fragment_user_profile_page.fabCreatePost
import kotlinx.android.synthetic.main.amity_fragment_user_profile_page.refreshLayout

const val ARG_USER_ID = "com.ekoapp.ekosdk.uikit.community.profile.userid"

class EkoUserProfilePageFragment internal constructor() : EkoBaseFragment(),
        AppBarLayout.OnOffsetChangedListener {
    private var TAG = EkoUserProfilePageFragment::class.java.canonicalName

    lateinit var mViewModel: EkoUserProfileViewModel
    private lateinit var fragmentStateAdapter: EkoFragmentStateAdapter
    private lateinit var mBinding: AmityFragmentUserProfilePageBinding

    private var isRefreshing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.userId = requireArguments().getString(ARG_USER_ID)

        fragmentStateAdapter = EkoFragmentStateAdapter(
                childFragmentManager,
                requireActivity().lifecycle
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(requireActivity()).get(EkoUserProfileViewModel::class.java)
        mBinding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.amity_fragment_user_profile_page,
                        container,
                        false
                )
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabLayout()
        appBar.setExpanded(true)
        getUserDetails()
        fabCreatePost.setOnClickListener {
            EkoCommunityNavigation.navigateToCreatePost(requireContext())
        }

        mBinding.userProfileHeader.btnUserProfileAction.setOnClickListener {
            if (mViewModel.editUserProfileClickListener != null) {
                mViewModel.editUserProfileClickListener?.onClickEditUserProfile(mViewModel.userId)
            } else {
                if (mViewModel.isLoggedInUser()) {
                    activity?.also {
                        val intent = EkoEditUserProfileActivity.newIntent(it)
                        startActivity(intent)
                    }
                }
            }
        }

        refreshLayout.setColorSchemeResources(R.color.amityColorPrimary)
        refreshLayout.setOnRefreshListener {
            refreshFeed()
        }
    }

    override fun onStart() {
        super.onStart()
        if (isRefreshing) {
            refreshLayout?.isRefreshing = true
            refreshFeed()
        }
        isRefreshing = true
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        refreshLayout.isEnabled = (verticalOffset == 0)
    }

    override fun onResume() {
        super.onResume()
        appBar.addOnOffsetChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        appBar.removeOnOffsetChangedListener(this)
    }

    private fun refreshFeed() {
        childFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is EkoMyFeedFragment -> {
                    fragment.refresh()
                }
                is EkoUserFeedFragment -> {
                    fragment.refresh()
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            refreshLayout?.isRefreshing = false
        }, 1000)
    }

    private fun getUserDetails() {
        mBinding.userProfileHeader.setIsLoggedInUser(mViewModel.isLoggedInUser())
        mViewModel.getUser()?.let {
            disposable.add(
                    it
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ result ->
                                mBinding.userProfileHeader.setUserData(result)
                                fabCreatePost.visibility =
                                        if (mViewModel.isLoggedInUser()) View.VISIBLE else View.GONE
                            }, {
                                Log.d(TAG, it.message ?: "")
                            })
            )
        }
    }

    private fun initTabLayout() {
        fragmentStateAdapter.setFragmentList(
                arrayListOf(
                        EkoFragmentStateAdapter.EkoPagerModel(
                                getString(R.string.amity_timeline),
                                getTimeLineFragment()
                        )
                )
        )
        tabLayout.setAdapter(fragmentStateAdapter)
    }

    private fun getTimeLineFragment(): Fragment {
        if (mViewModel.feedFragmentDelegate != null)
            return mViewModel.feedFragmentDelegate!!.getFeedFragment()
        if (mViewModel.isLoggedInUser()) {
            return EkoMyFeedFragment()
        } else {
            return EkoUserFeedFragment.Builder().userId(mViewModel.userId!!)
                    .build(activity as AppCompatActivity)
        }
    }

    class Builder() {
        private var userId: String? = null
        var feedFragmentDelegate: IFeedFragmentDelegate? = null
        var editUserProfileClickListener: IEditUserProfileClickListener? = null
        fun build(activity: AppCompatActivity): EkoUserProfilePageFragment {
            if (userId == null)
                throw IllegalArgumentException("Require userId or user")
            val fragment = EkoUserProfilePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, this@Builder.userId)
                }
            }

            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoUserProfileViewModel::class.java)
            fragment.mViewModel.feedFragmentDelegate = feedFragmentDelegate
            fragment.mViewModel.editUserProfileClickListener = editUserProfileClickListener
            return fragment
        }

        fun userId(userId: String): Builder {
            this.userId = userId
            return this
        }

        fun user(user: EkoUser): Builder {
            this.userId = user.getUserId()
            return this
        }

        fun feedFragmentDelegate(delegate: IFeedFragmentDelegate): Builder {
            this.feedFragmentDelegate = delegate
            return this
        }

        fun onClickEditUserProfile(onEditUserProfileClickListener: IEditUserProfileClickListener): Builder {
            return apply { this.editUserProfileClickListener = onEditUserProfileClickListener }
        }
    }
}