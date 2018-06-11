package com.gemini.energy.presentation.zone

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.zone.adapter.TypePagerAdapter
import com.gemini.energy.presentation.zone.list.TypeListFragment
import kotlinx.android.synthetic.main.activity_home_detail.*

class TypeActivity : BaseActivity(),
        ZoneListFragment.OnZoneSelectedListener {


    /*
    * Case 1 : Set via ZoneListFragment - On Zone Click - Within different Activity
    * Case 2 : Set via ZoneListFragment - On Zone Click - Within the same Activity
    * */
    private var zone: ZoneModel? = null


    /*
    * Fragment Lifecycle Methods
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        zone = intent.getParcelableExtra(PARCEL_ZONE)

        // *** Initially being set from ZoneActivity *** //
        setZone(zone as ZoneModel)
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
        zone?.let { zoneModel ->
            binder.viewPager.adapter = TypePagerAdapter(
                    supportFragmentManager, zoneModel
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

    /*
    * This gets called when the Zone is selected
    * Should reload the specific Fragment that is currently being selected
    * */
    private fun refreshTypeViewModel(zone: ZoneModel) {

        for (index in 0..4) {
            val tag = "${ANDROID_SWITCHER}:${view_pager.id}:$index"

            Log.d(TAG, "################################")
            Log.d(TAG, "View Pager Index : $index")
            Log.d(TAG, tag.toString())

            val fragment = supportFragmentManager.findFragmentByTag(tag) as TypeListFragment?
            fragment?.let {
                fragment.setZoneModel(zone)
            }
        }
    }

    /*
    * ZoneModel gets set from the ZoneListFragment
    * */
    override fun onZoneSelected(zone: ZoneModel) {
        setZone(zone)
        setZoneHeader(zone)
        refreshTypeViewModel(zone)
    }

    private fun setZoneHeader(zone: ZoneModel) {
        findViewById<TextView>(R.id.txt_header_audit).text = "${zone.auditId} > ${zone.name}"
    }

    private fun setZone(zone: ZoneModel) {
        this.zone = zone
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

        private const val FRAG_ZONE_LIST = "TypeActivityZoneListFragment"
        private const val CALL_TAG = "ZoneListFragment"
        private const val PARCEL_ZONE = "$CALL_TAG.EXTRA.ZONE"

        private const val ANDROID_SWITCHER = "android:switcher"
        private const val CURRENT_FRAGMENT_INDEX = 0
    }

}