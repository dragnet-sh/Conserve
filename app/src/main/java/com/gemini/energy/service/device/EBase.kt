package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.internal.AppSchedulers
import com.gemini.energy.presentation.util.EDay
import com.gemini.energy.presentation.util.ERateKey
import com.gemini.energy.service.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

abstract class EBase(private val computable: Computable<*>,
                     private val energyUtility: EnergyUtility,
                     val operatingHours: EnergyUsage,
                     val outgoingRows: OutgoingRows) {

    lateinit var schedulers: Schedulers
    lateinit var gasUtility: EnergyUtility
    lateinit var electricityUtility: EnergyUtility
    lateinit var preconditions: Preconditions

    var preAudit: Map<String, Any> = mapOf()
    var featureData: Map<String, Any> = mapOf()
    private var electricRateStructure: String = RATE

    private val energyUsageBusiness = EnergyUsage()
    private val energyUsageSpecific = EnergyUsage()

    private fun initialize() {
        val base = this

        Log.d(TAG, "<< COMPUTE :: ${identifier()} >> [Start] - (${thread()})")
        Log.d(TAG, computable.toString())

        base.schedulers = AppSchedulers()
        base.featureData = computable.mappedFeatureAuditScope()
        base.preAudit = computable.mappedFeaturePreAudit()

        base.gasUtility = energyUtility.initUtility(Gas()).build()
        base.electricRateStructure = preAudit["Electric Rate Structure"] as String
        base.electricityUtility = energyUtility.initUtility(
                Electricity(electricRateStructure)).build()

        base.operatingHours.initUsage(mappedUsageHours()).build()

        base.energyUsageBusiness.initUsage(mappedBusinessHours()).build()
        base.energyUsageSpecific.initUsage(mappedSpecificHours()).build()

        base.outgoingRows.computable = computable
        base.outgoingRows.dataHolder = mutableListOf()
        base.preconditions = Preconditions()

    }

    private fun thread() = Thread.currentThread().name
    private fun identifier() = "${computable.auditScopeType} - ${computable.auditScopeSubType}"


    /**
     * Collect the Various Energy Calculation - Concat them
     * Write the result to the CSV - Emit back Computable
     * */
    fun compute(extra: (param: String) -> Unit): Observable<Computable<*>> {
        initialize()
        validatePreConditions()

        class Mapper : Function<DataHolder, Computable<*>> {
            override fun apply(dataHolder: DataHolder): Computable<*> {
                synchronized(outgoingRows.dataHolder) {
                    if (dataHolder.path.isNotEmpty()) {
                        outgoingRows.dataHolder.add(dataHolder)
                    }
                }
                computable.outgoingRows = outgoingRows
                return computable
            }
        }

        return Observable.concat(calculateEnergyPreState(extra), calculateEnergyPostState(extra),
                calculateEnergySavings(extra))
                .map(Mapper()).doOnComplete {
                    Log.d(TAG, "$$$$$$$ SUPER.COMPUTE.CONCAT.COMPLETE $$$$$$$")
//                    outgoingRows.save()
                }

    }

    private fun validatePreConditions() = preconditions.validate()


    private fun calculateEnergySavings(extra: (param: String) -> Unit): Observable<DataHolder> {

        class Mapper : Function<DataHolder, DataHolder> {
            override fun apply(dataHolder: DataHolder): DataHolder {
                Log.d(TAG, "%^%^% Energy Savings Calculation - (${thread()}) %^%^%")
                Log.d(TAG, "Energy Post State [Item Count] : (${computable.energyPostState?.count()})")

                val preRunHours = energyUsageSpecific.yearly()
                val postRunHours = energyUsageBusiness.yearly()

                val preHourlyEnergyUse = featureData["Daily Energy Used (kWh)"] as Double
                val postHourlyEnergyUse = 0.0 //ToDo - Get this from the Post State Database

                val prePower = preHourlyEnergyUse / 24
                val postPower = postHourlyEnergyUse / 24

                fun energyPowerChange() = preRunHours * (prePower - postPower)
                fun energyTimeChange() = (preRunHours - postRunHours) * prePower
                fun energy() = (preRunHours - postRunHours) * (prePower - postPower)

                fun checkPowerChange() = energyPowerChange() != 0.0 && energyTimeChange() == 0.0
                fun checkTimeChange() = energyPowerChange() == 0.0 && energyTimeChange() != 0.0
                fun checkPowerTimeChange() = energyPowerChange() != 0.0 && energyTimeChange() != 0.0

                Log.d(TAG, "Energy Power Change : (${energyPowerChange()})")
                Log.d(TAG, "Energy Time Change : (${energyTimeChange()})")
                Log.d(TAG, "Energy : (${energy()})")

                Log.d(TAG, "Check Power Change : (${checkPowerChange()})")
                Log.d(TAG, "Check Time Change : (${checkTimeChange()})")
                Log.d(TAG, "Check Power Time Change : (${checkPowerTimeChange()})")

                val energySavings = when {
                    checkPowerChange()          -> energyPowerChange()
                    checkTimeChange()           -> energyTimeChange()
                    checkPowerTimeChange()      -> energy()
                    else                        -> 0.0
                }

                return dataHolder

            }
        }

        return Observable.just(DataHolder())
                .map(Mapper())
    }


    private fun calculateCostSavings(extra: (param: String) -> Unit): Observable<DataHolder> {
        return Observable.just(DataHolder())
    }

    /**
     * Pre State - Energy Calculation
     * Gives an Observable with the Data Holder
     * */
    private fun calculateEnergyPreState(extra: (param: String) -> Unit): Observable<DataHolder> {

        fun initDataHolder(): DataHolder {
            val dataHolderPreState = DataHolder()

            dataHolderPreState.header?.addAll(featureDataFields())
            dataHolderPreState.computable = computable
            dataHolderPreState.fileName = "${Date().time}_pre_state.csv"

            return dataHolderPreState
        }

        Log.d(TAG, "%^%^% Pre-State Energy Calculation - (${thread()}) %^%^%")
        val dataHolderPreState = initDataHolder()
        val preRow = mutableMapOf<String, String>()
        featureDataFields().forEach { field ->
            preRow[field] = if (featureData.containsKey(field)) featureData[field].toString() else ""
        }

        val dailyEnergyUsed = featureData["Daily Energy Used (kWh)"]
        dailyEnergyUsed?.let {
            val cost = cost(it)
            dataHolderPreState.header?.add("__electric_cost")
            preRow["__electric_cost"] = cost.toString()
        }

        dataHolderPreState.rows?.add(preRow)
        computable.energyPreState = preRow

        Log.d(TAG, "## Data Holder - PRE STATE - (${thread()}) ##")
        Log.d(TAG, dataHolderPreState.toString())

        return Observable.just(dataHolderPreState)

    }


    /**
     * Post State - Energy Calculation
     * Gives an Observable with the Data Holder
     * */
    private fun calculateEnergyPostState(extra: (param: String) -> Unit): Observable<DataHolder> {

        class Mapper : Function<JsonArray, DataHolder> {

            override fun apply(response: JsonArray): DataHolder {

                Log.d(TAG, "%^%^% Post-State Energy Calculation - (${thread()}) %^%^%")
                Log.d(TAG, "### Efficient Alternate Count - [${response.count()}] - ###")

                val jsonElements = response.map { it.asJsonObject.get("data") }
                computable.energyPostState = jsonElements

                val dataHolderPostState = initDataHolder()

                jsonElements.forEach { element ->
                    val postRow = mutableMapOf<String, String>()
                    postStateFields().forEach { key ->
                        val value = element.asJsonObject.get(key)
                        postRow[key] = value.asString
                    }

                    val postDailyEnergyUsed = element.asJsonObject.get("daily_energy_use").asDouble
                    val cost = cost(postDailyEnergyUsed)
                    postRow["__electric_cost"] = cost.toString()
                    dataHolderPostState.rows?.add(postRow)
                }

                Log.d(TAG, "## Data Holder - POST STATE  - (${thread()}) ##")
                Log.d(TAG, dataHolderPostState.toString())

                return dataHolderPostState
            }

            private fun initDataHolder(): DataHolder {
                val dataHolderPostState = DataHolder()
                dataHolderPostState.header = postStateFields()
                dataHolderPostState.header?.add("__electric_cost")

                dataHolderPostState.computable = computable
                dataHolderPostState.fileName = "${Date().time}_post_state.csv"

                return dataHolderPostState
            }

        }

        return starValidator(queryEnergyStar())
                .flatMap { if (it && efficientLookup()) {
                    efficientAlternative(queryEfficientFilter()).map(Mapper())
                } else { Observable.just(DataHolder())} }
    }

    companion object {
        private const val TAG = "EBase"
        private const val RATE = "A-1 TOU"
    }

    abstract fun queryEfficientFilter(): String
    abstract fun efficientLookup(): Boolean

    abstract fun preAuditFields(): MutableList<String>
    abstract fun featureDataFields(): MutableList<String>
    abstract fun preStateFields(): MutableList<String>
    abstract fun postStateFields(): MutableList<String>
    abstract fun computedFields(): MutableList<String>

    abstract fun cost(vararg params: Any): Double

    private fun queryEnergyStar() = JSONObject()
            .put("data.company_name", featureData["Company"])
            .put("data.model_number", featureData["Model Number"])
            .toString()

    private fun queryLaborCost() = JSONObject()
                .put("data.zipcode", preAudit["ZipCode"])
                .put("data.profession", preAudit["Profession"])
                .toString()

    private fun starValidator(query: String): Observable<Boolean> {
        return parseAPIService.fetchPlugload(query)
                .map { it.getAsJsonArray("results").count() == 0 }
                .toObservable()
    }

    private fun efficientAlternative(query: String): Observable<JsonArray> {
        return parseAPIService.fetchPlugload(query)
                .map { it.getAsJsonArray("results") }
                .toObservable()
    }

    private fun laborCost(query: String): Observable<JsonArray> {
        return parseAPIService.fetchLaborCost(query)
                .map { it.getAsJsonArray("results") }
                .toObservable()
    }


    /**
     * Get the Weekly Hours Map Ready
     * This should decide where to look - [PreAudit or Individual]
     * */
    private fun mappedUsageHours(): Map<EDay, String?> {
        val usage = mutableListOf<String>()

        for (eDay in EDay.values()) {
            if (preAudit.containsKey(eDay.value)) {
                usage.add(preAudit[eDay.value] as String)
            } else {usage.add("")}
        }

        Log.d(TAG, usage.toString())

        return EDay.values().associateBy({ it }, {
            usage[EDay.values().indexOf(it)]
        })
    }

    private fun mappedBusinessHours(): Map<EDay, String?> {
        val usage = mutableListOf<String>()

        for (eDay in EDay.values()) {
            if (preAudit.containsKey(eDay.value)) {
                usage.add(preAudit[eDay.value] as String)
            } else {usage.add("")}
        }

        Log.d(TAG, usage.toString())

        return EDay.values().associateBy({ it }, {
            usage[EDay.values().indexOf(it)]
        })
    }

    private fun mappedSpecificHours(): Map<EDay, String?> {
        val usage = mutableListOf<String>()

        for (eDay in EDay.values()) {
            if (featureData.containsKey(eDay.value)) {
                usage.add(featureData[eDay.value] as String)
            } else {usage.add("")}
        }

        Log.d(TAG, usage.toString())

        return EDay.values().associateBy({ it }, {
            usage[EDay.values().indexOf(it)]
        })
    }

    //ToDo - ReWrite this later !!
    fun costElectricity(powerUsed: Double, usage: EnergyUsage, utility: EnergyUtility): Double {
        val regex = "^.*TOU$".toRegex()
        val usageByPeak = usage.mappedPeakHourYearly()
        val usageByYear = usage.yearly() //ToDo - Figure out a way to adjust the Usage Hours by negating the Vacation Days

        if (electricRateStructure.matches(regex)) {

            var summer = usageByPeak[ERateKey.SummerOn]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOn.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerPart]!! * .504 * powerUsed * utility.structure[ERateKey.SummerPart.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerOff]!! * .504 * powerUsed * utility.structure[ERateKey.SummerOff.value]!![0].toDouble()

            var winter = usageByPeak[ERateKey.WinterPart]!! * .496 * powerUsed * utility.structure[ERateKey.WinterPart.value]!![0].toDouble()
            winter += usageByPeak[ERateKey.WinterOff]!! * .496 * powerUsed * utility.structure[ERateKey.WinterOff.value]!![0].toDouble()

            return (summer + winter)

        } else {

            val summer = usageByYear * .504 * powerUsed * utility.structure[ERateKey.SummerNone.value]!![0].toDouble()
            val winter = usageByYear * .496 * powerUsed * utility.structure[ERateKey.WinterNone.value]!![0].toDouble()

            return (summer + winter)

        }

    }

    /**
     * Parse API Service ToDo: Move this to the Network Layer !!
     * */
    private val parseAPIService by lazy { ParseAPI.create() }

    class ParseAPI {

        interface ParseAPIService {
            @GET("classes/PlugLoad")
            fun fetchPlugload(@Query("where") where: String): Single<JsonObject>

            @GET("classes/LaborCost")
            fun fetchLaborCost(@Query("where") where: String): Single<JsonObject>
        }

        companion object {
            private const val applicationId = "47f916f7005d19ddd78a6be6b4bdba3ca49615a0"
            private const val masterKey = "NLI214vDqkoFTJSTtIE2xLqMme6Evd0kA1BbJ20S"

            private val okHttpClient = OkHttpClient()
                    .newBuilder()
                    .addInterceptor {
                        val original = it.request()
                        val request = original.newBuilder()
                                .header("User-Agent", "OkHttp Headers.java")
                                .addHeader("Content-Type", "application/json")
                                .addHeader("X-Parse-Application-Id", applicationId)
                                .addHeader("X-Parse-REST-API-Key", masterKey)
                                .build()
                        it.proceed(request)
                    }

            fun create(): ParseAPIService {
                val retrofit = Retrofit.Builder()
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl("http://ec2-18-220-200-115.us-east-2.compute.amazonaws.com:80/parse/")
                        .client(okHttpClient.build())
                        .build()

                return retrofit.create(ParseAPIService::class.java)
            }

        }
    }
}