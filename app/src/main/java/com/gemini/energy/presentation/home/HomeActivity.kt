package com.gemini.energy.presentation.home

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeBinding
import com.gemini.energy.presentation.audit.detail.adapter.DetailPagerAdapter
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.navigation.Navigator
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home_mini_bar.*
import javax.inject.Inject

class HomeActivity : DaggerAppCompatActivity(), AuditListFragment.OnAuditSelectedListener {

    @Inject
    lateinit var crossfader: Crossfader<GmailStyleCrossFadeSlidingPaneLayout>

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binder = DataBindingUtil
                .setContentView<ActivityHomeBinding>(this, R.layout.activity_home)

        setupToolbar()
        setupCrossfader()

        // 1. Audit List
        var auditListFragment = AuditListFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .add(R.id.side_bar, auditListFragment, FRAG_AUDIT_LIST)
                .commit()

        // 2. View Pager [PreAudit -- Zone List]
        binder.viewPager.adapter = DetailPagerAdapter(supportFragmentManager)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    private fun setupCrossfader() {
        val first = layoutInflater.inflate(R.layout.activity_home_side_bar, null)
        val second = layoutInflater.inflate(R.layout.activity_home_mini_bar, null)

        crossfader
                .withContent(findViewById(R.id.root_home_container))
                .withFirst(first , WIDTH_FIRST)
                .withSecond(second, WIDTH_SECOND)
                .withResizeContentPanel(true)
                .withGmailStyleSwiping()
                .build()

        crossfader.crossFadeSlidingPaneLayout.openPane()
        crossfader.crossFadeSlidingPaneLayout.setOffset(1f)
        crossfader.resize(1f)

        side_panel_button.setOnClickListener {
            crossfader.crossFade()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create_audit -> showCreateAudit()
            else -> super.onOptionsItemSelected(item)
        }
        return true
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
        findViewById<TextView>(R.id.txt_header_audit).text = "${audit.name} -- ${audit.id}"
    }

    private fun refreshZoneViewModel(auditModel: AuditModel) {
        val tag = "$ANDROID_SWITCHER:${view_pager.id}:$ZONE_LIST_FRAGMENT_INDEX"
        val fragment = supportFragmentManager.findFragmentByTag(tag) as ZoneListFragment?
        fragment?.let {
            fragment.setAuditModel(auditModel)
        }
    }

    private fun showCreateAudit() {
        val dialogFragment = AuditDialogFragment()
        dialogFragment.show(supportFragmentManager, FRAG_DIALOG)
    }

    override fun onStop() {
        super.onStop()
        disposables.dispose()
    }

    companion object {
        private const val TAG = "HomeActivity"

        private const val FRAG_DIALOG = "AuditDialogFragment"
        private const val FRAG_AUDIT_LIST = "AuditListFragment"

        private const val WIDTH_SECOND = 70
        private const val WIDTH_FIRST = 200
        private const val ZONE_LIST_FRAGMENT_INDEX = 1
        private const val ANDROID_SWITCHER = "android:switcher"

        private var disposables = CompositeDisposable()
    }
}
