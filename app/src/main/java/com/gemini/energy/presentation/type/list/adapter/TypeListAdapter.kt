package com.gemini.energy.presentation.type.list.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.App
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneTypeListItemBinding
import com.gemini.energy.presentation.type.list.model.TypeModel
import timber.log.Timber


class TypeListAdapter(private val items: List<TypeModel>, private val callbacks: OnTypeClickListener? = null,
                      private val app: App) :
        RecyclerView.Adapter<TypeListAdapter.ViewHolder>(),

        View.OnClickListener {

    interface OnTypeClickListener {
        fun onTypeClick(view: View, typeModel: TypeModel)
        fun onEditClick(view: View, typeModel: TypeModel)
        fun onDeleteClick(view: View, typeModel: TypeModel)
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

        holder.binding.showClose = true
        holder.binding.showEdit = true
        holder.binding.buttonClick = this

        if (app.isChild()) {
            holder.binding.showClose = false
            holder.binding.showEdit = false
        }
    }

    override fun onClick(v: View?) {
        v?.let {
            val type = it.tag as TypeModel
            when (it.id) {
                R.id.button_update_zone -> callbacks?.onEditClick(it, type)
                R.id.button_delete_zone -> callbacks?.onDeleteClick(it, type)
            }
            Timber.d("On Click :: Type - $type")
        }
    }

    inner class ViewHolder(val binding: FragmentZoneTypeListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {

            itemView.setOnClickListener {
                callbacks?.onTypeClick(it, items[adapterPosition])
            }

        }

    }
}