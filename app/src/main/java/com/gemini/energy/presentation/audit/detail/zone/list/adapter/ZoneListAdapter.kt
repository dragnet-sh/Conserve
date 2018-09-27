package com.gemini.energy.presentation.audit.detail.zone.list.adapter

import android.app.Activity
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneListItemBinding
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.type.TypeActivity
import timber.log.Timber

class ZoneListAdapter(private val items: List<ZoneModel>, private val callbacks: OnZoneClickListener? = null,
                      private val activity: Activity):
        RecyclerView.Adapter<ZoneListAdapter.ViewHolder>() {

    interface OnZoneClickListener { // OnItemClickListener //
        fun onZoneClick(view: View, item: ZoneModel, position: Int)
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

    private var currentPosition = RecyclerView.NO_POSITION

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.zone = items[position]
        holder.binding.executePendingBindings()

        holder.binding.showClose = true
        holder.binding.showEdit = true

        // *** Updating UI Based on the Parent Activity **** //
        if (activity is TypeActivity) {
            holder.binding.showClose = false
            holder.binding.showEdit = false
        }

        when (currentPosition == position) {
            true    -> holder.cardViewZone?.setBackgroundColor(Color.LTGRAY)
            false   -> holder.cardViewZone?.setBackgroundColor(Color.WHITE)
        }

        holder.cardViewZone?.setOnClickListener {
            callbacks?.onZoneClick(it, items[position], position)
            currentPosition = position
            notifyDataSetChanged()
        }

        holder.deleteImageButton?.setOnClickListener {
            callbacks?.onDeleteClick(it, items[position])
        }

        holder.editImageButton?.setOnClickListener {
            callbacks?.onEditClick(it, items[position])
        }
    }

    inner class ViewHolder(val binding: FragmentZoneListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        var cardViewZone: CardView? = null
        var deleteImageButton: ImageButton? = null
        var editImageButton: ImageButton? = null

        init {
            cardViewZone = itemView.findViewById(R.id.card_view_zone)
            deleteImageButton = itemView.findViewById(R.id.button_delete_zone)
            editImageButton = itemView.findViewById(R.id.button_update_zone)
        }
    }

}