package com.gemini.energy.presentation.scope

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityAuditScopeBinding
import com.gemini.energy.presentation.scope.parent.dialog.ScopeDialogFragment
import dagger.android.support.DaggerAppCompatActivity

class ScopeParentActivity : DaggerAppCompatActivity() {

    private var auditId: Int? = null
    private var zoneId: Int? = null
    private var zoneName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binder = DataBindingUtil
                .setContentView<ActivityAuditScopeBinding>(this, R.layout.activity_audit_scope)

        setupToolbar()

        auditId = intent.getIntExtra(EXTRA_AUDIT_ID, 0)
        zoneId = intent.getIntExtra(EXTRA_ZONE_ID, 0)
        zoneName = intent.getStringExtra(EXTRA_ZONE_NAME)

        Log.d(TAG, auditId.toString())
        Log.d(TAG, zoneId.toString())
        Log.d(TAG, zoneName)

        binder.txtHeaderScope.text = zoneName
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_scope_parent, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
            R.id.menu_create_audit_scope_parent -> consume {showCreateAuditScope()}
            else -> super.onOptionsItemSelected(item)
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }

    private fun showCreateAuditScope() {
        Log.d(TAG, "Show Dialog - Create Audit Scope Parent")
        val dialogFragment = ScopeDialogFragment()
        dialogFragment.show(fragmentManager, FRAG_DIALOG)
    }

    companion object {
        private const val TAG = "ScopeParentActivity"
        private const val CALL_TAG = "ZoneListFragment"
        private const val FRAG_DIALOG = "ScopeParentDialogFragment"

        private const val EXTRA_AUDIT_ID    = "$CALL_TAG.EXTRA.AUDIT_ID"
        private const val EXTRA_ZONE_ID     = "$CALL_TAG.EXTRA.ZONE_ID"
        private const val EXTRA_ZONE_NAME   = "$CALL_TAG.EXTRA.ZONE_NAME"
    }
}