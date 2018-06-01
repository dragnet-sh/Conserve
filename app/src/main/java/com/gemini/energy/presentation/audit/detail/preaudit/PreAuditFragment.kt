package com.gemini.energy.presentation.audit.detail.preaudit

import android.os.Bundle
import dagger.android.support.DaggerFragment

class PreAuditFragment : DaggerFragment()  {

    companion object {
        fun newInstance(): PreAuditFragment {
            return PreAuditFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}