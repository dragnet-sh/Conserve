package com.gemini.energy.presentation.audit.detail.zone.list.adapter

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import com.gemini.energy.presentation.base.BaseActivity

object ViewBindingAdapter {

    @JvmStatic
    @BindingAdapter("zoneList", "zoneListCallback", "activity", requireAll = false)
    fun setZoneListAdapter(recyclerView: RecyclerView, zoneList: List<ZoneModel>?,
                           zoneListCallback: ZoneListAdapter.OnZoneClickListener?,
                           activity: BaseActivity) {

        zoneList?.let {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = ZoneListAdapter(it, zoneListCallback, activity)
        }
    }

}