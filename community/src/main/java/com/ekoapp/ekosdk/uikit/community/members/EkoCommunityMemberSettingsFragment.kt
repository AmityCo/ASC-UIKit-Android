package com.ekoapp.ekosdk.uikit.community.members

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.exception.EkoException
import com.ekoapp.ekosdk.permission.EkoPermission
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.base.EkoFragmentStateAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.home.activity.EkoCommunityHomePageActivity
import com.ekoapp.ekosdk.uikit.community.utils.EkoSelectMemberContract
import com.ekoapp.ekosdk.uikit.utils.AlertDialogUtil
import com.ekoapp.ekosdk.uikit.utils.EkoConstants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_community_member_settings.*

private const val ARG_COMMUNITY_ID = "ARG_COMMUNITY_ID"
private const val ARG_IS_MEMBER = "ARG_IS_MEMBER"
private const val ARG_IS_COMMUNITY = "ARG_COMMUNITY"

class EkoCommunityMemberSettingsFragment internal constructor() : EkoBaseFragment() {

    private lateinit var fragmentStateAdapter: EkoFragmentStateAdapter
    private val mViewModel: EkoCommunityMembersViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel.communityId = arguments?.getString(ARG_COMMUNITY_ID) ?: ""
        mViewModel.isJoined.set(arguments?.getBoolean(ARG_IS_MEMBER) ?: false)
        mViewModel.community = arguments?.getParcelable(ARG_IS_COMMUNITY)

        fragmentStateAdapter = EkoFragmentStateAdapter(
                childFragmentManager,
                requireActivity().lifecycle
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.amity_fragment_community_member_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.addRemoveErrorData.observe(requireActivity(), Observer {
            handleNoPermissionError(it)
        })

        setUpToolbar()
        setUpTabLayout()
    }

    private fun setUpToolbar() {
        (activity as AppCompatActivity).supportActionBar?.title =
                getString(R.string.amity_members_capital)
        if (mViewModel.isJoined.get()) {
            disposable.add(mViewModel.checkModeratorPermissionAtCommunity(
                    EkoPermission.ADD_COMMUNITY_USER,
                    mViewModel.communityId
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .firstOrError()
                    .doOnSuccess {
                        setHasOptionsMenu(it)
                        mViewModel.isModerator.set(it)
                    }.doOnError {

                    }.subscribe()
            )
        }
    }

    private fun setUpTabLayout() {
        fragmentStateAdapter.setFragmentList(
                arrayListOf(
                        EkoFragmentStateAdapter.EkoPagerModel(
                                getString(R.string.amity_members_capital),
                                EkoMembersFragment.newInstance()
                        ),
                        EkoFragmentStateAdapter.EkoPagerModel(
                                getString(R.string.amity_moderators),
                                EkoModeratorsFragment.newInstance()
                        )
                )
        )
        membersTabLayout.setAdapter(fragmentStateAdapter)
    }

    private fun handleNoPermissionError(exception: Throwable) {
        if (exception is EkoException) {
            if (exception.code == EkoConstants.NO_PERMISSION_ERROR_CODE) {
                AlertDialogUtil.showNoPermissionDialog(requireContext(),
                        DialogInterface.OnClickListener { dialog, _ ->
                            dialog?.dismiss()
                            checkUserRole()
                        })
            } else {
                Log.e("MemberSettingsFragment", "${exception.message}")
            }
        } else {
            Log.e("MemberSettingsFragment", "${exception.message}")
        }
    }

    private fun checkUserRole() {
        mViewModel.getCommunityDetail().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .firstOrError()
                .doOnSuccess {
                    if (it.isJoined()) {
                        requireActivity().finish()
                    } else {
                        val intent =
                                Intent(requireContext(), EkoCommunityHomePageActivity::class.java).addFlags(
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                )
                        startActivity(intent)
                    }
                }.doOnError {
                    Log.e("MemberSettingsFragment", "checkUserRole: ${it.localizedMessage}")
                }.subscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.amity_ic_add)
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.amity_add))
                ?.setIcon(drawable)
                ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val selectMembers = registerForActivityResult(EkoSelectMemberContract()) {
            mViewModel.handleAddRemoveMembers(it)
        }
        selectMembers.launch(mViewModel.selectMembersList)
        return super.onOptionsItemSelected(item)
    }

    class Builder {

        private var communityId = ""
        private var isMember = false
        private var community: EkoCommunity? = null

        fun build(activity: AppCompatActivity): EkoCommunityMemberSettingsFragment {
            return EkoCommunityMemberSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COMMUNITY_ID, communityId)
                    putBoolean(ARG_IS_MEMBER, isMember)
                    putParcelable(ARG_IS_COMMUNITY, community)
                }
            }
        }

        fun communityId(id: String): Builder {
            communityId = id
            return this
        }

        fun isMember(value: Boolean): Builder {
            isMember = value
            return this
        }

        fun community(community: EkoCommunity): Builder {
            communityId = community.getCommunityId()
            isMember = community.isJoined()
            return apply { this.community = community }
        }
    }
}