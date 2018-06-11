package com.gemini.energy.presentation.zone.list.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneTypeListItemBinding
import com.gemini.energy.presentation.zone.list.model.TypeModel


class TypeListAdapter(private val items: List<TypeModel>, private val callbacks: OnZoneTypeClickListener? = null) :
        RecyclerView.Adapter<TypeListAdapter.ViewHolder>() {

    interface OnZoneTypeClickListener { // OnItemClickListener //
        fun onZoneTypeClick(view: View, item: TypeModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: FragmentZoneTypeListItemBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_type_list_item, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount() = items.count()


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.type = items[position]
        holder.binding.executePendingBindings()
    }

    inner class ViewHolder(val binding: FragmentZoneTypeListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {

            itemView.setOnClickListener {
                callbacks?.onZoneTypeClick(it, items[adapterPosition])
            }

        }

    }
}