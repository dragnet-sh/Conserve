package com.gemini.energy.presentation.type.feature

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.gemini.energy.R
import com.gemini.energy.presentation.base.BaseFormFragment
import com.gemini.energy.presentation.type.SharedViewModel
import com.gemini.energy.presentation.type.list.model.TypeModel
import com.gemini.energy.presentation.util.EApplianceType
import com.gemini.energy.presentation.util.EZoneType

class FeatureDataFragment : BaseFormFragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private var typeModel: TypeModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
        sharedViewModel.getType().observe(this,
                Observer<TypeModel> {
                    this.typeModel = it
                    super.loadForm()
                })
    }

    override fun resourceId(): Int {

        var rawId: Int = R.raw.preaudit_sample

        typeModel?.let { model ->
            if (model.type == EZoneType.Plugload.value) {
                if (model.subType == EApplianceType.CombinationOven.value) {rawId = R.raw.combination_oven}
                if (model.subType == EApplianceType.ConvectionOven.value) {rawId = R.raw.convection_oven}
                if (model.subType == EApplianceType.ConveyorOven.value) {rawId = R.raw.conveyor_oven}
                if (model.subType == EApplianceType.Refrigerator.value) {rawId = R.raw.freezer_fridge}
                if (model.subType == EApplianceType.Fryer.value) {rawId = R.raw.fryer}
                if (model.subType == EApplianceType.IceMaker.value) {rawId = R.raw.icemaker}
                if (model.subType == EApplianceType.RackOven.value) {rawId = R.raw.rack_oven}
                if (model.subType == EApplianceType.SteamCooker.value) {rawId = R.raw.steam_cooker}
            }
            else if (model.type == EZoneType.HVAC.value) {rawId = R.raw.hvac}
            else if (model.type == EZoneType.Motors.value) {rawId = R.raw.motors}
            else if (model.type == EZoneType.Lighting.value) {rawId = R.raw.lighting}

        }

        return rawId
    }

    companion object {

        fun newInstance(): FeatureDataFragment {
            return FeatureDataFragment()
        }

        private const val TAG = "FeatureDataFragment"
    }

}