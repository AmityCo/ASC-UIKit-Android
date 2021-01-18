package com.ekoapp.ekosdk.uikit.imagepreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.ekoapp.ekosdk.uikit.R
import kotlinx.android.synthetic.main.activity_eko_image_preview.*

class EkoImagePreviewActivity : AppCompatActivity() {
    private lateinit var ekoImages: List<PreviewImage>
    private var imagePosition: Int = 0
    private var showImageCount = true
    private lateinit var imagePreviewAdapter: EkoImagePreviewPagerAdapter
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.upstraColorSecondary);

        setContentView(R.layout.activity_eko_image_preview)
        ekoImages = intent.getParcelableArrayListExtra(EXTRA_IMAGES) ?: listOf()
        showImageCount = intent.getBooleanExtra(EXTRA_SHOW_IMAGE_COUNT, true)
        imagePosition = intent.getIntExtra(EXTRA_IMAGE_POSITION, 0)

        initToolbar()
        initViewPager()

    }


    private fun initViewPager() {
        imagePreviewAdapter = EkoImagePreviewPagerAdapter()

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setToolbarTitle(position)
            }
        }
        imageViewPages.adapter = imagePreviewAdapter
        imagePreviewAdapter.setItems(ekoImages)
        imageViewPages.setCurrentItem(imagePosition, false)
        setToolbarTitle(imagePosition)

    }

    override fun onResume() {
        super.onResume()
        imageViewPages.registerOnPageChangeCallback(pageChangeCallback)
    }

    override fun onPause() {
        super.onPause()
        imageViewPages.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_uikit_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setToolbarTitle(position: Int) {
        if (!showImageCount) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
        } else {
            supportActionBar?.title =
                String.format(getString(R.string.image_preview_title, position + 1, ekoImages.size))
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val EXTRA_IMAGES = "images"
        private const val EXTRA_IMAGE_POSITION = "image_position"
        private const val EXTRA_SHOW_IMAGE_COUNT = "show_image_count"

        fun newIntent(
            context: Context,
            imagePosition: Int,
            showImageCount: Boolean,
            images: ArrayList<PreviewImage>
        ): Intent {
            val intent = Intent(context, EkoImagePreviewActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_IMAGES, ArrayList(images))
            intent.putExtra(EXTRA_IMAGE_POSITION, imagePosition)
            intent.putExtra(EXTRA_SHOW_IMAGE_COUNT, showImageCount)
            return intent
        }
    }
}