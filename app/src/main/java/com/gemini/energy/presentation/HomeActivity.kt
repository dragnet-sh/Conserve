package com.gemini.energy.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.gemini.energy.R
import com.gemini.energy.presentation.list.AuditDialogFragment
import com.gemini.energy.presentation.list.AuditListFragment
import com.gemini.energy.presentation.navigation.Navigator
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.view.GmailStyleCrossFadeSlidingPaneLayout
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class HomeActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var crossfader: Crossfader<GmailStyleCrossFadeSlidingPaneLayout>

    @Inject
    lateinit var navigator: Navigator

//    private val binder by lazyThreadSafetyNone<ActivityHomeBinding> {
//        DataBindingUtil.setContentView(this, R.layout.activity_home)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        setupToolbar()

        crossfader.withContent(findViewById(R.id.root_home_container))
                .withFirst(layoutInflater.inflate(R.layout.activity_side_bar, null), WIDTH_SECONDARY)
                .withSecond(layoutInflater.inflate(R.layout.activity_mini_bar, null), WIDTH_PRIMARY)
                .build()

        // 1. Audit List
        supportFragmentManager.beginTransaction()
                .replace(R.id.side_bar, AuditListFragment.newInstance(), TAG_FRAG_SECONDARY)
                .commit()

        // 2. View Pager
//        binder.viewPager.adapter = HomePagerAdapter(supportFragmentManager)

    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        //ToDo: This is where to implement the Back Button and stuff !!
        supportActionBar?.run {
            //setDisplayHomeAsUpEnabled(true)
            //setHomeButtonEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_create_audit -> showCreateAudit()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showCreateAudit() {
        val dialogFragment = AuditDialogFragment()
        dialogFragment.show(supportFragmentManager, TAG_FRAG_DIALOG)
    }

    companion object {
        private const val TAG = "HomeActivity"
        private const val TAG_FRAG_DIALOG = "AuditDialogFragment"
        private const val TAG_FRAG_PRIMARY = ""
        private const val TAG_FRAG_SECONDARY = "AuditListFragment"
        private const val WIDTH_PRIMARY = 70
        private const val WIDTH_SECONDARY = 200
    }
}
