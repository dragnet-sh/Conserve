package com.gemini.energy.presentation.type.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.gemini.energy.presentation.type.feature.FeatureDataFragment
import com.gemini.energy.presentation.util.EZoneType

class FeaturePagerAdapter(fm: FragmentManager, private val typeId: Int) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return FeatureDataFragment()
    }

    override fun getCount(): Int {
        return 1
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return getType(typeId)!!.value
    }

    companion object {
        private fun getType(pagerIndex: Int) = EZoneType.get(pagerIndex)
        private const val TAG = "FeaturePagerAdapter"
    }
}