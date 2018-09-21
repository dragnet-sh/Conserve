package com.gemini.energy.presentation.audit.list.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentAuditListItemBinding
import com.gemini.energy.presentation.audit.list.model.AuditModel
import io.reactivex.Observable
import timber.log.Timber

class AuditListAdapter(private val items: List<AuditModel>, private val callbacks: OnAuditClickListener? = null) :
        RecyclerView.Adapter<AuditListAdapter.ViewHolder>() {

    interface OnAuditClickListener {
        fun onAuditClick(observable: Observable<AuditModel>)
        fun onEditClick(view: View, item: AuditModel)
        fun onDeleteClick(view: View, item: AuditModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: FragmentAuditListItemBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_audit_list_item, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.audit = items[position]
        holder.binding.executePendingBindings()

        holder.auditTextView?.setOnClickListener {
            callbacks?.onAuditClick(Observable.just(items[position]))
        }

        holder.deleteImageButton?.setOnClickListener {
            val audit = it.tag as AuditModel
            callbacks?.onDeleteClick(it, audit)
        }

        holder.updateImageButton?.setOnClickListener {
            val audit = it.tag as AuditModel
            callbacks?.onEditClick(it, audit)
        }
    }

    inner class ViewHolder(val binding: FragmentAuditListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        var auditTextView: TextView? = null
        var deleteImageButton: ImageButton? = null
        var updateImageButton: ImageButton? = null

        init {
            auditTextView = itemView.findViewById(R.id.text_audit_id)
            deleteImageButton = itemView.findViewById(R.id.info_button)
            updateImageButton = itemView.findViewById(R.id.edit_button)
        }

    }

    companion object {
        private const val TAG = "AuditListAdapter"
    }
}