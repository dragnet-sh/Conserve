package com.gemini.energy.presentation.audit.detail.zone.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.gemini.energy.R
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.util.Navigator
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ZoneDialogFragment: DialogFragment(), Validator.ValidationListener {

    @NotEmpty
    private lateinit var zoneTag: EditText
    var zone: ZoneModel? = null

    @Inject
    lateinit var navigator: Navigator

    private lateinit var validator: Validator

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        validator = Validator(this)
        validator.setValidationListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_zone_dialog, container, false)
        view.findViewById<Button>(R.id.btn_cancel_zone).setOnClickListener { dismiss() }
        view.findViewById<Button>(R.id.btn_save_zone).setOnClickListener { validator.validate() }

        zoneTag = view.findViewById(R.id.edt_create_zone_tag)
        zone?.let {
            zoneTag.setText(it.name)
        }

        return view
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        navigator.message("Zone Create - Form Validation Failed.")
    }

    override fun onValidationSucceeded() {
        val args = Bundle().apply {
            this.putString("zoneTag", zoneTag.text.toString())
        }

        // *** IMP: Callback Switcher *** //

        val callbacks: OnZoneCreateListener? = if (parentFragment == null) {
            activity as OnZoneCreateListener
        } else {
            parentFragment as OnZoneCreateListener
        }

        if (zone == null) { callbacks?.onZoneCreate(args) }
        else { callbacks?.onZoneUpdate(args, zone!!) }

        dismiss()
    }

    companion object {
        private const val TAG = "AuditDialogFragment"
        private const val FRAG_ZONE_LIST = "ZoneListFragment"
    }

    interface OnZoneCreateListener {
        fun onZoneCreate(args: Bundle)
        fun onZoneUpdate(args: Bundle, zone: ZoneModel)
    }

}