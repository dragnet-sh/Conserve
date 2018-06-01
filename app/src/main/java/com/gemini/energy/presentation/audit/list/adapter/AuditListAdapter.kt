package com.gemini.energy.presentation.audit.list.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.databinding.FragmentAuditListItemBinding

class AuditListAdapter(private val items: List<AuditModel>, private val callbacks: Callbacks? = null):
        RecyclerView.Adapter<AuditListAdapter.ViewHolder>() {

    interface Callbacks {
        fun onItemClick(view: View, item: AuditModel)
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
    }

    inner class ViewHolder(val binding: FragmentAuditListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                callbacks?.onItemClick(it, items[adapterPosition])
            }
        }
    }
}