package com.ekoapp.ekosdk.uikit.community.mycommunity.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.common.setShape
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentMyCommunityBinding
import com.ekoapp.ekosdk.uikit.community.detailpage.EkoCommunityPageActivity
import com.ekoapp.ekosdk.uikit.community.mycommunity.adapter.EkoMyCommunityListAdapter
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener
import com.ekoapp.ekosdk.uikit.community.mycommunity.viewmodel.EkoMyCommunityListViewModel
import com.ekoapp.ekosdk.uikit.community.ui.view.EkoCommunityCreateActivity
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.utils.AndroidUtil
import com.ekoapp.ekosdk.uikit.utils.EkoRecyclerViewItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_my_community.*

private const val ARG_SHOW_SEARCH = "ARG_SHOW_SEARCH"
private const val ARG_SHOW_OPTIONS_MENU = "ARG_SHOW_OPTIONS_MENU"

class EkoMyCommunityFragment internal constructor() : EkoBaseFragment(),
        IMyCommunityItemClickListener {
    private val TAG = EkoMyCommunityFragment::class.java.simpleName
    private lateinit var mViewModel: EkoMyCommunityListViewModel
    lateinit var mBinding: AmityFragmentMyCommunityBinding
    private lateinit var mAdapter: EkoMyCommunityListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.getBoolean(ARG_SHOW_OPTIONS_MENU) != false) {
            setHasOptionsMenu(true)
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoMyCommunityListViewModel::class.java)
        mBinding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.amity_fragment_my_community,
                        container,
                        false
                )
        mBinding.lifecycleOwner = viewLifecycleOwner
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        subscribeObservers()
        initRecyclerView()
        handleEditTextInput()
        if (arguments?.getBoolean(ARG_SHOW_SEARCH) != false) {
            etSearch.visibility = View.VISIBLE
        } else {
            etSearch.visibility = View.GONE
        }
    }

    private fun handleEditTextInput() {
        etSearch.setShape(
                null, null, null, null,
                R.color.amityColorBase, null, ColorShade.SHADE4
        )
        etSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    AndroidUtil.hideKeyboard(etSearch)
                    return true
                }
                return false
            }
        })
    }

    private fun setUpToolBar() {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.amity_my_community)
    }

    private fun subscribeObservers() {
        mViewModel.setPropertyChangeCallback()
        mViewModel.onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.SEARCH_STRING_CHANGED -> searchCommunity()
                else -> {
                }
            }
        }
    }

    private fun searchCommunity() {
        disposable.clear()
        disposable.add(mViewModel.getCommunityList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { list ->
                    mViewModel.emptyCommunity.set(list.size == 0)
                    mAdapter.submitList(list)
                }.doOnError {
                    Log.e(TAG, "initRecyclerView: ${it.localizedMessage}")
                }.subscribe()
        )
    }

    private fun initRecyclerView() {
        mAdapter = EkoMyCommunityListAdapter(this, false)
        rvMyCommunities.layoutManager = LinearLayoutManager(requireContext())
        rvMyCommunities.adapter = mAdapter
        rvMyCommunities.addItemDecoration(
                EkoRecyclerViewItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.eight),
                        0, resources.getDimensionPixelSize(R.dimen.eight), 0
                )
        )
        rvMyCommunities.setHasFixedSize(true)

        disposable.add(mViewModel.getCommunityList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { list ->
                    mViewModel.emptyCommunity.set(list.size == 0)
                    mAdapter.submitList(list)
                }.doOnError {
                    Log.e(TAG, "initRecyclerView: ${it.localizedMessage}")
                }.subscribe()
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.amity_ic_add)
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.amity_add))
                ?.setIcon(drawable)
                ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val createCommunityIntent =
                Intent(requireActivity(), EkoCommunityCreateActivity::class.java)
        startActivity(createCommunityIntent)
        return super.onOptionsItemSelected(item)
    }

    override fun onCommunitySelected(ekoCommunity: EkoCommunity?) {
        if (mViewModel.myCommunityItemClickListener != null) {
            mViewModel.myCommunityItemClickListener?.onCommunitySelected(ekoCommunity)
        } else {
            navigateToCommunityDetails(ekoCommunity)
        }
    }

    private fun navigateToCommunityDetails(ekoCommunity: EkoCommunity?) {
        if (ekoCommunity != null) {
            val detailIntent = EkoCommunityPageActivity.newIntent(
                    requireContext(),
                    ekoCommunity.getCommunityId()
            )
            startActivity(detailIntent)
        }

    }

    class Builder {
        private var myCommunityItemClickListener: IMyCommunityItemClickListener? = null
        private var showSearch = true
        private var showOptionsMenu = true

        fun build(activity: AppCompatActivity): EkoMyCommunityFragment {
            val fragment = EkoMyCommunityFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_SEARCH, showSearch)
                    putBoolean(ARG_SHOW_OPTIONS_MENU, showOptionsMenu)
                }
            }
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoMyCommunityListViewModel::class.java)
            fragment.mViewModel.myCommunityItemClickListener = myCommunityItemClickListener
            return fragment
        }

        fun showSearch(value: Boolean): Builder {
            showSearch = value
            return this
        }

        fun showOptionsMenu(value: Boolean): Builder {
            showOptionsMenu = value
            return this
        }

        private fun myCommunityItemClickListener(listener: IMyCommunityItemClickListener): Builder {
            this.myCommunityItemClickListener = listener
            return this
        }
    }

}