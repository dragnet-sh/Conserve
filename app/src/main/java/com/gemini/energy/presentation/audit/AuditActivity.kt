package com.gemini.energy.presentation.audit

import android.os.Bundle
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.presentation.audit.detail.adapter.DetailPagerAdapter
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.util.Navigator
import javax.inject.Inject

class AuditActivity : BaseActivity() {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        super.binder?.let {
            setupContent(it)
        }
    }

    private fun setupContent(binder: ActivityHomeDetailBinding) {
        binder.viewPager.adapter = DetailPagerAdapter(supportFragmentManager)
    }

    companion object {
        private const val TAG = "AuditActivity"
    }

}
