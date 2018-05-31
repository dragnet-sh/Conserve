package com.gemini.energy.presentation.list

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.gemini.energy.R
import com.gemini.energy.presentation.navigation.Navigator
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Pattern
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AuditDialogFragment : DialogFragment(), Validator.ValidationListener {

    @NotEmpty
    @Pattern(regex = "^\\d+$")
    private lateinit var auditId: EditText

    @NotEmpty
    private lateinit var auditTag: EditText

    //ToDo: Check why this fails from Dagger
    private lateinit var validator: Validator

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        validator = Validator(this)
        validator.setValidationListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_audit_dialog, container, false)
        dialog.setTitle(R.string.create_audit)

        view.findViewById<Button>(R.id.btn_cancel_audit).setOnClickListener { dismiss() }
        view.findViewById<Button>(R.id.btn_save_audit).setOnClickListener { validator.validate() }

        auditId = view.findViewById(R.id.edt_create_audit_id)
        auditTag = view.findViewById(R.id.edt_create_audit_tag)

        return view
    }


    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        navigator.message("Audit Create - Form Validation Failed.")
    }

    override fun onValidationSucceeded() {
        var args = Bundle().apply {
            this.putInt("auditId", auditId.text.toString().toInt())
            this.putString("auditTag", auditTag.text.toString())
        }

        val callbacks = fragmentManager?.findFragmentByTag(TAG_AUDIT_LIST) as Callbacks
        callbacks.onAuditCreate(args)
        dismiss()
    }


    companion object {
        private const val TAG = "AuditDialogFragment"
        private const val TAG_AUDIT_LIST = "AuditListFragment"
    }

    interface Callbacks {
        fun onAuditCreate(args: Bundle)
    }
}
