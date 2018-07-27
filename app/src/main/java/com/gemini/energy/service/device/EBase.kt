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

    var preAudit: Map<String, Any> = mapOf()
    var featureData: Map<String, Any> = mapOf()
    var electricRateStructure: String = RATE

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
        base.outgoingRows.computable = computable
        base.outgoingRows.dataHolder = mutableListOf()

    }

    private fun thread() = Thread.currentThread().name
    private fun identifier() = "${computable.auditScopeType} - ${computable.auditScopeSubType}"

    fun compute(extra: (param: String) -> Unit): Observable<Computable<*>> {

        return Observable.create<Computable<*>> {

            initialize()

            // #### Step 1: Pre-State Energy Calculation ####

            Log.d(TAG, ":::::::: Pre-State Energy Calculation - (${thread()}) ::::::::")
            val dataHolderPreState = DataHolder()
            dataHolderPreState.header?.addAll(featureDataFields())
            dataHolderPreState.computable = computable
            dataHolderPreState.fileName = "${Date()}_pre_state.csv"

            val preRow = mutableMapOf<String, String>()
            featureDataFields().forEach { field ->
                val value = featureData[field]
                preRow[field] = when (value) {
                    is String       -> value
                    is Int          -> value.toString()
                    is Double       -> value.toString()
                    else            -> ""
                }
            }

            val dailyEnergyUsed = featureData["Daily Energy Used (kWh)"]
            dailyEnergyUsed?.let {
                val cost = cost(dailyEnergyUsed)
                dataHolderPreState.header?.add("__electric_cost")
                preRow["__electric_cost"] = cost.toString()
            }

            dataHolderPreState.rows?.add(preRow)
            outgoingRows.dataHolder.add(dataHolderPreState)

            Log.d(TAG, "## Data Holder - PRE STATE - (${thread()}) ##" )
            Log.d(TAG, dataHolderPreState.header.toString())
            Log.d(TAG, dataHolderPreState.rows.toString())

            // #### Step 2: Post-State Energy Calculation ####

            Log.d(TAG, ":::::::: Post-State Energy Calculation - (${thread()}) ::::::::")
            if (efficientLookup()) {
                starValidator(queryEnergyStar())
                        .observeOn(schedulers.subscribeOn)
                        .subscribe { starValidationFail ->

                            if (starValidationFail) {

                                Log.d(TAG, "### Debug - Query Filter - (${thread()})###")
                                Log.d(TAG, queryFilter())

                                efficientAlternative(queryFilter())
                                        .observeOn(schedulers.subscribeOn)
                                        .subscribe { response ->

                                            Log.d(TAG, "### Efficient Alternate Count - [${response.count()}] - ###")
                                            val jsonElements = response.map { it.asJsonObject.get("data") }
                                            computable.efficientAlternative = jsonElements

                                            /**
                                             * Cost Function to be Called - for each of the Data Row!!
                                             * */
                                            val dataHolderPostState = DataHolder()
                                            dataHolderPostState.header = postStateFields()
                                            dataHolderPostState.computable = computable
                                            dataHolderPostState.fileName = "${Date()}_post_state.csv"

                                            jsonElements.forEach { element ->
                                                val postRow = mutableMapOf<String, String>()
                                                postStateFields().forEach { key ->
                                                    val value = element.asJsonObject.get(key)
                                                    postRow[key] = value.asString
                                                }

                                                val postDailyEnergyUsed = element.asJsonObject.get("daily_energy_use").asDouble
                                                val cost = cost(postDailyEnergyUsed)
                                                dataHolderPostState.header?.add("__electric_cost")
                                                postRow["__electric_cost"] = cost.toString()

                                                dataHolderPostState.rows?.add(postRow)
                                            }

                                            outgoingRows.dataHolder.add(dataHolderPostState)

                                            Log.d(TAG, "## Data Holder - POST STATE  - (${thread()}) ##")
                                            Log.d(TAG, dataHolderPostState.header.toString())
                                            Log.d(TAG, dataHolderPostState.rows.toString())

                                            it.onNext(computable)
                                            extra("OnComplete :: Efficient Data Row Processing - Done")
                                            it.onComplete()

                                        }

                            } else {
                                computable.isEnergyStar = true
                                it.onNext(computable)
                                extra("OnComplete :: Energy Star - true")
                                it.onComplete()
                            }

                        }
            } else {
                it.onNext(computable)
                extra("OnComplete :: Energy Efficiency Lookup - false")
                it.onComplete()
            }

        }

    }

    companion object {
        private const val TAG = "EBase"
        private const val RATE = "A-1 TOU"
    }

    abstract fun queryFilter(): String
    abstract fun efficientLookup(): Boolean

    abstract fun preAuditFields(): MutableList<String>
    abstract fun featureDataFields(): MutableList<String>
    abstract fun preStateFields(): MutableList<String>
    abstract fun postStateFields(): MutableList<String>
    abstract fun computedFields(): MutableList<String>

    abstract fun cost(vararg params: Any): Double

    private fun queryEnergyStar(): String {
        val json = JSONObject()
        json.put("data.company_name", featureData["Company"])
        json.put("data.model_number", featureData["Model Number"])
        return json.toString()
    }

    private fun starValidator(query: String): Observable<Boolean> {
        return Observable.create<Boolean> {
            parseAPIService.fetchPlugload(query)
                    .subscribeOn(schedulers.subscribeOn)
                    .observeOn(schedulers.observeOn)
                    .subscribe { response ->
                        val rows = response.getAsJsonArray("results")
                        Log.d(TAG, "Star Validator Count - ${rows.count()}")
                        it.onNext(rows.count() == 0)
                        it.onComplete()
                    }
        }
    }

    private fun efficientAlternative(query: String): Observable<JsonArray> {
        return Observable.create<JsonArray> {
            parseAPIService.fetchPlugload(query)
                    .subscribeOn(schedulers.subscribeOn)
                    .observeOn(schedulers.observeOn)
                    .subscribe { response ->
                        val rows = response.getAsJsonArray("results")
                        it.onNext(rows)
                        it.onComplete()
                    }
        }
    }

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

    fun costElectricity(energyUsed: Double, usage: EnergyUsage, utility: EnergyUtility): Double {
        val regex = "^.*TOU$".toRegex()
        val usageByPeak = usage.mappedPeakHourYearly()
        val usageByYear = usage.yearly()

        if (electricRateStructure.matches(regex)) {

            var summer = usageByPeak[ERateKey.SummerOn]!! * energyUsed * utility.structure[ERateKey.SummerOn.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerPart]!! * energyUsed * utility.structure[ERateKey.SummerPart.value]!![0].toDouble()
            summer += usageByPeak[ERateKey.SummerOff]!! * energyUsed * utility.structure[ERateKey.SummerOff.value]!![0].toDouble()

            var winter = usageByPeak[ERateKey.WinterPart]!! * energyUsed * utility.structure[ERateKey.WinterPart.value]!![0].toDouble()
            winter += usageByPeak[ERateKey.WinterOff]!! * energyUsed * utility.structure[ERateKey.WinterOff.value]!![0].toDouble()

            return (summer + winter)

        } else {

            val summer = usageByYear * energyUsed * utility.structure[ERateKey.SummerNone.value]!![0].toDouble()
            val winter = usageByYear * energyUsed * utility.structure[ERateKey.WinterNone.value]!![0].toDouble()

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
            fun fetchPlugload(@Query("where") where: String): Observable<JsonObject>
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