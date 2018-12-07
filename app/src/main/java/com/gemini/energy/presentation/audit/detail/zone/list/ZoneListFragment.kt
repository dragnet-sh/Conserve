package com.gemini.energy.presentation.audit.detail.zone.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneListBinding
import com.gemini.energy.internal.util.lazyThreadSafetyNone
import com.gemini.energy.presentation.audit.AuditActivity
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneCreateViewModel
import com.gemini.energy.presentation.audit.detail.zone.dialog.ZoneDialogFragment
import com.gemini.energy.presentation.audit.detail.zone.list.adapter.ZoneListAdapter
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.type.TypeActivity
import dagger.android.support.DaggerFragment
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ZoneListFragment : DaggerFragment(),

        ZoneListAdapter.OnZoneClickListener,
        ZoneDialogFragment.OnZoneCreateListener,

        View.OnClickListener {


    interface OnZoneSelectedListener {
        fun onZoneSelected(zone: ZoneModel, position: Int)
    }


    /*
    * View Model Setup - [ZoneListViewModel | ZoneCreateViewModel]
    * */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val zoneListViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(ZoneListViewModel::class.java)
    }

    private val zoneCreateViewModel by lazyThreadSafetyNone {
        ViewModelProviders.of(this, viewModelFactory).get(ZoneCreateViewModel::class.java)
    }


    /*
    * Binder - fragment_zone_list.xml
    * */
    private lateinit var binder: FragmentZoneListBinding


    /*
    * Audit Model is set by
    * 1. Audit Activity via Zone List Fragment (View Pager - Content)
    * 2. Type Activity
    * */
    private var auditModel: AuditModel? = null
    private var position = -1

    /*
    * Fragment Lifecycle Methods
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupListeners()
        setupArguments()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binder = DataBindingUtil.inflate(inflater,
                R.layout.fragment_zone_list, container, false)

        binder.viewModel = zoneListViewModel
        binder.callbacks = this
        binder.fabClick = this
        binder.activity = activity as BaseActivity
        binder.showCreate = true

        if (activity is TypeActivity) { binder.showCreate = false }
        binder.recyclerView.layoutManager = LinearLayoutManager(context)

        return binder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (position == -1) return
        val handler = Handler()
        handler.postDelayed({
            try {
                val vh = binder.recyclerView.findViewHolderForAdapterPosition(position)
                vh.itemView.findViewById<CardView>(R.id.card_view_zone).performClick()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 100)
    }

    private fun showCreateZone(zone: ZoneModel? = null) {
        val dialogFragment = ZoneDialogFragment()
        dialogFragment.zone = zone
        dialogFragment.show(childFragmentManager, FRAG_DIALOG)
    }

    private fun refreshViewModel() {
        auditModel?.let {
            zoneListViewModel.loadZoneList(it.id)
        }
    }

    private fun setupArguments() {
        val auditId = arguments?.getLong("auditId")
        val position = arguments?.getInt("position")

        auditId?.let {
            setAuditModel(AuditModel(auditId, "n/a", Date(), Date()))
        }

        position?.let { this.position = it }

        Timber.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        Timber.d("POSITION -- DEFAULT ZONE !! DEFAULT ZONE !! -- POSITION")
        Timber.d("$position")
        Timber.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
    }


    /**
     * Message Passing : Audit List Fragment <> Audit Activity <> Zone List Fragment
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


    /*
    * Listeners | Observers
    * */
    override fun onClick(v: View?) {
        showCreateZone()
    }

    override fun onZoneClick(view: View, item: ZoneModel, position: Int) {

        val intent = Intent(activity, TypeActivity::class.java)
        intent.putExtra(PARCEL_ZONE, item)
        intent.putExtra(PARCEL_AUDIT, auditModel)
        intent.putExtra(PARCEL_POSITION, position)

        this.position = position

        //Case 1: Navigate form Audit Activity to Type Activity
        if (activity is AuditActivity) {
            context?.let {
                ActivityCompat.startActivity(it, intent, null)
            }
        }

        //Case 2: Populate Zone Type for each of the Zone Click
        if (activity is TypeActivity) {
            val callbacks = activity as OnZoneSelectedListener
            callbacks.onZoneSelected(item, position)
            Timber.d("******************************************************************************")
            Timber.d("Zone Id : ${item.id} | Zone Name : ${item.name} | Audit Id : ${item.auditId}")
            Timber.d("Zone Position : $position")
            Timber.d("******************************************************************************")
        }

    }

    override fun onEditClick(view: View, item: ZoneModel) {
        showCreateZone(item)
    }

    override fun onDeleteClick(view: View, item: ZoneModel) {
        zoneListViewModel.deleteZone(item)
    }

    override fun onZoneCreate(args: Bundle) {
        auditModel?.let {
            zoneCreateViewModel.createZone(it.id, args.getString("zoneTag"))
        }
    }

    override fun onZoneUpdate(args: Bundle, zone: ZoneModel) {
        zoneCreateViewModel.updateZone(zone, args.getString("zoneTag"))
    }

    private fun setupListeners() {
        zoneCreateViewModel.result.observe(this, Observer {
            refreshViewModel()
        })
    }

    /*
    * Static Content
    * */
    companion object {
        fun newInstance(auditId: Long? = null, auditTag: String? = null, position: Int? = null): ZoneListFragment {
            val fragment = ZoneListFragment()

            fragment.arguments = Bundle().apply {
                auditId?.let {this.putLong("auditId", it)}
                auditTag?.let {this.putString("auditTag", it)}
                position?.let { this.putInt("position", it) }
            }

            return fragment
        }

        private const val FRAG_DIALOG = "ZoneDialogFragment"
        private const val PARCEL_ZONE = "EXTRA.ZONE"
        private const val PARCEL_AUDIT = "EXTRA.AUDIT"
        private const val PARCEL_POSITION = "EXTRA.POSITION.ZONE"
    }
}