package com.gemini.energy.presentation.zone.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneTypeListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.zone.TypeActivity
import com.gemini.energy.presentation.zone.dialog.ZoneTypeCreateViewModel
import com.gemini.energy.presentation.zone.dialog.ZoneTypeDialogFragment
import com.gemini.energy.presentation.zone.list.adapter.TypeListAdapter
import com.gemini.energy.presentation.zone.list.model.TypeModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TypeListFragment : DaggerFragment(),

        TypeListAdapter.OnZoneTypeClickListener,
        ZoneTypeDialogFragment.OnAuditScopeCreateListener,

        View.OnClickListener {


    interface OnZoneTypeSelectedListener {
        fun onZoneTypeSelected(zone: TypeModel)
    }


    /*
    * View Model Setup - [ TypeListViewModel ]
    * */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val typeListViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(TypeListViewModel::class.java)
    }

    private val zoneTypeCreateViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(ZoneTypeCreateViewModel::class.java)
    }


    /*
    * Binder - fragment_zone_list.xml
    * */
    private lateinit var binder: FragmentZoneTypeListBinding

    private var typeModel: TypeModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupListeners()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binder = DataBindingUtil.inflate(inflater,
                R.layout.fragment_zone_type_list, container, false)

        binder.viewModel = typeListViewModel
        binder.callbacks = this
        binder.fabClick = this
        binder.activity = activity as BaseActivity
        binder.showCreate = true

        if (activity is TypeActivity) { binder.showCreate = false }

        binder.recyclerView.layoutManager = LinearLayoutManager(context)

        return binder.root

    }

    private fun showCreateZoneType() {
        val dialogFragment = ZoneTypeDialogFragment()
        dialogFragment.show(childFragmentManager, FRAG_DIALOG)
    }

    private fun refreshViewModel() {
        typeModel?.let {
            typeListViewModel.loadZoneTypeList(it.zoneId!!, it.type!!)
        }
    }

    /*
    * Listeners | Observers
    * */
    override fun onClick(v: View?) {
        showCreateZoneType()
    }


    // *** Navigator : Audit Activity <> Type Activity *** //
    override fun onZoneTypeClick(view: View, item: TypeModel) {

    }


    override fun onAuditScopeCreate(args: Bundle) {
        typeModel?.let {
            zoneTypeCreateViewModel.createZoneType(
                    it.zoneId!!,
                    args.getString("zoneType"),
                    args.getString("zoneTypeTag"),
                    it.auditId!!
            )
        }
    }

    private fun setupListeners() {
        zoneTypeCreateViewModel.result.observe(this, Observer {
            refreshViewModel()
        })
    }


    companion object {
        private const val FRAG_DIALOG = "TypeDialogFragment"
    }

}
