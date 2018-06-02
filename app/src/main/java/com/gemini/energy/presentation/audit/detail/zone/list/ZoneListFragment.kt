package com.gemini.energy.presentation.audit.detail.zone.list

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
import com.gemini.energy.databinding.FragmentZoneListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneCreateViewModel
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneDialogFragment
import com.gemini.energy.presentation.audit.detail.zone.list.adapter.ZoneListAdapter
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ZoneListFragment : DaggerFragment(),

        ZoneListAdapter.Callbacks,
        ZoneDialogFragment.Callbacks,

        View.OnClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binder: FragmentZoneListBinding

    private val viewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(ZoneListViewModel::class.java)
    }

    private val _viewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(ZoneCreateViewModel::class.java)
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
        Log.d(TAG, "Fab Button On Click")
        showCreateZone()
    }

    override fun onItemClick(view: View, item: ZoneModel) {
        Log.d(TAG, "Zone List Item - Click")
        //ToDo: ***** Load the Audit Entity ****
    }

    override fun onZoneCreate(args: Bundle) {
        _viewModel.createZone(0, args.getString("zoneTag"))
    }

    private fun showCreateZone() {
        val dialogFragment = ZoneDialogFragment()
        dialogFragment.show(fragmentManager, FRAG_DIALOG)
    }

    fun refreshViewModel(auditId: Int) {
        Log.d(TAG, "Zone List - Audit Id ---- $auditId")
//        viewModel.loadZoneList(auditId)
    }


    companion object {
        fun newInstance(): ZoneListFragment {
            return ZoneListFragment()
        }

        private const val TAG = "ZoneListFragment"
        private const val FRAG_DIALOG = "ZoneDialogFragment"
    }
}