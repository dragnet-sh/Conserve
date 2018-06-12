package com.gemini.energy.presentation.zone.list.adapter

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.gemini.energy.presentation.zone.list.model.TypeModel


object ViewBindingAdapter {

    @JvmStatic
    @BindingAdapter("zoneTypeList", "zoneTypeListCallback", requireAll = false)
    fun setZoneTypeListAdapter(recyclerView: RecyclerView, zoneTypeList: List<TypeModel>?,
                               zoneTypeListCallback: TypeListAdapter.OnTypeClickListener?) {

        zoneTypeList?.let {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = TypeListAdapter(it, zoneTypeListCallback)
        }

    }
}
