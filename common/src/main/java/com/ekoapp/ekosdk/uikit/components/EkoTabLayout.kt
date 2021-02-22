package com.ekoapp.ekosdk.uikit.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.base.EkoFragmentStateAdapter
import com.ekoapp.ekosdk.uikit.common.setBackgroundColor
import com.ekoapp.ekosdk.uikit.common.views.ColorPaletteUtil
import com.ekoapp.ekosdk.uikit.common.views.ColorShade
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class EkoTabLayout : ConstraintLayout {

    private lateinit var mAdapter: EkoFragmentStateAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val rootView = LayoutInflater.from(context).inflate(R.layout.amity_tab_layout, this, true)
        tabLayout = rootView.findViewById(R.id.tab_header)
        viewPager2 = rootView.findViewById(R.id.eko_viewPager)
        val divider = rootView.findViewById<View>(R.id.divider)
        divider.setBackgroundColor(
                ContextCompat.getColor(context, R.color.amityColorBase),
                ColorShade.SHADE4
        )
    }

    fun setAdapter(adapter: EkoFragmentStateAdapter) {
        mAdapter = adapter
        viewPager2.adapter = mAdapter

        tabLayout.setTabTextColors(
                ColorPaletteUtil.getColor(
                        ContextCompat.getColor(context, R.color.amityColorBase),
                        ColorShade.SHADE3
                ), ContextCompat.getColor(
                context,
                R.color.amityColorPrimary
        )
        )

        try {
            TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                tab.text = mAdapter.getTitle(position)
            }.attach()
        } catch (ex: Exception) {
            // log
        }
    }

    fun switchTab(position: Int) {
        try {
            viewPager2.setCurrentItem(position, true)
        } catch (ex: Exception) {

        }
    }

    fun disableSwipe() {
        viewPager2.isUserInputEnabled = false
    }
}