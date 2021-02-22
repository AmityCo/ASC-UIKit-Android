package com.ekoapp.ekosdk.uikit.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.uikit.R
import com.ekoapp.ekosdk.uikit.components.EkoToolBar
import com.ekoapp.ekosdk.uikit.components.EkoToolBarClickListener
import com.ekoapp.ekosdk.uikit.databinding.AmityActivityBaseToolbarFragmentContainerBinding
import kotlinx.android.synthetic.main.amity_activity_base_toolbar_fragment_container.*


abstract class EkoBaseToolbarFragmentContainerActivity : AppCompatActivity(),
        EkoToolBarClickListener {
    lateinit var binding: AmityActivityBaseToolbarFragmentContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this,
                R.layout.amity_activity_base_toolbar_fragment_container
        )
        if (savedInstanceState == null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = getContentFragment()
            fragmentTransaction.replace(R.id.fragmentContainer, fragment)
            fragmentTransaction.commit()
        }

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        setSupportActionBar(toolbar)
        toolbar?.setClickListener(this)

        initToolbar()
    }

    fun getToolBar(): EkoToolBar? {
        return toolbar
    }

    fun showToolbarDivider() {
        binding.divider.visibility = View.VISIBLE
    }

    abstract fun initToolbar()

    abstract fun getContentFragment(): Fragment

    override fun leftIconClick() {
        onBackPressed()
    }

    override fun rightIconClick() {

    }
}