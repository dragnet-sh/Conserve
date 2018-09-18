package com.gemini.energy.presentation.audit.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentAuditListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.dialog.AuditCreateViewModel
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import com.gemini.energy.presentation.audit.list.adapter.AuditListAdapter
import com.gemini.energy.presentation.audit.list.model.AuditModel
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import javax.inject.Inject

class AuditListFragment : DaggerFragment(),

        AuditListAdapter.OnAuditClickListener,
        AuditDialogFragment.OnAuditCreateListener {


    interface OnAuditSelectedListener {
        fun onAuditSelected(observable: Observable<AuditModel>)
    }


    /*
    * View Model Setup - [AuditListViewModel | AuditCreateViewModel]
    * */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val auditListViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(AuditListViewModel::class.java)
    }

    private val auditCreateViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(AuditCreateViewModel::class.java)
    }


    /*
    * Binder - fragment_audit_list.xml
    * */
    private lateinit var binder: FragmentAuditListBinding


    /*
    * Fragment Lifecycle Methods
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupListeners()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binder = DataBindingUtil.inflate(inflater,
                R.layout.fragment_audit_list, container, false)

        binder.viewModel = auditListViewModel

        // *** Handler to Execute the Callback *** //
        // *** onAuditClick *** //
        // *** This gets passed on to the Adapter via ViewBindingAdapter *** //
        binder.callbacks = this

//        binder.recyclerView.addItemDecoration(
//                DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binder.recyclerView.layoutManager = LinearLayoutManager(context)

        return binder.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        refreshViewModel()
    }

    private fun refreshViewModel() {
        auditListViewModel.loadAuditList()
    }


    /*
    * Listeners | Observers
    * */
    override fun onAuditClick(observable: Observable<AuditModel>) {
        val activity = activity as OnAuditSelectedListener?
        activity?.let {
            it.onAuditSelected(observable)
        }
    }

    override fun onAuditCreate(args: Bundle) {
        auditCreateViewModel.createAudit(args.getInt("auditId"), args.getString("auditTag"))
    }

    private fun setupListeners() {
        auditCreateViewModel.result.observe(this, Observer {
            refreshViewModel()
        })
    }


    /*
    * Static Content
    * */
    companion object {
        fun newInstance(): AuditListFragment {
            return AuditListFragment()
        }

        private const val TAG = "AuditListFragment"
    }
}