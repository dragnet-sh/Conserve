package com.gemini.energy.presentation.zone.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.App
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneTypeListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.util.EAction
import com.gemini.energy.presentation.util.EZoneType
import com.gemini.energy.presentation.zone.TypeActivity
import com.gemini.energy.presentation.zone.dialog.TypeCreateViewModel
import com.gemini.energy.presentation.zone.dialog.TypeDialogFragment
import com.gemini.energy.presentation.zone.list.adapter.TypeListAdapter
import com.gemini.energy.presentation.zone.list.model.TypeModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TypeListFragment : DaggerFragment(),

        TypeListAdapter.OnTypeClickListener,
        TypeDialogFragment.OnTypeCreateListener,

        View.OnClickListener {


    interface OnTypeSelectedListener {
        fun onTypeSelected(type: TypeModel)
    }


    /*
    * View Model Setup - [ TypeListViewModel ]
    * */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val typeListViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(TypeListViewModel::class.java)
    }

    private val typeCreateViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(TypeCreateViewModel::class.java)
    }


    /*
    * Binder - fragment_zone_list.xml
    * */
    private lateinit var binder: FragmentZoneTypeListBinding

    private var auditModel: AuditModel? = null
    private var zoneModel: ZoneModel? = null
    private var typeModel: TypeModel? = null //ToDo: When do you set this ??


    /*
    * Type Id is used to query the specific Type
    * [Plugload - Motors - HVAC - Lighting - Others]
    * */
    private var typeId: Int? = null


    /*
    * Fragment Life Cycle Methods
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupArguments()
        setupListeners()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binder = DataBindingUtil.inflate(inflater,
                R.layout.fragment_type_list, container, false)

        binder.viewModel = typeListViewModel
        binder.callbacks = this
        binder.fabClick = this
        binder.showCreate = true

        binder.recyclerView.layoutManager = LinearLayoutManager(context)

        return binder.root

    }

    private fun showCreateZoneType() {
        val dialogFragment = TypeDialogFragment()
        dialogFragment.show(childFragmentManager, FRAG_DIALOG)
    }

    private fun refreshViewModel() {
        zoneModel?.let {zone ->
            typeId?.let { typeId ->
                typeListViewModel.loadZoneTypeList(
                        zone.id!!,
                        getType(typeId)
                )
            }
        }
    }

    private fun setupArguments() {

        val zone = arguments?.getParcelable<ZoneModel>(PARCEL_ZONE)
        val audit = arguments?.getParcelable<AuditModel>(PARCEL_AUDIT)

        setAuditModel(audit)
        setZoneModel(zone)
        setTypeId(arguments?.getInt("typeId"))

    }

    /*
    * Listeners | Observers
    * */
    override fun onClick(v: View?) {
        showCreateZoneType()
    }


    // *** Navigator : Audit Activity <> Type Activity *** //
    override fun onTypeClick(view: View, itemModel: TypeModel) {

        // Step 1 : Check if the Type has a Child
        // Step 2 : If it has a Child load the Type Activity with the proper set of Dialog
        // Step 3 : If it has no Child - Load the Feature Form

        if (itemModel.type == EZoneType.Plugload.value) {

            if (app.getCount() == 0) {

                // 1. Pass a message to the Type Activity that this is a Child View for Plugload
                // 2. Side List -> Has Plugload Types
                // 3. View Pager -> Has only one Element the PlugLoad Child Elements

                Log.d(TAG, "<<<<< AUDIT MODEL >>>>>")
                Log.d(TAG, auditModel.toString())

                Log.d(TAG, "<<<<< ZONE MODEL >>>>>")
                Log.d(TAG, zoneModel.toString())

                Log.d(TAG, "<<<<< ITEM MODEL >>>>>")
                Log.d(TAG, itemModel.toString())

                Log.d(TAG, "<<<<< TYPE ID >>>>>")
                Log.d(TAG, typeId.toString())
                Log.d(TAG, getType(typeId!!))

                val intent = Intent(activity, TypeActivity::class.java)
                intent.putExtra(PARCEL_AUDIT, auditModel)
                intent.putExtra(PARCEL_ZONE, zoneModel)
                intent.putExtra(PARCEL_TYPE, itemModel)
                intent.putExtra("typeId", typeId as Int)

                context?.let {
                    ActivityCompat.startActivity(it, intent, null)
                }

                app.setCounter(EAction.Push, itemModel)

            } else {

                // 1. Load the Form
                // 2. When you get back from Form perhaps Pop the Stack

            }

        } else {

            // 1. Side List -> Has Parent Elements for the Specific Type
            // 2. View Pager -> Has the Form for the Specific Type for the Selected Parent

            Log.d(TAG, "N/A -- N/A -- N/A")

        }

    }

    override fun onTypeCreate(args: Bundle) {

        val typeTag = args.getString("typeTag")
        val type= args.getString("type")

        Log.d(TAG, typeTag)
        Log.d(TAG, type)

        zoneModel?.let {zone ->
            if (zone.id != null) {
                typeCreateViewModel.createZoneType(
                        zone.id,
                        type,
                        typeTag,
                        zone.auditId
                )
            }
        }
    }

    private fun setupListeners() {
        typeCreateViewModel.result.observe(this, Observer {
            refreshViewModel()
        })
    }

    private fun setAuditModel(audit: AuditModel?) {
        Log.d(TAG, "Setting Audit Model -- ${audit.toString()}")
        this.auditModel = audit
    }

    fun setZoneModel(zone: ZoneModel?) {
        Log.d(TAG, "Setting Zone Model -- ${zone.toString()}")
        this.zoneModel = zone
        refreshViewModel()
    }

    private fun setTypeId(typeId: Int?) {
        this.typeId = typeId
    }

    private fun getType(pagerIndex: Int): String {
        return when(pagerIndex) {
            0 -> EZoneType.Plugload.value
            1 -> EZoneType.HVAC.value
            2 -> EZoneType.Lighting.value
            3 -> EZoneType.Motors.value
            else -> EZoneType.Others.value
        }
    }

    companion object {

        fun newInstance(type: Int, zone: ZoneModel, audit: AuditModel): TypeListFragment {
            val fragment = TypeListFragment()

            fragment.arguments = Bundle().apply {
                this.putParcelable(PARCEL_ZONE, zone)
                this.putParcelable(PARCEL_AUDIT, audit)
                this.putInt("typeId", type)
            }

            return fragment
        }

        private val app = App.instance
        private const val FRAG_DIALOG = "TypeDialogFragment"
        private const val TAG = "TypeListFragment"

        private const val PARCEL_AUDIT = "EXTRA.AUDIT"
        private const val PARCEL_ZONE = "EXTRA.ZONE"
        private const val PARCEL_TYPE = "EXTRA.TYPE"

    }

}
