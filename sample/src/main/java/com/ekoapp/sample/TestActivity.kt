package com.ekoapp.sample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ekoapp.ekosdk.uikit.community.home.fragments.EkoCommunityHomePageFragment
import kotlinx.android.synthetic.main.amity_activity_test.*

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.amity_activity_test)
        setupToolbar()
        initializeFragment()
    }

    private fun setupToolbar() {
        mToolbar.setLeftDrawable(
            ContextCompat.getDrawable(
                this,
                com.ekoapp.ekosdk.uikit.community.R.drawable.amity_ic_arrow_back
            )
        )
        mToolbar.setLeftString("Test Activity")
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        setSupportActionBar(mToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val drawable = getDrawable(com.ekoapp.ekosdk.uikit.community.R.drawable.amity_ic_add)
        menu?.add(
            Menu.NONE,
            1,
            Menu.NONE,
            getString(com.ekoapp.ekosdk.uikit.community.R.string.amity_add)
        )
            ?.setIcon(drawable)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initializeFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mFrameLayout, EkoCommunityHomePageFragment.Builder().build(this))
        transaction.commit()
    }
}