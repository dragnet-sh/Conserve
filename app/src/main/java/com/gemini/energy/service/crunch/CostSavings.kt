package com.gemini.energy.service.crunch

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.presentation.util.ERateKey
import com.gemini.energy.service.DataHolder
import com.gemini.energy.service.type.UsageHours
import com.gemini.energy.service.type.UtilityRate
import io.reactivex.functions.Function
import timber.log.Timber
import java.util.*

class CostSavings {

    class Mapper : Function<Unit, DataHolder> {

        lateinit var computable: Computable<*>
        lateinit var usageHoursSpecific: UsageHours
        lateinit var usageHoursBusiness: UsageHours
        lateinit var electricRateStructure: String
        lateinit var electricityUtilityRate: UtilityRate
        lateinit var gasUtilityRate: UtilityRate
        lateinit var featureData: Map<String, Any>
        lateinit var powerTimeChange: PowerTimeChange

        lateinit var hourlyEnergyUsagePre: () -> Double

        override fun apply(unit: Unit): DataHolder {

            Timber.d("----::::---- $computable ----::::----")

            /**
             * Pre UsageHours Hours - Mapped Peak Hours (Specific)
             * */
            val preUsageByPeak = usageHoursSpecific.mappedPeakHourYearly()
            val preHoursOnPeakPricing = preUsageByPeak[ERateKey.SummerOn]!! * .504
            val preHoursOnPartPeakPricing = preUsageByPeak[ERateKey.SummerPart]!! * .504 +
                    preUsageByPeak[ERateKey.WinterPart]!! * .496 
            val preHoursOnOffPeakPricing = preUsageByPeak[ERateKey.SummerOff]!! * .504 +
                    preUsageByPeak[ERateKey.WinterOff]!! * .496

            Timber.d("----::::---- Pre UsageHours By Peak ($preUsageByPeak) ----::::----")
            Timber.d("----::::---- Pre Hours On Peak Pricing ($preHoursOnPeakPricing) ----::::----")
            Timber.d("----::::---- Pre Hours On Part Peak Pricing ($preHoursOnPartPeakPricing) ----::::----")
            Timber.d("----::::---- Pre Hours On Off Peak Pricing ($preHoursOnOffPeakPricing) ----::::----")

            /**
             * Post UsageHours Hours - Mapped Peak Hours (Business)
             * */
            val postUsageByPeak = usageHoursBusiness.mappedPeakHourYearly()
            val postHoursOnPeakPricing = postUsageByPeak[ERateKey.SummerOn]!! * .504
            val postHoursOnPartPeakPricing = postUsageByPeak[ERateKey.SummerPart]!! * .504 +
                    postUsageByPeak[ERateKey.WinterPart]!! * .496 
            val postHoursOnOffPeakPricing = postUsageByPeak[ERateKey.SummerOff]!! * .504 +
                    postUsageByPeak[ERateKey.WinterOff]!! * .496

            Timber.d("----::::---- Post UsageHours By Peak ($postUsageByPeak) ----::::----")
            Timber.d("----::::---- Post Hours On Peak Pricing ($postHoursOnPeakPricing) ----::::----")
            Timber.d("----::::---- Post Hours On Part Peak Pricing ($postHoursOnPartPeakPricing) ----::::----")
            Timber.d("----::::---- Post Hours On Off Peak Pricing ($postHoursOnOffPeakPricing) ----::::----")

            /**
             * UtilityRate Rate - Electricity
             * ToDo - Verify if dividing by 2 is correct to find the average between the Summer | Winter Rate
             * */

            var peakPrice = 0.0
            var partPeakPrice = 0.0
            var offPeakPrice = 0.0

            if (electricRateStructure.matches("^.*TOU$".toRegex())) {

                peakPrice = electricityUtilityRate.structure[ERateKey.SummerOn.value]!![0].toDouble()
                partPeakPrice =
                        (electricityUtilityRate.structure[ERateKey.SummerPart.value]!![0].toDouble()
                                + electricityUtilityRate.structure[ERateKey.WinterPart.value]!![0].toDouble()) / 2
                offPeakPrice =
                        (electricityUtilityRate.structure[ERateKey.SummerOff.value]!![0].toDouble()
                                + electricityUtilityRate.structure[ERateKey.WinterOff.value]!![0].toDouble()) / 2
            }

            Timber.d("----::::---- Electricity Peak Price ($peakPrice) ----::::----")
            Timber.d("----::::---- Electricity Part Peak Price ($partPeakPrice) ----::::----")
            Timber.d("----::::---- Electricity Off Peak Price ($offPeakPrice) ----::::----")

            /**
             * UtilityRate Rate - Gas
             * */
            val winterRate = gasUtilityRate.structure[ERateKey.GasWinter.value]!![0].toDouble()
            val summerRate = gasUtilityRate.structure[ERateKey.GasSummer.value]!![0].toDouble()

            Timber.d("----::::---- Gas Winter Rate ($winterRate) ----::::----")
            Timber.d("----::::---- Gas Summer Rate ($summerRate) ----::::----")

            /**
             * Parse API Energy Efficient Database - materialCost
             * Parse API Labor Cost - laborCost
             * */
            fun purchasePricePerUnit(): Double {
                if ((computable.energyPostStateLeastCost.count() > 0) &&
                        computable.energyPostStateLeastCost[0].containsKey("purchase_price_per_unit")) {

                    try {
                        return computable.energyPostStateLeastCost[0].getValue("purchase_price_per_unit").toDouble()
                    } catch (exception: Exception) {
                        Timber.e("Exception @ Purchase Price Unit - toDouble")
                    }

                }

                return 0.0
            }

            val materialCost = purchasePricePerUnit()
            Timber.d("----::::---- Material Cost ($materialCost) ----::::----")

            val laborCost = computable.laborCost
            Timber.d("----::::---- Labor Cost ($laborCost) ----::::----")

            /**
             * ToDo - Where do we get these values from ??
             * */
            val maintenanceCostSavings = 0.0
            val otherEquipmentSavings = 0.0

            Timber.d( "----::::---- Maintenance Cost Savings ($maintenanceCostSavings) ----::::----")
            Timber.d( "----::::---- Other Equipment Savings ($otherEquipmentSavings) ----::::----")

            /**
             * Parse API Energy Efficient Database - Rebate
             * */
            fun rebate(): Double {
                if ((computable.energyPostStateLeastCost.count() > 0) &&
                        computable.energyPostStateLeastCost[0].containsKey("rebate")) {

                    try {
                        return computable.energyPostStateLeastCost[0].getValue("rebate").toDouble()
                    } catch (exception: Exception) {
                        Timber.e("Exception @ Rebate - toDouble")
                    }

                }

                return 0.0
            }
            val incentives = rebate()

            Timber.d("----::::---- Incentive ($incentives) ----::::----")


            /**
             * Fetch these from the UtilityRate Rate Structure
             * ToDo - Verify if A1 | A10 | E19 qualify as Non TOU
             * ToDo - Verify if dividing by 2 is correct to find the average between the Summer | Winter Rate
             * ToDo - Demand Charge can be Empty - Right now i have added 0 in the CSV for the empty ones - need to fix this
             * */
            //We can not use blended energy rate because winter and summer energy are different. Therefore we need two
            //distinct rates for summer and winter [Kinslow]

            var nonTOUEnergyRateSummer = 0.0
            var nonTOUEnergyRateWinter = 0.0
            if (!electricRateStructure.matches("^.*TOU$".toRegex())) {
                nonTOUEnergyRateSummer = electricityUtilityRate.structure[ERateKey.SummerNone.value]!![0].toDouble()
                nonTOUEnergyRateWinter = electricityUtilityRate.structure[ERateKey.WinterNone.value]!![0].toDouble()
            }

            Timber.d("----::::---- Non TOU Energy Rate Summer ($nonTOUEnergyRateSummer) ----::::----")
            Timber.d("----::::---- Non TOU Energy Rate Winter ($nonTOUEnergyRateWinter) ----::::----")

            val blendedDemandRate = if (!electricRateStructure.matches("^.*TOU$".toRegex())) {
                (electricityUtilityRate.structure[ERateKey.SummerNone.value]!![2].toDouble() +
                        electricityUtilityRate.structure[ERateKey.WinterNone.value]!![2].toDouble()) / 2
            } else {
                (electricityUtilityRate.structure[ERateKey.SummerOff.value]!![2].toDouble() +
                        electricityUtilityRate.structure[ERateKey.WinterOff.value]!![2].toDouble()) / 2
            }

            Timber.d("----::::---- Blended Demand Rate ($blendedDemandRate) ----::::----")

            /**
             * UtilityRate Rate Structure
             * */
            val getRateSchedule = electricRateStructure

            Timber.d("----::::---- Rate Schedule ($getRateSchedule) ----::::----")

            /**
             * Flag to denote a gas based equipment
             * ToDo - Create input and get it from the feature data
             * */
            val checkForGas = false

            Timber.d("----::::---- Check For Gas ($checkForGas) ----::::----")

            /**
             * Energy UsageHours - Efficient Alternative (Post State)
             * ToDo - Write the Energy Use for Lighting into the Mapped List
             * */
            fun energyUse(): Double {
                if ((computable.energyPostStateLeastCost.count() > 0) &&
                        computable.energyPostStateLeastCost[0].containsKey("daily_energy_use")) {

                    try {
                        return computable.energyPostStateLeastCost[0].getValue("daily_energy_use").toDouble()
                    } catch (exception: Exception) {
                        Timber.e("Exception @ Energy Use (Post State) - toDouble")
                    }
                }

                return 0.0

            }

            val energyUse = energyUse()

            Timber.d("----::::---- Energy Use ($energyUse) ----::::----")

            /**
             * Power, Time Change or Both
             * */
            val ptc = powerTimeChange.delta(computable)
            val powerChangeCheck = ptc.checkPowerChange
            val timeChangeCheck = ptc.checkTimeChange
            val powerTimeChangeCheck = ptc.checkPowerTimeChange

            Timber.d("----::::---- Power Change Check ($powerChangeCheck) ----::::----")
            Timber.d("----::::---- Time Change Check ($timeChangeCheck) ----::::----")
            Timber.d("----::::---- Power Time Change Check ($powerTimeChangeCheck) ----::::----")

            val multiplePowerCheck = false
            val multipleTimeCheck = false
            val multiplePowerTimeCheck = false

            Timber.d("----::::---- Multiple Power Check ($multiplePowerCheck) ----::::----")
            Timber.d("----::::---- Multiple Time Check ($multipleTimeCheck) ----::::----")
            Timber.d("----::::---- Multiple Power Time Check ($multiplePowerTimeCheck) ----::::----")

            /**
             * Power Value
             * Case 1 : Multiple Power | Time Check -- @powerValues
             * Case 2 : Single Power | Time -- @powerValue
             * ToDo - Need to Populate these from the Post State - [Kinslow] Will do it when implementing any device with two energy e.g., idle and preheat
             * */
            val powerValues = listOf(0.0, 0.0)

            /**
             * Single Power Value
             * */
            val preHourlyEnergyUse = hourlyEnergyUsagePre()
            val postHourlyEnergyUse = energyUse()
            val powerValue = (preHourlyEnergyUse - postHourlyEnergyUse) / 24

            Timber.d("----::::---- Power Value ($powerValue) ----::::----")
            Timber.d("----::::---- Power Values ($powerValues) ----::::----")

            /**
             * <<< Energy Cost Saving >>>
             * */
            fun energyCostSaving(): HashMap<String, Double> {

                /**
                 * Energy Cost Savings Calculations - [TOU Based Savings]
                 *
                 * Multiple Power - Time
                 * Single Power - Time
                 * */
                //PowerValue represents the change
                fun electricityCostsCalcMultiplePowerChange(powerValues: List<Double>): Double {
                    var cost = 0.0
                    powerValues.forEach { powerValue ->
                        cost +=
                                preHoursOnPeakPricing * powerValue * peakPrice +
                                preHoursOnPartPeakPricing * powerValue * partPeakPrice +
                                preHoursOnOffPeakPricing * powerValue * offPeakPrice
                    }

                    return cost
                }

                fun electricityCostsCalcMultipleTimeChange(powerValues: List<Double>): Double {
                    var cost = 0.0
                    val deltaPeak = preHoursOnPeakPricing - postHoursOnPeakPricing
                    val deltaPartPeak = preHoursOnPartPeakPricing - postHoursOnPartPeakPricing
                    val deltaOffPeak = preHoursOnOffPeakPricing - postHoursOnOffPeakPricing
                    powerValues.forEach { powerValue ->
                        cost +=
                                deltaPeak * powerValue * peakPrice +
                                deltaPartPeak * powerValue * partPeakPrice +
                                deltaOffPeak * powerValue * offPeakPrice
                    }

                    return cost
                }

                fun electricityCostsCalcPowerChange(powerValue: Double) =
                        electricityCostsCalcMultiplePowerChange(listOf(powerValue))

                fun electricityCostsCalcTimeChange(powerValue: Double) =
                        electricityCostsCalcMultipleTimeChange(listOf(powerValue))

                /**
                 * Energy Cost Savings - Case 1 : TOU Based
                 * */
                //Return whatever is true [Kinslow]
                //If powerchange return powerchange & if timechange return timechange & if powertimechange return powertimechange [Kinslow]
                fun findTimeOfUseCostSavings(): HashMap<String, Double> {
                    val outgoing= hashMapOf<String, Double>()

                    if (multiplePowerCheck) {
                        outgoing["CostSavingMultiplePowerCheck"] = electricityCostsCalcMultiplePowerChange(powerValues)
                    }

                    if (multipleTimeCheck || multiplePowerTimeCheck) {
                        outgoing["CostSavingMultipleTimeOrPowerTimeCheck"] = electricityCostsCalcMultipleTimeChange(powerValues)
                    }

                    if (powerChangeCheck) {
                        outgoing["CostSavingPowerChangeCheck"] = electricityCostsCalcPowerChange(powerValue)
                    }

                    if (timeChangeCheck) {
                        outgoing["CostSavingTimeChangeCheck"] = electricityCostsCalcTimeChange(powerValue)
                    }

                    return outgoing
                }

                /**
                 * Energy Cost Savings - Case 2 : Non TOU Based
                 * */
                //Need to remove blendedEnergyRate for Summer and Winter and energyUse should be seperated too [Kinslow]
                fun summerCostNonTOU() = energyUse * nonTOUEnergyRateSummer * .504
                fun winterCostNonTOU() = energyUse * nonTOUEnergyRateWinter * .496
                fun findNonTimeOfUseCostSavings() = hashMapOf(
                        "CostSavingNonTimeOfUse" to summerCostNonTOU() + winterCostNonTOU())

                /**
                 * Energy Cost Savings - Case 3 : Gas Based
                 * */
                fun gasCostSavings() =
                        (energyUse / 99976.1) * ((winterRate + summerRate)) / 2

                fun findGasCostSavings() = hashMapOf(
                        "CostSavingGas" to gasCostSavings()
                )

                /**
                 * Main Block
                 * */
                val matchTimeOfUse = getRateSchedule.matches("^.*TOU$".toRegex())

                fun negate(flag: Boolean) = !flag
                val energyCostSavings: HashMap<String, Double>
                energyCostSavings = when {

                    matchTimeOfUse                  -> findTimeOfUseCostSavings()
                    negate(matchTimeOfUse)          -> findNonTimeOfUseCostSavings()
                    checkForGas                     -> findGasCostSavings()
                    else -> hashMapOf()

                }

                return energyCostSavings

            }


            /**
             * <<< Demand Cost Saving >>>
             * */
            fun demandCostSaving(): Double {

                fun demandCostSavingsYearCalc(power: Double) = blendedDemandRate * power * 12

                var demandCostSavings = 0.0
                demandCostSavings = when {
                    powerValue > 0 && powerValues.count() == 0 -> demandCostSavingsYearCalc(powerValue)
                    powerValue <= 0 && powerValues.count() > 0 -> demandCostSavingsYearCalc(powerValues[0])
                    else -> 0.0
                }

                return demandCostSavings

            }


            /**
             * <<< Implementation Cost >>>
             * */
            fun implementationCost() = (materialCost + laborCost) - incentives


            /**
             * <<< Total Cost Saved >>>
             * */
            fun aggregateEnergyCostSavings(): Double {

                val savings = energyCostSaving()
                var aggregate = 0.0

                /**
                 * TOU Based Savings
                 * */
                if (savings.containsKey("CostSavingMultiplePowerCheck")) {
                    aggregate += savings["CostSavingMultiplePowerCheck"]!!
                }
                if (savings.containsKey("CostSavingMultipleTimeOrPowerTimeCheck")) {
                    aggregate += savings["CostSavingMultipleTimeOrPowerTimeCheck"]!!
                }
                if (savings.containsKey("CostSavingPowerChangeCheck")) {
                    aggregate += savings["CostSavingPowerChangeCheck"]!!
                }
                if (savings.containsKey("CostSavingTimeChangeCheck")) {
                    aggregate += savings["CostSavingTimeChangeCheck"]!!
                }

                /**
                 * Non TOU Based Saving*/
                if (savings.containsKey("CostSavingNonTimeOfUse")) {
                    aggregate += savings["CostSavingNonTimeOfUse"]!!
                }

                /**
                 * Gas Based Savings
                 * */
                if (savings.containsKey("CostSavingGas")) {
                    aggregate += savings["CostSavingGas"]!!
                }

                return aggregate
            }

            fun totalCostSaved() = aggregateEnergyCostSavings() + maintenanceCostSavings + otherEquipmentSavings + demandCostSaving()


            /**
             * <<< Payback Period - Months >>>
             * */
            fun paybackPeriodMonths() = (implementationCost() / totalCostSaved()) / 12


            /**
             * <<< Payback Period - Years >>>
             * */
            fun paybackPeriodYears() = (implementationCost() / totalCostSaved())

            /**
             * Preparing the Data-Holder to Store the Outgoing Data
             * */
            val energyCostSavingHeader = listOf("__costSavingMultiplePowerCheck",

                    "__costSavingMultipleTimeOrPowerTimeCheck", "__costSavingPowerChangeCheck",
                    "__costSavingTimeChangeCheck", "__costSavingNonTimeOfUse", "__costSavingGas",

                    "__demand_cost_saving", "__implementation_cost", "__total_cost_saved",
                    "__payback_period_months", "__payback_period_years")

            fun initDataHolder(): DataHolder {
                val dataHolderPostState = DataHolder()
                dataHolderPostState.header?.addAll(energyCostSavingHeader)

                dataHolderPostState.computable = computable
                dataHolderPostState.fileName = "${Date().time}_energy_cost_savings.csv"

                return dataHolderPostState
            }

            val dataHolder = initDataHolder()

            /**
             * Prepare the Outgoing Rows
             * */
            dataHolder.rows?.add(mapOf(

                    "__costSavingMultiplePowerCheck" to if (energyCostSaving().containsKey("CostSavingMultiplePowerCheck")) {
                        energyCostSaving()["CostSavingMultiplePowerCheck"]!!.toString()
                    } else {
                        ""
                    },
                    "__costSavingMultipleTimeOrPowerTimeCheck" to if (energyCostSaving().containsKey("CostSavingMultipleTimeOrPowerTimeCheck")) {
                        energyCostSaving()["CostSavingMultipleTimeOrPowerTimeCheck"]!!.toString()
                    } else {
                        ""
                    },
                    "__costSavingPowerChangeCheck" to if (energyCostSaving().containsKey("CostSavingPowerChangeCheck")) {
                        energyCostSaving()["CostSavingPowerChangeCheck"]!!.toString()
                    } else {
                        ""
                    },
                    "__costSavingTimeChangeCheck" to if (energyCostSaving().containsKey("CostSavingTimeChangeCheck")) {
                        energyCostSaving()["CostSavingTimeChangeCheck"]!!.toString()
                    } else {
                        ""
                    },

                    "__costSavingNonTimeOfUse" to if (energyCostSaving().containsKey("CostSavingNonTimeOfUse")) {
                        energyCostSaving()["CostSavingNonTimeOfUse"]!!.toString()
                    } else {
                        ""
                    },
                    "__costSavingGas" to if (energyCostSaving().containsKey("CostSavingGas")) {
                        energyCostSaving()["CostSavingGas"]!!.toString()
                    } else {
                        ""
                    },

                    "__demand_cost_saving" to demandCostSaving().toString(),
                    "__implementation_cost" to implementationCost().toString(),
                    "__total_cost_saved" to totalCostSaved().toString(),
                    "__payback_period_months" to paybackPeriodMonths().toString(),
                    "__payback_period_years" to paybackPeriodYears().toString()
            ))

            return dataHolder
        }

    }
}
