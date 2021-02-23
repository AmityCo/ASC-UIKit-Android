package com.ekoapp.ekosdk.uikit.community.home.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoFragmentStateAdapter
import com.ekoapp.ekosdk.uikit.common.views.ColorPaletteUtil
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentCommunityHomePageBinding
import com.ekoapp.ekosdk.uikit.community.detailpage.EkoCommunityPageActivity
import com.ekoapp.ekosdk.uikit.community.explore.fragments.EkoExploreFragment
import com.ekoapp.ekosdk.uikit.community.home.listener.IExploreFragmentFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.home.listener.INewsFeedFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.mycommunity.adapter.EkoMyCommunitiesAdapter
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.fragment.EkoNewsFeedFragment
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.AndroidUtil
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.amity_fragment_community_home_page.*
import java.util.concurrent.TimeUnit


class EkoCommunityHomePageFragment internal constructor() : Fragment(),
        IMyCommunityItemClickListener {

    private lateinit var fragmentStateAdapter: EkoFragmentStateAdapter
    private lateinit var searchMenuItem: MenuItem
    lateinit var mViewModel: EkoCommunityHomeViewModel
    private lateinit var mBinding: AmityFragmentCommunityHomePageBinding
    private lateinit var mSearchAdapter: EkoMyCommunitiesAdapter
    private var searchResultDisposable: Disposable? = null
    private var textChangeDisposable: Disposable? = null
    private val textChangeSubject: PublishSubject<String> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentStateAdapter = EkoFragmentStateAdapter(
                childFragmentManager,
                requireActivity().lifecycle
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(requireActivity()).get(EkoCommunityHomeViewModel::class.java)
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.amity_fragment_community_home_page,
                container,
                false
        )
        mBinding.viewModel = mViewModel
        mBinding.tabLayout.disableSwipe()
//        mBinding.tabLayout.setOffscreenPageLimit(2)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initTabLayout()
        initSearchRecyclerview()
        addViewModelListeners()
        subscribeTextChangeEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (searchResultDisposable?.isDisposed == false) {
            searchResultDisposable?.dispose()
        }
        if (textChangeDisposable?.isDisposed == false) {
            textChangeDisposable?.dispose()
        }
    }

    private fun initTabLayout() {
        fragmentStateAdapter.setFragmentList(
                arrayListOf(
                        EkoFragmentStateAdapter.EkoPagerModel(
                                getString(R.string.amity_title_news_feed),
                                getNewsFeedFragment()
                        ),
                        EkoFragmentStateAdapter.EkoPagerModel(
                                getString(R.string.amity_title_explore),
                                getExploreFragment()
                        )
                )
        )
        tabLayout.setAdapter(fragmentStateAdapter)
    }

    private fun getExploreFragment(): Fragment {
        if (mViewModel.exploreFragmentDelegate != null)
            return mViewModel.exploreFragmentDelegate!!.getExploreFragment()
        return EkoExploreFragment.Builder().build(activity as AppCompatActivity)
    }

    private fun getNewsFeedFragment(): Fragment {
        if (mViewModel.newsFeedFragmentDelegate != null)
            return mViewModel.newsFeedFragmentDelegate!!.getNewsFeedFragment()
        return EkoNewsFeedFragment.Builder().build(activity as AppCompatActivity)
    }

    private fun addViewModelListeners() {
        mViewModel.onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.EXPLORE_COMMUNITY -> {
                    //searchMenuItem.expandActionView()
                    tabLayout.switchTab(1)
                }
                else -> {

                }
            }
        }
    }

    private fun initSearchRecyclerview() {
        mSearchAdapter = EkoMyCommunitiesAdapter(this)
        rvCommunitySearch.layoutManager = LinearLayoutManager(requireContext())
        rvCommunitySearch.adapter = mSearchAdapter
        rvCommunitySearch.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.sixteen)
                )
        )
        rvCommunitySearch.setHasFixedSize(true)
    }

    private fun subscribeTextChangeEvents() {
        textChangeDisposable = textChangeSubject.debounce(500, TimeUnit.MILLISECONDS)
                .map { searchCommunity(it) }
                .subscribe()
    }

    private fun searchCommunity(newText: String) {
        if (newText.isEmpty()) {
            mViewModel.emptySearchString.set(true)
        } else {
            mViewModel.emptySearchString.set(false)
            searchResultDisposable?.dispose()
            searchResultDisposable = mViewModel.searchCommunity(newText)
                    .throttleLatest(1, TimeUnit.SECONDS, true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { list ->
                        if (list.isEmpty()) {
                            mViewModel.emptySearch.set(true)
                        } else {
                            mViewModel.emptySearch.set(false)
                        }
                        if (newText.isNotEmpty()) {
                            mSearchAdapter.submitList(list)
                        }
                    }.doOnError {
                        Log.e("CommunityHomeFragment", "searchCommunity: ${it.localizedMessage}")
                    }.subscribe()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchManager =
                requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView =
                SearchView((activity as AppCompatActivity).supportActionBar!!.themedContext)
        searchView.queryHint = getString(R.string.amity_search)
        searchView.maxWidth = Int.MAX_VALUE

        val searchEditText =
                searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.amityColorBase
                )
        )
        searchEditText.setHintTextColor(
                ColorPaletteUtil.getColor(
                        ContextCompat.getColor(requireContext(), R.color.amityColorBase), ColorShade.SHADE2
                )
        )
        searchEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        searchEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    AndroidUtil.hideKeyboard(searchEditText)
                    return true
                }
                return false
            }
        })

        searchMenuItem = menu.add("SearchMenu").setVisible(true).setActionView(searchView)
                .setIcon(R.drawable.amity_ic_search)
        searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)

        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        val queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { textChangeSubject.onNext(it) }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { textChangeSubject.onNext(it) }
                searchMenuItem.collapseActionView()
                return true
            }
        }

        searchView.setOnQueryTextListener(queryTextListener)

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                mViewModel.isSearchMode.set(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                mViewModel.isSearchMode.set(false)
                return true
            }
        })

    }

    override fun onCommunitySelected(ekoCommunity: EkoCommunity?) {
        if (ekoCommunity != null) {
            val detailIntent = EkoCommunityPageActivity.newIntent(
                    requireContext(),
                    ekoCommunity.getCommunityId()
            )
            startActivity(detailIntent)
        }
    }

    class Builder() {
        private var newsFeedFragmentDelegate: INewsFeedFragmentDelegate? = null
        private var exploreFragmentDelegate: IExploreFragmentFragmentDelegate? = null

        fun build(activity: AppCompatActivity): EkoCommunityHomePageFragment {
            val fragment = EkoCommunityHomePageFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoCommunityHomeViewModel::class.java)
            fragment.mViewModel.newsFeedFragmentDelegate = newsFeedFragmentDelegate
            fragment.mViewModel.exploreFragmentDelegate = exploreFragmentDelegate
            return fragment
        }

        fun newsFeedFragmentDelegate(delegate: INewsFeedFragmentDelegate): Builder {
            this.newsFeedFragmentDelegate = delegate
            return this
        }

        fun exploreFragmentDelegate(delegate: IExploreFragmentFragmentDelegate): Builder {
            this.exploreFragmentDelegate = delegate
            return this
        }
    }
}