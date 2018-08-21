package com.gemini.energy.service

import com.google.gson.JsonObject
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class ParseAPI {

    interface ParseAPIService {
        @GET("classes/PlugLoad")
        fun fetchPlugload(@Query("where") where: String): Single<JsonObject>

        @GET("classes/LaborCost")
        fun fetchLaborCost(@Query("where") where: String): Single<JsonObject>

        @GET("classes/HVAC")
        fun fetchHVAC(@Query("where") where: String): Single<JsonObject>
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

