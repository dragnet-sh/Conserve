package com.gemini.energy.presentation.zone

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.widget.TextView
import com.gemini.energy.App
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.util.EAction
import com.gemini.energy.presentation.zone.adapter.TypePagerAdapter
import com.gemini.energy.presentation.zone.list.TypeListFragment
import com.gemini.energy.presentation.zone.list.model.TypeModel
import kotlinx.android.synthetic.main.activity_home_detail.*

class TypeActivity : BaseActivity(),
        ZoneListFragment.OnZoneSelectedListener,
        TypeListFragment.OnTypeSelectedListener {


    /*
    * Case 1 : Set via ZoneListFragment - On Zone Click - Within different Activity
    * Case 2 : Set via ZoneListFragment - On Zone Click - Within the same Activity
    * */
    private var audit: AuditModel? = null
    private var zone: ZoneModel? = null
    private var type: TypeModel? = null

    private var app: App? = null


    /*
    * Fragment Lifecycle Methods
    * Type Counter - 0 :: Parent Type Activity
    * Type Counter - 1 :: Child Type Activity
    *
    * Note : Only Plugload and Lighting have Parent | Child rest Types only have Parent
    *
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupArguments()

        setAppInstance()
        setZone(zone as ZoneModel)
        setHeader(zone as ZoneModel, type)

        super.binder?.let {
            setupContent(it)
            setupZoneList()
        }

    }


    override fun onResume() {
        super.onResume()

        when (app?.getCount()) {
            0 -> Log.d(TAG, "*** PARENT TYPE ACTIVITY ***")
            1 -> Log.d(TAG, "*** CHILD TYPE ACTIVITY ***")
        }
    }


    private fun setupArguments() {
        this.audit = intent.getParcelableExtra(PARCEL_AUDIT)
        this.zone = intent.getParcelableExtra(PARCEL_ZONE)
        this.type = intent.getParcelableExtra(PARCEL_TYPE)
    }


    /*
    * View Pager Main Content
    * Morphs it's behaviour based on the Type - [Parent or Child]
    * */
    private fun setupContent(binder: ActivityHomeDetailBinding) {

        if (zone == null || audit == null) {
            Log.e(TAG, "Null - Zone or Audit")
            return
        }

        if (app?.isParent()!!) {

            binder.viewPager.adapter = TypePagerAdapter(
                    supportFragmentManager, zone!!, audit!!
            )

        } else {

            binder.viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return TypeListFragment.newInstance(0, zone!!, audit!!)
                }

                override fun getCount(): Int {
                    return 1
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return "Plugload - Child"
                }
            }

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

            val fragment = supportFragmentManager
                    .findFragmentByTag(tag) as TypeListFragment?

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
        setHeader(zone)
        refreshTypeViewModel(zone)
    }

    override fun onTypeSelected(type: TypeModel) {
        if (zone != null) {
            setHeader(zone!!, type)
        }
    }

    private fun setHeader(zone: ZoneModel, type: TypeModel? = null) {
        var header = "${audit?.name} > ${zone.name}"
        if (type != null) {
            header = "$header > ${type.name}"
        }

        findViewById<TextView>(R.id.txt_header_audit).text = header
    }

    private fun setZone(zone: ZoneModel) {
        this.zone = zone
    }

    private fun setAppInstance() {
        this.app = App.instance
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

        if (App.instance.getCount() > 0) {
            App.instance.setCounter(EAction.Pop)
        }

        return true
    }

    companion object {
        private const val TAG = "TypeActivity"

        private const val FRAG_ZONE_LIST = "TypeActivityZoneListFragment"
        private const val PARCEL_AUDIT = "EXTRA.AUDIT"
        private const val PARCEL_ZONE = "EXTRA.ZONE"
        private const val PARCEL_TYPE = "EXTRA.TYPE"

        private const val ANDROID_SWITCHER = "android:switcher"
    }

}