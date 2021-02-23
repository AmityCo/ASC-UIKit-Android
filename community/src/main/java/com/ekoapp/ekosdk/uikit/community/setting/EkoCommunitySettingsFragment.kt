package com.ekoapp.ekosdk.uikit.community.setting

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentCommunitySettingsBinding
import com.ekoapp.ekosdk.uikit.community.edit.EkoCommunityProfileActivity
import com.ekoapp.ekosdk.uikit.community.home.activity.EkoCommunityHomePageActivity
import com.ekoapp.ekosdk.uikit.community.members.EkoCommunityMemberSettingsActivity
import com.ekoapp.ekosdk.uikit.utils.AlertDialogUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_community_settings.*
import java.util.*

private const val ARG_COMMUNITY_ID = "ARG_COMMUNITY_ID"
private const val ARG_EKO_COMMUNITY = "ARG_IS_ADMIN"

class EkoCommunitySettingsFragment : Fragment() {
    private val mViewModel: EkoCommunitySettingViewModel by activityViewModels()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mViewModel.communityId.set(arguments?.getString(ARG_COMMUNITY_ID))
            val community = arguments?.getParcelable<EkoCommunity>(ARG_EKO_COMMUNITY)
            if (community != null) {
                mViewModel.ekoCommunity = community
                mViewModel.setCommunity(community)
            }

        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mBinding: AmityFragmentCommunitySettingsBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.amity_fragment_community_settings, container, false
        )
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleClickEvents()
        getCommunityDetails()
    }

    private fun getCommunityDetails() {
        if (mViewModel.communityId.get() != null && mViewModel.ekoCommunity == null) {
            compositeDisposable.add(mViewModel.getCommunityDetail().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        mViewModel.setCommunity(it)
                    }.doOnError {

                    }.subscribe()
            )
        }
    }

    private fun handleClickEvents() {
        groupEditProfile.setOnClickListener {
            val intent = EkoCommunityProfileActivity.newIntent(
                    requireContext(),
                    mViewModel.communityId.get() ?: ""
            )
            startActivity(intent)
        }

        groupMembers.setOnClickListener {
            mViewModel.ekoCommunity?.let {
                val intent = EkoCommunityMemberSettingsActivity.newIntent(requireContext(), it)
                startActivity(intent)
            } ?: kotlin.run {
                val intent = EkoCommunityMemberSettingsActivity.newIntent(
                        requireContext(),
                        mViewModel.communityId.get() ?: "", mViewModel.isPublic.get()
                )
                startActivity(intent)
            }
        }

        tvLeaveCommunity.setOnClickListener {
            if (mViewModel.isAdmin.get()) {
                closeCommunity()
            } else {
                leaveCommunity()
            }
        }
    }

    private fun leaveCommunity() {
        AlertDialogUtil.showDialog(requireContext(),
                getString(R.string.amity_leave_community) + "?",
                getString(R.string.amity_leave_community_msg),
                getString(R.string.amity_leave).toUpperCase(Locale.getDefault()),
                getString(R.string.amity_cancel).toUpperCase(Locale.getDefault()),
                DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        mViewModel.leaveCommunity().subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    requireActivity().finish()
                                }.doOnError {

                                }.subscribe()
                    } else {
                        dialog.cancel()
                    }
                })
    }

    private fun closeCommunity() {
        AlertDialogUtil.showDialog(requireContext(),
                getString(R.string.amity_close_community) + "?",
                getString(R.string.amity_close_community_msg),
                getString(R.string.amity_close).toUpperCase(Locale.getDefault()),
                getString(R.string.amity_cancel).toUpperCase(Locale.getDefault()),
                DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        mViewModel.closeCommunity().subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    navigateToCommunityHome()
                                }.doOnError {

                                }.subscribe()
                    } else {
                        dialog.cancel()
                    }
                })
    }

    private fun navigateToCommunityHome() {
        val intent = Intent(requireContext(), EkoCommunityHomePageActivity::class.java)
        startActivity(intent)
    }

    class Builder {
        private var communityId: String? = null
        private var ekoCommunity: EkoCommunity? = null

        fun build(activity: AppCompatActivity): EkoCommunitySettingsFragment {
            return EkoCommunitySettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COMMUNITY_ID, communityId)
                    putParcelable(ARG_EKO_COMMUNITY, ekoCommunity)
                }
            }
        }

        fun communityId(id: String): Builder {
            communityId = id
            return this
        }

        fun community(community: EkoCommunity?): Builder {
            ekoCommunity = community
            return this
        }
    }
}