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
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneTypeListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.zone.dialog.ZoneTypeCreateViewModel
import com.gemini.energy.presentation.zone.dialog.ZoneTypeDialogFragment
import com.gemini.energy.presentation.zone.list.adapter.TypeListAdapter
import com.gemini.energy.presentation.zone.list.model.TypeModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TypeListFragment() : DaggerFragment(),

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
    private var zoneModel: ZoneModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupListeners()
        setupArguments()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binder = DataBindingUtil.inflate(inflater,
                R.layout.fragment_zone_type_list, container, false)

        binder.viewModel = typeListViewModel
        binder.callbacks = this
        binder.fabClick = this
        binder.showCreate = true

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

    private fun setupArguments() {
        val auditId = arguments?.getInt("auditId")
        val zoneId = arguments?.getInt("zoneId")
        val typeId = arguments?.getInt("typeId")
        val zoneName = arguments?.getString("zoneName")

        Log.d(TAG, "*********************************************")
        Log.d(TAG, "$auditId -- $zoneId -- $typeId -- $zoneName")
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

        fun newInstance(type: Int, zone: ZoneModel): TypeListFragment {
            val fragment = TypeListFragment()

            fragment.arguments = Bundle().apply {
                this.putInt("auditId", zone.auditId)
                this.putInt("zoneId", zone.id!!)
                this.putInt("typeId", type)
                this.putString("zoneName", zone.name)
            }

            return fragment
        }

        private const val FRAG_DIALOG = "TypeDialogFragment"
        private const val TAG = "TypeListFragment"

    }

}
