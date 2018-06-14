package com.gemini.energy.presentation.type.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.presentation.type.list.TypeListFragment

class TypePagerAdapter(fm: FragmentManager, private val zone: ZoneModel, private val audit: AuditModel)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TypeListFragment.newInstance(position, zone, audit)
    }

    override fun getCount(): Int {
        return EZoneType.count()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return EZoneType.get(position)?.value
    }

    companion object {
        private const val TAG = "TypePagerAdapter"
    }

}