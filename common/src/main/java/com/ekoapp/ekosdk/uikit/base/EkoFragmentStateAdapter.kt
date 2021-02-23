package com.ekoapp.ekosdk.uikit.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class EkoFragmentStateAdapter(fm: FragmentManager, lifeCycle: Lifecycle)
    : FragmentStateAdapter(fm, lifeCycle) {

    private val fragmentList: ArrayList<EkoPagerModel> = arrayListOf()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position].fragment

    fun setFragmentList(list: List<EkoPagerModel>) {
        fragmentList.clear()
        fragmentList.addAll(list)
        notifyDataSetChanged()
    }

    fun getTitle(position: Int): String = fragmentList[position].title

    data class EkoPagerModel(
            val title: String,
            val fragment: Fragment
    )
}