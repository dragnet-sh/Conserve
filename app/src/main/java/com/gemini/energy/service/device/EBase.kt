package com.gemini.energy.service.device

import android.util.Log
import com.gemini.energy.domain.Schedulers
import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.domain.entity.Feature
import com.gemini.energy.internal.AppSchedulers
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

abstract class EBase(private val computable: Computable<*>) {

    lateinit var schedulers: Schedulers

    var preAudit: Map<String?, Feature>? = mapOf()
    var featureData: Map<String?, Feature>? = mapOf()

    fun initialize() {
        schedulers = AppSchedulers()
        featureData = computable.mappedFeatureAuditScope()
        preAudit = computable.mappedFeaturePreAudit()
    }


    fun compute(extra: (param: String) -> Unit): Observable<Computable<*>> {

        return Observable.create<Computable<*>> {

            if (efficientLookup()) {
                starValidator(queryEnergyStar())
                        .observeOn(schedulers.observeOn)
                        .subscribeOn(schedulers.subscribeOn)
                        .subscribe { starValidation ->

                            if (starValidation) {

                                // 1. Fetch Efficient Devices
                                // 2. Start Doing your Calculations -- Where ??

                                efficientAlternative(queryFilter())
                                        .observeOn(schedulers.observeOn)
                                        .subscribeOn(schedulers.subscribeOn)
                                        .subscribe { response ->
                                            Log.d(TAG, response.count().toString())
                                            Log.d(TAG, response.toString())

                                            // computable.energyEquivalent = ????

                                            extra("Hola!! from Efficient Alternative !!")

                                            it.onNext(computable)
                                            it.onComplete()
                                        }


                            } else { extra("Already Efficient !!") }

                        }
            } else {
                extra("Hola!! from Negative Efficiency Lookup !!")
                it.onNext(computable)
                it.onComplete()
            }

        }

    }


    companion object {
        private const val TAG = "EBase"
    }

    abstract fun queryFilter(): String
    abstract fun efficientLookup(): Boolean

    fun queryEnergyStar(): String {
        val json = JSONObject()
        json.put("data.company_name", featureData?.get("company")?.valueString ?: "")
        json.put("data.model_number", featureData?.get("model_number")?.valueString ?: "")

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

    fun efficientAlternative(query: String): Observable<JsonArray> {
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