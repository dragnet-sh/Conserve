package com.gemini.energy.presentation.pager

import android.os.Bundle
import dagger.android.support.DaggerFragment

class ZoneListFragment : DaggerFragment() {

    companion object {
        fun newInstance(): ZoneListFragment {
            return ZoneListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}