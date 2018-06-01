package com.gemini.energy.presentation.pager.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.gemini.energy.presentation.pager.PreAuditFragment
import com.gemini.energy.presentation.pager.ZoneListFragment

class HomePagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm){

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