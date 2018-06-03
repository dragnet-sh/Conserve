package com.gemini.energy.presentation.audit.detail.zone.list.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gemini.energy.R
import com.gemini.energy.databinding.FragmentZoneListItemBinding
import com.gemini.energy.presentation.audit.detail.zone.list.model.ZoneModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class ZoneListAdapter(private val items: List<ZoneModel>, private val callbacks: OnZoneClickListener? = null):
    RecyclerView.Adapter<ZoneListAdapter.ViewHolder>() {

    interface OnZoneClickListener { // OnItemClickListener //
        fun onZoneClick(view: View, item: ZoneModel)
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
    }


    inner class ViewHolder(val binding: FragmentZoneListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {

            itemView.setOnClickListener {

                // *** CREATE AN OBSERVABLE SO THAT ANY FRAGMENT CAN SUBSCRIBE TO IT **** //

                Observable.just("item").subscribe(
                       object: Observer<String> {
                           override fun onComplete() { Log.d(TAG, "JUST ON COMPLETE") }
                           override fun onSubscribe(d: Disposable) { Log.d(TAG, "JUST ON SUBSCRIBE")}
                           override fun onNext(t: String) { Log.d(TAG, "JUST -- $t")}
                           override fun onError(e: Throwable) { Log.d(TAG, "Just ON ERROR -- ${e.printStackTrace()}") }
                       }
                )

                callbacks?.onZoneClick(it, items[adapterPosition])
            }

        }
    }

    companion object {
        private const val TAG = "ZoneListAdapter"
    }

}