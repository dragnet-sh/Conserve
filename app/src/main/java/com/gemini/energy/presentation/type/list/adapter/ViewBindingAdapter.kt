package com.gemini.energy.presentation.type.list.adapter

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.gemini.energy.App
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.type.list.model.TypeModel


object ViewBindingAdapter {

    @JvmStatic
    @BindingAdapter("zoneTypeList", "zoneTypeListCallback", "app", requireAll = false)
    fun setZoneTypeListAdapter(recyclerView: RecyclerView, zoneTypeList: List<TypeModel>?,
                               zoneTypeListCallback: TypeListAdapter.OnTypeClickListener?,
                               app: App) {

        zoneTypeList?.let {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = TypeListAdapter(it, zoneTypeListCallback, app)
        }

    }
}
