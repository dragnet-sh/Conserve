package com.gemini.energy.presentation.audit.detail.preaudit

import com.gemini.energy.R
import com.gemini.energy.presentation.base.BaseFormFragment

class PreAuditFragment : BaseFormFragment()  {

    override fun resourceId(): Int {
        return R.raw.preaudit_sample
    }

    companion object {

        fun newInstance(): PreAuditFragment {
            return PreAuditFragment()
        }

        private const val TAG = "PreAuditFragment"
    }
}