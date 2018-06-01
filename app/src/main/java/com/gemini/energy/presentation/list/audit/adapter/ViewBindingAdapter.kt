package com.gemini.energy.presentation.list.audit.adapter

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.gemini.energy.presentation.list.audit.model.AuditModel

object ViewBindingAdapter {

    @JvmStatic
    @BindingAdapter("auditList", "auditListCallback", requireAll = false)
    fun setAuditListAdapter(recyclerView: RecyclerView, auditList: List<AuditModel>?,
                            auditListCallback: AuditListAdapter.Callbacks?) {

        auditList?.let {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = AuditListAdapter(it, auditListCallback)
        }

    }
}