package com.gemini.energy.presentation.type

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
import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.presentation.type.adapter.TypePagerAdapter
import com.gemini.energy.presentation.type.feature.FeatureDataFragment
import com.gemini.energy.presentation.type.list.TypeListFragment
import com.gemini.energy.presentation.type.list.model.TypeModel
import kotlinx.android.synthetic.main.activity_home_detail.*

class TypeActivity : BaseActivity(),
        ZoneListFragment.OnZoneSelectedListener,
        TypeListFragment.OnTypeSelectedListener {


    /*
    * Case 1 : Set via ZoneListFragment - On Zone Click - Within different Activity
    * Case 2 : Set via ZoneListFragment - On Zone Click - Within the same Activity
    * */
    private var auditModel: AuditModel? = null
    private var zoneModel: ZoneModel? = null
    private var typeModel: TypeModel? = null

    private var typeId: Int? = null


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

        super.binder?.let {
            setupContent(it)
            setupSideMenu()
        }

    }


    override fun onResume() {
        super.onResume()

        when (app.getCount()) {
            0 -> Log.d(TAG, "*** PARENT TYPE ACTIVITY ***")
            1 -> Log.d(TAG, "*** CHILD TYPE ACTIVITY ***")
        }
    }


    private fun setupArguments() {

        setAudit(intent.getParcelableExtra(PARCEL_AUDIT))
        setZone(intent.getParcelableExtra(PARCEL_ZONE))

        setType(intent.getParcelableExtra(PARCEL_TYPE))
        setTypeId(intent.getIntExtra("typeId", 0))

        zoneModel?.let {
            setHeader(it, typeModel)
        }

    }


    /*
    * View Pager Main Content
    * Morphs it's behaviour based on the Type - [Parent or Child]
    * */
    private fun setupContent(binder: ActivityHomeDetailBinding) {

        if (zoneModel == null || auditModel == null) {
            Log.e(TAG, "Null - Zone or Audit")
            return
        }

        if (app.isParent()) {

            binder.viewPager.adapter = TypePagerAdapter(
                    supportFragmentManager, zoneModel!!, auditModel!!
            )

        } else {

            binder.viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return FeatureDataFragment()
                }

                override fun getCount(): Int {
                    return 1
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return getType(typeId ?: 0)
                }
            }

        }

    }


    private fun setupSideMenu() {
        if (app.isParent()) { setupZoneList() }
        if (app.isChild()) { setupTypeList() }
    }


    /*
    * Side Panel Content Setup
    * Loading the Audit List Fragment
    * */
    private fun setupZoneList() {
        val zoneListFragment = ZoneListFragment.newInstance()

        //ToDo : Maybe move this bundling to the Fragment itself
        zoneListFragment.arguments = Bundle().apply {
            this.putInt("auditId", zoneModel?.auditId!!)
            this.putString("auditTag", "n/a")
        }

        supportFragmentManager
                .beginTransaction()
                .add(R.id.side_bar, zoneListFragment, FRAG_ZONE_LIST)
                .commit()

    }

    private fun setupTypeList() {

        Log.d(TAG, "Type Id -- $typeId")
        Log.d(TAG, "Zone Model -- ${zoneModel.toString()}")
        Log.d(TAG, "Audit Model -- ${auditModel.toString()}")

        if (typeId != null && zoneModel != null && auditModel != null) {
            val typeListFragment = TypeListFragment.newInstance(
                    typeId!!, zoneModel!!, auditModel!!
            )

            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.side_bar, typeListFragment, FRAG_TYPE_LIST)
                    .commit()
        }

    }

    /*
    * This gets called when the Zone is selected
    * Should reload the specific Fragment that is currently being selected
    * */
    private fun refreshTypeViewModel(zone: ZoneModel) {

        for (index in 0..4) {
            val tag = "$ANDROID_SWITCHER:${view_pager.id}:$index"

            val fragment = supportFragmentManager
                    .findFragmentByTag(tag) as TypeListFragment?

            fragment?.let {
                fragment.setZoneModel(zone)
            }
        }
    }


    /*
    * Listeners Setup
    * */
    override fun onZoneSelected(zone: ZoneModel) {
        setZone(zone)
        setHeader(zone)
        refreshTypeViewModel(zone)
    }

    override fun onTypeSelected(type: TypeModel) {
        if (zoneModel != null) {
            setHeader(zoneModel!!, type)
        }
    }


    /*
    * Data Setup
    * */
    private fun setAudit(audit: AuditModel?) {
        this.auditModel = audit
    }

    private fun setZone(zone: ZoneModel?) {
        this.zoneModel = zone
    }

    private fun setType(type: TypeModel?) {
        this.typeModel = type
    }

    private fun setTypeId(id: Int) {
        this.typeId = id
    }

    private fun setHeader(zone: ZoneModel, type: TypeModel? = null) {
        var header = "${auditModel?.name} > ${zone.name}"
        if (type != null) {
            header = "$header > ${type.name}"
        }

        findViewById<TextView>(R.id.txt_header_audit).text = header
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

    private fun getType(pagerIndex: Int): String {
        return when(pagerIndex) {
            0 -> EZoneType.Plugload.value
            1 -> EZoneType.HVAC.value
            2 -> EZoneType.Lighting.value
            3 -> EZoneType.Motors.value
            else -> EZoneType.Others.value
        }
    }

    companion object {
        private const val TAG = "TypeActivity"
        private var app: App = App.instance

        private const val FRAG_ZONE_LIST    = "TypeActivityZoneListFragment"
        private const val FRAG_TYPE_LIST    = "TypeActivityTypeListFragment"

        private const val PARCEL_AUDIT      = "EXTRA.AUDIT"
        private const val PARCEL_ZONE       = "EXTRA.ZONE"
        private const val PARCEL_TYPE       = "EXTRA.TYPE"

        private const val ANDROID_SWITCHER = "android:switcher"
    }

}