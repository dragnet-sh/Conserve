package com.gemini.energy.presentation.base

import android.content.pm.ActivityInfo
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_home_mini_bar.*
import javax.inject.Inject

open class BaseActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var crossfader: Crossfader<GmailStyleCrossFadeSlidingPaneLayout>
    var binder: ActivityHomeDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        binder = DataBindingUtil
                .setContentView(this, R.layout.activity_home_detail)

        setupToolbar()
        setupCrossfader()
    }

    open fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    private fun setupCrossfader() {
        val first = layoutInflater.inflate(R.layout.activity_home_side_bar, null)
        val second = layoutInflater.inflate(R.layout.activity_home_mini_bar, null)

        crossfader
                .withContent(findViewById(R.id.root_home_container))
                .withFirst(first, WIDTH_FIRST)
                .withSecond(second, WIDTH_SECOND)
                .withResizeContentPanel(true)
                .withGmailStyleSwiping()
                .build()

        crossfader.crossFadeSlidingPaneLayout.openPane()
        crossfader.crossFadeSlidingPaneLayout.setOffset(1f)
        crossfader.resize(1f)

        side_panel_button.setOnClickListener {
            crossfader.crossFade()
        }
    }

    companion object {
        private const val TAG = "BaseActivity"
        private const val WIDTH_SECOND = 70
        private const val WIDTH_FIRST = 180
    }

}