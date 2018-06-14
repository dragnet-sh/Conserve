package com.gemini.energy.presentation.type.feature

import com.gemini.energy.R
import com.gemini.energy.presentation.base.BaseFormFragment

class FeatureDataFragment : BaseFormFragment() {

    override fun resourceId(): Int {
        return R.raw.preaudit_sample
    }

    companion object {

        fun newInstance(): FeatureDataFragment {
            return FeatureDataFragment()
        }

        private const val TAG = "FeatureDataFragment"
    }

}