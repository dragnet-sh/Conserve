package com.gemini.energy.service.sync

import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.data.local.model.FeatureLocalModel
import com.gemini.energy.data.local.model.TypeLocalModel
import com.gemini.energy.data.local.model.ZoneLocalModel
import com.gemini.energy.service.ParseAPI
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.merge
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import timber.log.Timber
import java.lang.StringBuilder
import java.util.*

class Syncer(private val parseAPIService: ParseAPI.ParseAPIService,
             private val col: Collection,
             private val mListener: Listener? = null) {

    private val auditTaskHolder: MutableList<Observable<JsonObject>> = mutableListOf()
    private val featureTaskHolder: MutableList<Observable<JsonObject>> = mutableListOf()

    private val auditList: MutableList<AuditLocalModel> = mutableListOf()

    fun sync() {

        fun prepare() {
            val audit = col.audit
            audit.forEach {
                auditList.add(it)
                buildFeature(it.auditId).forEach {
                    featureTaskHolder.add(parseAPIService.saveFeature(it).toObservable())
                }

                auditTaskHolder.add(parseAPIService.saveAudit(buildAudit(it)).toObservable())
            }
        }

        Timber.d("<< SYNC >>")
        Timber.d(col.audit.toString())

        download()

    }

    private fun download() {
        val query = JSONObject().put("usn", 1)

        fun splitFields(fields: String): List<String> {
            return fields.split("\\x1f".toRegex())
        }

        parseAPIService.fetchFeature(query.toString()).toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val rFeature = it.getAsJsonArray("results")
                    val models: MutableList<FeatureLocalModel> = mutableListOf()

                    rFeature.forEach {

                        val auditId = it.asJsonObject.get("auditId").asString
                        val zoneId = it.asJsonObject.get("zoneId").asString
                        val typeId = it.asJsonObject.get("typeId").asString
                        val belongsTo = it.asJsonObject.get("belongsTo").asString

                        val usn = it.asJsonObject.get("usn").asInt
                        val mod = it.asJsonObject.get("mod").asString

                        val values = splitFields(it.asJsonObject.get("values").asString)
                        val dataTypes = splitFields(it.asJsonObject.get("dataType").asString)
                        val fields = splitFields(it.asJsonObject.get("fields").asString)
                        val formIds = splitFields(it.asJsonObject.get("formId").asString)
                        val id = splitFields(it.asJsonObject.get("id").asString)

                        // *** Load Feature by Type to the Local DB
                        if (typeId != "null") {
                            if (col.featureType.containsKey(typeId.toInt())) {
                                val localFeature = col.featureType[typeId.toInt()]
                                val lMod = localFeature?.map { it.updatedAt.time }?.max()

                                Timber.d("Local Updated At (Feature - Type)")
                                Timber.d(lMod.toString())

                                Timber.d("Remote Last Modified At (Feature - Type)")
                                Timber.d(mod)

                            } else {
                                Timber.d("---------- Feature Type Fresh Entry -----------")
                                for (i in 0 until formIds.count() - 1) {
                                    val feature = FeatureLocalModel(id[i].toInt(), formIds[i].toInt(), belongsTo, dataTypes[i],
                                            null, zoneId.toInt(), typeId.toInt(), fields[i], values[i], null, null,
                                            Date(), Date())
                                    models.add(feature)
                                }
                            }
                        }

                        if (typeId == "null") {
                            if (col.featureAudit.containsKey(auditId.toInt())) {
                                val localFeature = col.featureAudit[auditId.toInt()]
                                val lMod = localFeature?.map { it.updatedAt.time }?.max()

                                Timber.d("Local Updated At (Feature - Audit)")
                                Timber.d(lMod.toString())

                                Timber.d("Remote Last Modified At (Feature - Audit)")
                                Timber.d(mod)

                            } else {
                                Timber.d("---------- Feature Audit Fresh Entry -----------")
                                for (i in 0 until formIds.count() - 1) {
                                    val feature = FeatureLocalModel(id[i].toInt(), formIds[i].toInt(), belongsTo, dataTypes[i],
                                        auditId.toInt(), null, null, fields[i], values[i], null, null,
                                        Date(), Date())
                                    models.add(feature)
                                }
                            }
                        }
                    }

                    col.db?.featureDao()?.insert(models)

                }, { it.printStackTrace() }, { Timber.d("-- Feature Fetch Complete --"); mListener?.onPostExecute()})


        parseAPIService.fetchAudit(query.toString()).toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    val rAudit = it.getAsJsonArray("results")
                    rAudit.forEach {

                        var auditId = ""
                        var name = ""
                        var usn = -99
                        var zone: JsonObject? = null
                        var type: JsonObject? = null

                        try {
                            auditId = it.asJsonObject.get("auditId").asString
                            name = it.asJsonObject.get("name").asString
                            usn = it.asJsonObject.get("usn").asInt

                            zone = it.asJsonObject.get("zone").asJsonObject
                            type = it.asJsonObject.get("type").asJsonObject
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        Timber.d(zone.toString())
                        Timber.d(type.toString())

                        // 1. Audit Entry - To local DB
                        Timber.d("<<<< AUDIT >>>>")
                        val localAuditId = auditList.map { it.auditId }
                        if (!localAuditId.contains(auditId.toInt())) {
                            val model = AuditLocalModel(auditId.toInt(), name, usn, Date(), Date())
                            col.db?.auditDao()?.insert(model)
                        }

                        // 2. Zone Entry - To local DB
                        Timber.d("<<<< ZONE >>>>")
                        zone?.keySet()?.forEach {
                            val inner = zone.get(it).asJsonObject
                            val iUsn = inner.get("usn").asInt
                            val iName = inner.get("name").asString
                            val iMod = inner.get("mod").asString
                            val iId = inner.get("id").asInt

                            if (col.zone.containsKey(auditId.toInt())) {
                                val zones = col.zone[auditId.toInt()]
                                zones?.let {
                                    val ids = it.map { it.zoneId }
                                    if (ids.contains(iId)) {
                                        val localZone = it[ids.indexOf(iId)]

                                        Timber.d("Local Updated At")
                                        Timber.d(localZone.updatedAt.time.toString())

                                        Timber.d("Remote Last Modified At")
                                        Timber.d(iMod)
                                    } else {
                                        Timber.d("-------- Zone Fresh Entry ----------")
                                        val model = ZoneLocalModel(iId, iName, "Sample Zone", auditId.toInt(), Date(), Date())
                                        col.db?.zoneDao()?.insert(model)
                                    }
                                }
                            } else {
                                Timber.d("-------- Zone Fresh Entry ----------")
                                val model = ZoneLocalModel(iId, iName, "Sample Zone", auditId.toInt(), Date(), Date())
                                col.db?.zoneDao()?.insert(model)
                            }
                        }

                        // 3. Type Entry - To Local DB
                        Timber.d("<<<< TYPE >>>>")
                        type?.keySet()?.forEach {
                            val inner = type.get(it).asJsonObject
                            val iUsn = inner.get("usn").asInt
                            val iName = inner.get("name").asString
                            val iType = inner.get("type").asString
                            val iSubType = inner.get("subtype").asString
                            val iMod = inner.get("mod").asString
                            val iId = inner.get("id").asInt
                            val iZoneId = inner.get("zoneId").asInt

                            if (col.type.containsKey(auditId.toInt())) {
                                val types = col.type[auditId.toInt()]
                                types?.let {
                                    val ids = it.map { it.auditParentId }
                                    if (ids.contains(iId)) {
                                        val localType = it[ids.indexOf(iId)]

                                        Timber.d("Local Updated At")
                                        Timber.d(localType.updatedAt?.time.toString())

                                        Timber.d("Remote Last Modified At")
                                        Timber.d(iMod)
                                    } else {
                                        Timber.d("------------ Type Fresh Entry --------------------")
                                        val model = TypeLocalModel(iId, iName, iType, iSubType, iZoneId, auditId.toInt(), Date(), Date())
                                        col.db?.auditScopeDao()?.insert(model)
                                    }
                                }
                            } else {
                                Timber.d("------------ Type Fresh Entry --------------------")
                                val model = TypeLocalModel(iId, iName, iType, iSubType, iZoneId, auditId.toInt(), Date(), Date())
                                col.db?.auditScopeDao()?.insert(model)
                            }
                        }

                    }

                }, { it.printStackTrace() }, {
                    Timber.d("-- AUDIT DOWNLOAD COMPLETE --"); mListener?.onPostExecute() })

    }

    private fun uploadAudit() {
        auditTaskHolder.merge()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    Timber.d(it.toString())


                }, { it.printStackTrace() }, { Timber.d("Complete - Audit Upload") })
    }
    private fun uploadFeature() {
        featureTaskHolder.merge()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ Timber.d(it.toString()) }, { it.printStackTrace() },
                        { Timber.d("Complete - Feature Upload") })
    }

    /**
     * 1. Audit
     * */
    private fun buildAudit(local: AuditLocalModel): JsonObject {
        val outgoing = JsonObject()
        outgoing.addProperty("auditId", local.auditId.toString())
        outgoing.addProperty("name", local.name)
        outgoing.addProperty("usn", ++local.usn)
        outgoing.addProperty("mod", Date().time.toString())

        outgoing.add("zone", buildZone(local.auditId))
        outgoing.add("type", buildType(local.auditId))

        Timber.d(outgoing.toString())

        return outgoing
    }

    /**
     * 2. Zone
     * */
    private fun buildZone(auditId: Int): JsonObject {

        fun associatedTypes(zoneId: Int?): JsonArray {
            val outgoing = JsonArray()
            if (col.type.containsKey(auditId)) {
                val type = col.type[auditId]
                type?.forEach {
                    if (it.zoneId == zoneId) {
                        outgoing.add(it.auditParentId)
                    }
                }
            }
            return outgoing
        }

        val outgoing = JsonObject()
        if (col.zone.containsKey(auditId)) {
            val zone = col.zone[auditId]
            zone?.let {
                it.forEach {

                    val inner = JsonObject()
                    inner.addProperty("usn", 0)
                    inner.addProperty("name", it.name)
                    inner.add("typeId", associatedTypes(it.zoneId))
                    inner.addProperty("mod", Date().time)
                    inner.addProperty("id", it.zoneId)

                    outgoing.add(it.zoneId.toString(), inner)
                }
            }
        }

        return outgoing
    }

    /**
     * 3. Type
     * */
    private fun buildType(auditId: Int): JsonObject {
        val outgoing = JsonObject()
        if (col.type.containsKey(auditId)) {
            val type = col.type[auditId]
            type?.let {
                it.forEach {

                    val inner = JsonObject()
                    inner.addProperty("usn", 0)
                    inner.addProperty("name", it.name)
                    inner.addProperty("type", it.type)
                    inner.addProperty("subtype", it.subType)
                    inner.addProperty("mod", Date().time)
                    inner.addProperty("id", it.auditParentId)
                    inner.addProperty("zoneId", it.zoneId)

                    outgoing.add(it.auditParentId.toString(), inner)
                }
            }
        }

        return outgoing
    }

    /**
     * 4. Feature
     * */
    private fun buildFeature(auditId: Int): List<JsonObject> {

        fun joinFields(fields: List<String?>): String {
            val result = StringBuilder(128)
            fields.forEach {
                it?.let {
                    result.append(it).append("\u001f")
                }
            }

            return result.toString()
        }

        fun create(auditId: Int, model: List<FeatureLocalModel>): JsonObject {
            val inner = JsonObject()
            inner.addProperty("usn", 0)
            inner.addProperty("auditId", auditId.toString())
            inner.addProperty("zoneId", model[0].zoneId.toString())
            inner.addProperty("typeId", model[0].typeId.toString())
            inner.addProperty("mod", Date().time.toString())
            inner.addProperty("belongsTo", model[0].belongsTo)

            inner.addProperty("id", joinFields(model.map { it.featureId.toString() }))
            inner.addProperty("dataType", joinFields(model.map { it.dataType }))
            inner.addProperty("formId", joinFields(model.map { it.formId.toString() }))
            inner.addProperty("fields", joinFields(model.map { it.key }))
            inner.addProperty("values", joinFields(model.map { it.valueString }))

            return inner
        }

        val outgoing: MutableList<JsonObject> = mutableListOf()

        if (col.typeIdsByAudit.containsKey(auditId)) {
            val typeIds = col.typeIdsByAudit[auditId]
            typeIds?.let {
                it.forEach {
                    it?.let {
                        if (col.featureType.containsKey(it)) {
                            val feature = col.featureType[it]
                            feature?.let {
                                if (it.isNotEmpty()) { outgoing.add(create(auditId, it)) }
                            }
                        }
                    }
                }
            }
        }

        if (col.featureAudit.containsKey(auditId)) {
            val feature = col.featureAudit[auditId]
            feature?.let {
                if (it.isNotEmpty()) { outgoing.add(create(auditId, it)) }
            }

        }

        return outgoing
    }

    interface Listener {
        fun onPreExecute()
        fun onPostExecute()
    }

}


/**
 * 1. Batch Subscribe - Done
 * 2. Update the USN Locally - Done
 * 3. Query Audit with USN :: -1 - Done
 *
 * 4. Write Parse API to query rAudit - Done
 * 5. Update the Local Database with the Remote Result - Done
 * 6. Trigger a Column Refresh once this is done so that the results are visible - Done
 * */
