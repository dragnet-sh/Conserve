package com.gemini.energy.presentation.audit.detail.zone.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneCreateViewModel
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneDialogFragment
import com.gemini.energy.presentation.audit.detail.zone.list.adapter.ZoneListAdapter
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ZoneListFragment : DaggerFragment(),

        ZoneListAdapter.OnZoneClickListener,
        ZoneDialogFragment.OnZoneCreateListener,

        View.OnClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binder: FragmentZoneListBinding
    private var auditModel: AuditModel? = null // **** IMP -- Audit Model ***** //

    private val viewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(ZoneListViewModel::class.java)
    }

    private val _viewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(ZoneCreateViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewModel.result.observe(this, Observer {
            refreshViewModel()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binder = DataBindingUtil.inflate(inflater,
                R.layout.fragment_zone_list, container, false)

        binder.viewModel = viewModel
        binder.callbacks = this
        binder.fabClick = this

        binder.recyclerView.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binder.recyclerView.layoutManager = LinearLayoutManager(context)

        return binder.root
    }

    override fun onClick(v: View?) {
        showCreateZone()
    }

    /* *
     * Home Audit Activity <> Scope | Audit Entity Activity
     * INTENT >> Gateway
     * */
    override fun onZoneClick(view: View, item: ZoneModel) {

//        val intent = Intent(activity, ScopeParentActivity::class.java)
//        intent.putExtra(EXTRA_AUDIT_ID, item.auditId)
//        intent.putExtra(EXTRA_ZONE_ID, item.id)
//        intent.putExtra(EXTRA_ZONE_NAME, item.name)
//
//        context?.let {
//            ActivityCompat.startActivity(it, intent, null)
//        }

    }

    override fun onZoneCreate(args: Bundle) {
        auditModel?.let {
            _viewModel.createZone(it.id, args.getString("zoneTag"))
        }
    }

    private fun showCreateZone() {
        val dialogFragment = ZoneDialogFragment()
        dialogFragment.show(childFragmentManager, FRAG_DIALOG)
    }

    private fun refreshViewModel() {
        auditModel?.let {
            viewModel.loadZoneList(it.id)
        }
    }

    /**
     * Message Passing : Audit List Fragment <> Home Activity <> Zone List Fragment
     *
     * Step 1: The Audit List Click Event Registers the Audit Id
     * Step 2: Passes Audit Id to the Home Activity
     * Step 3: Uses Fragment Manager to find Zone List Fragment
     * Step 4: Passes the Audit Model to Zone List Fragment
     *
     * */
    fun setAuditModel(auditModel: AuditModel) {
        this.auditModel = auditModel
        refreshViewModel()
    }

    companion object {
        fun newInstance(): ZoneListFragment {
            return ZoneListFragment()
        }

        private const val TAG = "ZoneListFragment"
        private const val FRAG_DIALOG = "ZoneDialogFragment"

        private const val EXTRA_AUDIT_ID    = "$TAG.EXTRA.AUDIT_ID"
        private const val EXTRA_ZONE_ID     = "$TAG.EXTRA.ZONE_ID"
        private const val EXTRA_ZONE_NAME   = "$TAG.EXTRA.ZONE_NAME"
    }
}