package com.gemini.energy.presentation.audit.detail.preaudit

import android.os.Bundle
import android.view.View
import com.gemini.energy.R
import com.gemini.energy.presentation.base.BaseFormFragment

class PreAuditFragment : BaseFormFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.loadForm()

    }

    override fun resourceId(): Int {
        return R.raw.preaudit
    }

    companion object {

        fun newInstance(): PreAuditFragment {
            return PreAuditFragment()
        }

        private const val TAG = "PreAuditFragment"
    }
}