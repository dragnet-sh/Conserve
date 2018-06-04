package com.gemini.energy.presentation.home

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeBinding
import com.gemini.energy.presentation.audit.detail.adapter.DetailPagerAdapter
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.navigation.Navigator
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.view.GmailStyleCrossFadeSlidingPaneLayout
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_home.*
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

        crossfader.withContent(findViewById(R.id.root_home_container))

                .withFirst(layoutInflater.inflate(
                        R.layout.activity_side_bar, null),
                        LENGTH_AUDIT_LIST)

                .withSecond(layoutInflater.inflate(
                        R.layout.activity_mini_bar, null),
                        LENGTH_MINI_BAR)

                .build()

        // 1. Audit List
        var auditListFragment = AuditListFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .replace(R.id.side_bar, auditListFragment, FRAG_AUDIT_LIST)
                .commit()

        // 2. View Pager [PreAudit -- Zone List]
        binder.viewPager.adapter = DetailPagerAdapter(supportFragmentManager)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        //ToDo: This is where to implement the Back Button and stuff !!
        supportActionBar?.run {
            //setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_create_audit -> showCreateAudit()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onAuditSelected(observable: Observable<AuditModel>) {
        disposables.add(observable.subscribeWith(object : DisposableObserver<AuditModel>() {
            override fun onComplete() { }
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

        private const val LENGTH_MINI_BAR = 70
        private const val LENGTH_AUDIT_LIST = 200
        private const val ZONE_LIST_FRAGMENT_INDEX = 1
        private const val ANDROID_SWITCHER = "android:switcher"

        private var disposables = CompositeDisposable()
    }
}
