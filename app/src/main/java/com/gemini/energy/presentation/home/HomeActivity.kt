package com.gemini.energy.presentation.home

import android.os.Bundle
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.presentation.audit.detail.adapter.DetailPagerAdapter
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.navigation.Navigator
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_home_detail.*
import javax.inject.Inject

class HomeActivity : BaseHomeActivity(), AuditListFragment.OnAuditSelectedListener {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        super.binder?.let {
            setupContent(it)
        }
    }

    private fun setupContent(binder: ActivityHomeDetailBinding) {
        // 1. Audit List
        var auditListFragment = AuditListFragment.newInstance()

        supportFragmentManager
                .beginTransaction()
                .add(R.id.side_bar, auditListFragment, FRAG_AUDIT_LIST)
                .commit()

        // 2. View Pager [PreAudit -- Zone List]
        binder.viewPager.adapter = DetailPagerAdapter(supportFragmentManager)
    }

    override fun onAuditSelected(observable: Observable<AuditModel>) {
        disposables.add(observable.subscribeWith(object : DisposableObserver<AuditModel>() {
            override fun onComplete() {}
            override fun onNext(t: AuditModel) {
                setAuditHeader(t)
                refreshZoneViewModel(t)
            }

            override fun onError(e: Throwable) {}
        }))
    }

    private fun setAuditHeader(audit: AuditModel) {
        findViewById<TextView>(R.id.txt_header_audit).text = "${audit.name}"
    }

    private fun refreshZoneViewModel(auditModel: AuditModel) {
        val tag = "$ANDROID_SWITCHER:${view_pager.id}:$ZONE_LIST_FRAGMENT_INDEX"
        val fragment = supportFragmentManager.findFragmentByTag(tag) as ZoneListFragment?
        fragment?.let {
            fragment.setAuditModel(auditModel)
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.dispose()
    }

    companion object {
        private const val TAG = "HomeActivity"
        private const val FRAG_AUDIT_LIST = "AuditListFragment"
        private const val ZONE_LIST_FRAGMENT_INDEX = 1
        private const val ANDROID_SWITCHER = "android:switcher"

        private var disposables = CompositeDisposable()
    }

}
