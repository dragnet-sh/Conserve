package com.gemini.energy.presentation.zone

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.AuditListViewModel
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.zone.adapter.TypePagerAdapter
import javax.inject.Inject

class TypeActivity : BaseActivity(), ZoneListFragment.OnZoneSelectedListener {


     /*
     * View Model Setup - [AuditListViewModel]
     * */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val auditListViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(AuditListViewModel::class.java)
    }

    /*
    * These parameters are being set by the Zone List Fragment
    * On Zone Click
    * */
    private var zone: ZoneModel? = null


     /*
     * Fragment Lifecycle Methods
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        zone = intent.getParcelableExtra(PARCEL_ZONE)

        setZoneHeader(zone as ZoneModel)

        super.binder?.let {
            setupContent(it)
            setupZoneList()
        }
    }


    /*
    * View Pager Main Content
    * */
    private fun setupContent(binder: ActivityHomeDetailBinding) {
        zone?.let {
            binder.viewPager.adapter = TypePagerAdapter(
                    supportFragmentManager, it
            )
        }
    }


    /*
    * Side Panel Content Setup
    * Loading the Audit List Fragment
    * */
    private fun setupZoneList() {
        val zoneListFragment = ZoneListFragment.newInstance()

        zoneListFragment.arguments = Bundle().apply {
            this.putInt("auditId", zone?.auditId!!)
            this.putString("auditTag", "n/a")
        }

        supportFragmentManager
                .beginTransaction()
                .add(R.id.side_bar, zoneListFragment, FRAG_ZONE_LIST)
                .commit()

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

        private const val PARCEL_ZONE     = "$CALL_TAG.EXTRA.ZONE"
    }

}