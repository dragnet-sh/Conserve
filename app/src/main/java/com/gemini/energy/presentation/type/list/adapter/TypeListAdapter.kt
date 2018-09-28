package com.gemini.energy.presentation.type.list.adapter

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.gemini.energy.App
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneTypeListItemBinding
import com.gemini.energy.presentation.type.list.model.TypeModel
import timber.log.Timber


class TypeListAdapter(private val items: List<TypeModel>, private val callbacks: OnTypeClickListener? = null,
                      private val app: App) :
        RecyclerView.Adapter<TypeListAdapter.ViewHolder>() {

    interface OnTypeClickListener {
        fun onTypeClick(view: View, typeModel: TypeModel, position: Int)
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

    private var currentPosition = RecyclerView.NO_POSITION

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.type = items[position]
        holder.binding.executePendingBindings()

        holder.binding.showClose = true
        holder.binding.showEdit = true

        if (app.isChild()) {
            holder.binding.showClose = false
            holder.binding.showEdit = false
        }

        when (currentPosition == position) {
            true    -> holder.cardViewType?.setBackgroundColor(Color.LTGRAY)
            false   -> holder.cardViewType?.setBackgroundColor(Color.WHITE)
        }

        holder.cardViewType?.setOnClickListener {
            callbacks?.onTypeClick(it, items[position], position)
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

    inner class ViewHolder(val binding: FragmentZoneTypeListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        var cardViewType: CardView? = null
        var deleteImageButton: ImageButton? = null
        var editImageButton: ImageButton? = null

        init {
            cardViewType = itemView.findViewById(R.id.card_view_type)
            deleteImageButton = itemView.findViewById(R.id.button_delete_type)
            editImageButton = itemView.findViewById(R.id.button_update_type)
        }

    }
}