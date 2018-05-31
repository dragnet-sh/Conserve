package com.gemini.energy.presentation.list

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
import com.gemini.energy.presentation.list.adapter.AuditListAdapter
import com.gemini.energy.presentation.list.model.AuditModel
import com.gemini.energy.databinding.FragmentAuditListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class AuditListFragment : DaggerFragment(), AuditListAdapter.Callbacks,
        AuditDialogFragment.Callbacks {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binder: FragmentAuditListBinding

    private val viewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(AuditListViewModel::class.java)
    }

    private val _viewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(AuditCreateViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewModel.result.observe(this, Observer {
            refreshViewModel()
        })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binder = DataBindingUtil.inflate(inflater,
                R.layout.fragment_audit_list, container, false)

        binder.viewModel = viewModel
        binder.callbacks = this

        binder.recyclerView.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binder.recyclerView.layoutManager = LinearLayoutManager(context)

        return binder.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        refreshViewModel()
    }

    override fun onItemClick(view: View, item: AuditModel) {
        Log.d(TAG, "Audit List Item - Click")
        //ToDo: ***** Load the List of Zones in the ViewPager ****
    }

    override fun onAuditCreate(args: Bundle) {
        _viewModel.createAudit(args.getInt("auditId"), args.getString("auditTag"))
    }

    private fun refreshViewModel() {
        viewModel.loadAuditList()
    }

    companion object {
        private const val TAG = "AuditListFragment"
        fun newInstance(): AuditListFragment {
            return AuditListFragment()
        }
    }
}