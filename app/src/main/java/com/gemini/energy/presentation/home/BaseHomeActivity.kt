package com.gemini.energy.presentation.home

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_home_mini_bar.*
import javax.inject.Inject

open class BaseHomeActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var crossfader: Crossfader<GmailStyleCrossFadeSlidingPaneLayout>
    var binder: ActivityHomeDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binder = DataBindingUtil
                .setContentView(this, R.layout.activity_home_detail)

        setupToolbar()
        setupCrossfader()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create_audit -> showCreateAudit()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    private fun setupCrossfader() {
        val first = layoutInflater.inflate(R.layout.activity_home_side_bar, null)
        val second = layoutInflater.inflate(R.layout.activity_home_mini_bar, null)

        crossfader
                .withContent(findViewById(R.id.root_home_container))
                .withFirst(first , WIDTH_FIRST)
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

    private fun showCreateAudit() {
        val dialogFragment = AuditDialogFragment()
        dialogFragment.show(supportFragmentManager, FRAG_DIALOG)
    }


    companion object {
        private const val TAG = "BaseHomeActivity"
        private const val FRAG_DIALOG = "AuditDialogFragment"

        private const val WIDTH_SECOND = 70
        private const val WIDTH_FIRST = 180
    }

}