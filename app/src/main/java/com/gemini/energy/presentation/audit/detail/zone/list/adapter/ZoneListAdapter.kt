package com.gemini.energy.presentation.audit.detail.zone.list.adapter

import android.app.Activity
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneListItemBinding
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.type.TypeActivity
import timber.log.Timber

class ZoneListAdapter(private val items: List<ZoneModel>, private val callbacks: OnZoneClickListener? = null,
                      private val activity: Activity):
        RecyclerView.Adapter<ZoneListAdapter.ViewHolder>(),

        View.OnClickListener {

    interface OnZoneClickListener { // OnItemClickListener //
        fun onZoneClick(view: View, item: ZoneModel)
        fun onEditClick(view: View, item: ZoneModel)
        fun onDeleteClick(view: View, item: ZoneModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneListAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: FragmentZoneListItemBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_zone_list_item, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.zone = items[position]
        holder.binding.executePendingBindings()

        holder.binding.showClose = true
        holder.binding.showEdit = true
        holder.binding.buttonClick = this

        // *** Updating UI Based on the Parent Activity **** //
        if (activity is TypeActivity) {
            holder.binding.showClose = false
            holder.binding.showEdit = false
        }

    }

    override fun onClick(v: View?) {
        v?.let {
            val zone = it.tag as ZoneModel
            when (it.id) {
                R.id.button_update_zone -> callbacks?.onEditClick(it, zone)
                R.id.button_delete_zone -> callbacks?.onDeleteClick(it, zone)
            }
            Timber.d("On Click :: Zone - $zone")
        }
    }

    inner class ViewHolder(val binding: FragmentZoneListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {

            itemView.setOnClickListener {
                callbacks?.onZoneClick(it, items[adapterPosition])
            }

        }
    }

    companion object {
        private const val TAG = "ZoneListAdapter"
    }

}