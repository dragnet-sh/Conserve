package com.gemini.energy.presentation.zone.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.presentation.zone.list.TypeListFragment
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel

class TypePagerAdapter(fm: FragmentManager, private val zone: ZoneModel) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TypeListFragment.newInstance(position, zone)
    }

    override fun getCount(): Int {
        return EZoneType.count()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> EZoneType.plugload.value
            1 -> EZoneType.hvac.value
            2 -> EZoneType.lighting.value
            3 -> EZoneType.motors.value
            else -> EZoneType.others.value
        }
    }

    companion object {
        private const val TAG = "TypePagerAdapter"
    }

}