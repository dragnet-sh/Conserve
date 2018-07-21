package com.gemini.energy.service.device.plugload

import android.util.Log
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRow
import com.gemini.energy.service.device.EBase
import io.reactivex.Flowable
import org.json.JSONObject

class Refrigerator(computable: Computable<*>) : EBase(computable), IComputable {

    /**
     * IMP !! This is the Main Compute Method
     * */
    override fun compute(): Flowable<List<OutgoingRow>> {
        super.initialize()
        super.compute(extra = {
            Log.d(TAG, it)
        })
                .subscribeOn(schedulers.subscribeOn)
                .observeOn(schedulers.observeOn)
                .subscribe {

                    Log.d(TAG, it.auditName)
                    Log.d(TAG, it.auditScopeName)
                    Log.d(TAG, it.zoneName)

                }

        return Flowable.just(listOf())
    }

    override fun efficientLookup() = true
    override fun queryFilter() = JSONObject()
            .put("data.style_type", "Reach-in")
            .put("data.total_volume", 17.89)
            .toString()

    companion object {
        private const val TAG = "Refrigerator"

        fun fields() = listOf(
                "company", "model_number", "style_type", "total_volume","daily_energy_use", "rebate",
                "pgne_measure_code", "purchase_price_per_unit", "vendor",

                "__daily_operating_hours", "__electric_energy", "__electric_cost"
        )
    }
}
