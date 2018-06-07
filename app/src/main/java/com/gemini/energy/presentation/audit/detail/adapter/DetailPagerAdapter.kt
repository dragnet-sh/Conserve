package com.gemini.energy.presentation.audit.detail.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.gemini.energy.presentation.audit.detail.preaudit.PreAuditFragment
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment

class DetailPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> PreAuditFragment.newInstance()
            else -> ZoneListFragment.newInstance()
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> "Pre-Audit"
            else -> "Zone"
        }
    }

}