package com.gemini.energy.presentation.zone

import android.os.Bundle
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.zone.adapter.TypePagerAdapter

class TypeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        super.binder?.let {
            setupContent(it)
        }
    }

    private fun setupContent(binder: ActivityHomeDetailBinding) {
        binder.viewPager.adapter = TypePagerAdapter(supportFragmentManager)
    }

    override fun setupToolbar() {
        super.setupToolbar()
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }





    companion object {
        private const val TAG = "TypeActivity"
    }

}