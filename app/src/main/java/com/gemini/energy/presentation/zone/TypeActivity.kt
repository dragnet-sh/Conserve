package com.gemini.energy.presentation.zone

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneDialogFragment
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import com.gemini.energy.presentation.audit.list.AuditListViewModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.zone.adapter.TypePagerAdapter
import javax.inject.Inject

class TypeActivity : BaseActivity(), ZoneListFragment.OnZoneSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(AuditListViewModel::class.java)
    }

    /*
    * These parameters are being set by the Zone List Fragment
    * On Zone Click
    * */
    private var auditId: Int? = null
    private var zoneId: Int? = null
    private var zoneName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, viewModel.toString())

        auditId = intent.getIntExtra(EXTRA_AUDIT_ID, 0)
        zoneId = intent.getIntExtra(EXTRA_ZONE_ID, 0)
        zoneName = intent.getStringExtra(EXTRA_ZONE_NAME)

        super.binder?.let {
            setupContent(it)
            setupZoneList()
        }
    }


    /*
    * Option Menu Management
    * */
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_home, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//        R.id.menu_create_zone -> consume { showCreateAudit() }
//        else -> super.onOptionsItemSelected(item)
//    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }


    /*
    * View Pager Main Content
    * */
    private fun setupContent(binder: ActivityHomeDetailBinding) {
        binder.viewPager.adapter = TypePagerAdapter(supportFragmentManager)
    }


    /*
    * Side Panel Content Setup
    * Loading the Audit List Fragment
    * */
    private fun setupZoneList() {
        val zoneListFragment = ZoneListFragment.newInstance()

        zoneListFragment.arguments = Bundle().apply {
            this.putInt("auditId", auditId!!)
            this.putString("auditTag", "n/a")
        }

        supportFragmentManager
                .beginTransaction()
                .add(R.id.side_bar, zoneListFragment, FRAG_ZONE_LIST)
                .commit()

    }

    /*
    * Create Zone - Dialog
    *
    * 1. Shows up the Zone Dialog Fragment
    * 2. On Validation Success - Callbacks onZoneCreate
    * 3. Invokes the Create Zone Method in the Zone View Model
    * 4. Subscribe to the Create Zone Channel
    * 5. Once the Zone is created reload the Zone List
    *
    * */
    private fun showCreateAudit() {
        val dialogFragment = AuditDialogFragment()
        dialogFragment.show(supportFragmentManager, FRAG_DIALOG)
    }

    override fun onZoneSelected(zone: ZoneModel) {
        setZoneHeader(zone)
    }

    private fun setZoneHeader(zone: ZoneModel) {
        findViewById<TextView>(R.id.txt_header_audit).text = "${zone.name}"
    }

    override fun setupToolbar() {
        super.setupToolbar()
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val TAG = "TypeActivity"

        private const val FRAG_DIALOG       = "TypeActivityAuditDialogFragment"
        private const val FRAG_ZONE_LIST    = "TypeActivityZoneListFragment"
        private const val CALL_TAG          = "ZoneListFragment"

        private const val EXTRA_AUDIT_ID    = "$CALL_TAG.EXTRA.AUDIT_ID"
        private const val EXTRA_ZONE_ID     = "$CALL_TAG.EXTRA.ZONE_ID"
        private const val EXTRA_ZONE_NAME   = "$CALL_TAG.EXTRA.ZONE_NAME"

    }

}