package com.gemini.energy.presentation.zone.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.gemini.energy.App
import com.gemini.energy.R
import com.gemini.energy.presentation.util.EApplianceType
import com.gemini.energy.presentation.util.EZoneType
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty


class TypeDialogFragment : DialogFragment(), Validator.ValidationListener {

    @NotEmpty
    private lateinit var scopeName: EditText

    @NotEmpty
    private lateinit var scopeType: String

    private lateinit var scopeSpinner: Spinner
    private lateinit var validator: Validator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        validator = Validator(this)
        validator.setValidationListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_type_parent_dialog, container, false)
        dialog.setTitle(R.string.menu_create_audit_scope_parent)

        view.findViewById<Button>(R.id.btn_cancel_scope).setOnClickListener { dismiss() }
        view.findViewById<Button>(R.id.btn_save_scope).setOnClickListener { validator.validate() }

        scopeName = view.findViewById(R.id.edt_create_audit_scope_parent_name)
        scopeSpinner = view.findViewById(R.id.spin_audit_scope) as Spinner

        val scopeOptions  = getScopeOptions()
        scopeSpinner.adapter = ArrayAdapter(activity, R.layout.support_simple_spinner_dropdown_item, scopeOptions)

        scopeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Toast.makeText(activity, getString(R.string.selected_item) + " " + scopeOptions[position], Toast.LENGTH_SHORT).show()
                scopeType = scopeOptions[position].name
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        Toast.makeText(activity, "Audit Entity Create - Validation Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onValidationSucceeded() {
        var args = Bundle().apply {
            this.putString("typeTag", scopeName.text.toString())
            this.putString("type", scopeType)
        }

        if (parentFragment != null) {
            val callbacks = parentFragment as OnTypeCreateListener?
            callbacks?.onTypeCreate(args)
        }

        dismiss()
    }

    /*
    * Options depend on the Type ToDo: Need to Expand on this thought !!
    * */
    private fun getScopeOptions() = when(app.getCount() == 0) {
        true -> EZoneType.values()
        false -> EApplianceType.values()
    }

    companion object {
        private val app = App.instance
        private const val TAG = "TypeDialogFragment"
    }

    interface OnTypeCreateListener {
        fun onTypeCreate(args: Bundle)
    }

}