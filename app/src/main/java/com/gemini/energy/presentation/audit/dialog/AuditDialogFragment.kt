package com.gemini.energy.presentation.audit.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.gemini.energy.R
import com.gemini.energy.presentation.audit.list.adapter.AuditListAdapter
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.util.Navigator
import com.gemini.energy.presentation.util.hideInput
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Pattern
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AuditDialogFragment : DialogFragment(), Validator.ValidationListener {

//    @NotEmpty
//    @Pattern(regex = "^\\d+$")
//    private lateinit var auditId: EditText
    var audit: AuditModel? = null

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

        view.findViewById<Button>(R.id.btn_cancel_audit).setOnClickListener { dismiss(); view.hideInput() }
        view.findViewById<Button>(R.id.btn_save_audit).setOnClickListener { validator.validate() }

        auditTag = view.findViewById(R.id.edt_create_audit_tag)
        audit?.let {
            auditTag.setText(it.name)
        }

        return view
    }


    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        navigator.message("Audit Create - Form Validation Failed.")
    }

    override fun onValidationSucceeded() {
        val args = Bundle().apply {
            this.putString("auditTag", auditTag.text.toString())
        }

        val callbacks: OnAuditCreateListener? = if (parentFragment == null) {
            fragmentManager?.findFragmentByTag(TAG_AUDIT_LIST) as OnAuditCreateListener
        } else { parentFragment as OnAuditCreateListener }

        if (audit == null) { callbacks?.onAuditCreate(args) }
        else { callbacks?.onAuditUpdate(args, audit!!) }

        dismiss()
        view?.hideInput()
    }


    companion object {
        private const val TAG_AUDIT_LIST = "AuditListFragment"
    }

    interface OnAuditCreateListener {
        fun onAuditCreate(args: Bundle)
        fun onAuditUpdate(args: Bundle, audit: AuditModel)
    }
}
