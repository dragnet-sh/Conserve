package com.gemini.energy.service.sync

import com.gemini.energy.data.local.model.AuditLocalModel
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

    private val taskHolder: MutableList<Observable<Unit>> = mutableListOf()
    private val auditList: MutableList<AuditLocalModel> = mutableListOf()

    fun sync() {

        Timber.d("<< SYNC >>")
        Timber.d(col.audit.toString())

        val audit = col.audit

        audit.forEach { local ->
            val outgoing = buildFeature(local.auditId)
            Timber.d("<<|>> -- <<|>> -- <<|>>")
            Timber.d(outgoing.toString())

            auditList.add(local)
//            taskHolder.add(
//                    parseAPIService.saveAudit(outgoing)
//                            .map { Timber.d(it.toString()) }.toObservable())
        }

        taskHolder.merge()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    auditList.forEach { col.db?.auditDao()?.update(it) }
                }, { it.printStackTrace() }, { })

    }

    private fun download() {
        val query = JSONObject().put("usn", 0)
        parseAPIService.fetchAudit(query.toString()).toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    val tmp = it.getAsJsonArray("results")
                    tmp.forEach {
                        val auditId = it.asJsonObject.get("auditId").asString
                        val name = it.asJsonObject.get("name").asString
                        val usn = it.asJsonObject.get("usn").asInt

                        val localAuditId = auditList.map { it.auditId }

                        if (!localAuditId.contains(auditId.toInt())) {
                            val model = AuditLocalModel(auditId.toInt(), name, usn, Date(), Date())
                            col.db?.auditDao()?.insert(model)
                        }
                    }

                }, { it.printStackTrace() }, {
                    Timber.d("-- DOWNLOAD COMPLETE --"); mListener?.onPostExecute() })
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

        return outgoing
    }

    /**
     * 2. Zone
     * */
    private fun buildZone(auditId: Int): JsonObject {

        fun associatedTypes(zoneId: Int?): JsonArray {
            val type = JsonArray()
            if (col.type.containsKey(zoneId)) {
                val typeList = col.type[zoneId]
                typeList?.forEach {
                    type.add(it.auditParentId)
                }
            }
            return type
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
    private fun buildFeature(auditId: Int): JsonObject {

        fun joinFields(fields: List<String?>): String {
            val result = StringBuilder(128)
            fields.forEach {
                it?.let {
                    result.append(it).append("\u001f")
                }
            }

//            Timber.d(":: Test Fields ::")
//            Timber.d(fields.toString())
//
//            Timber.d(":: Test Split ::")
//            val tmp = result.toString().split("\\x1f".toRegex())
//            Timber.d(tmp.toString())

            return result.toString()
        }

        val outgoing = JsonObject()

        if (col.typeIdsByAudit.containsKey(auditId)) {
            val typeIds = col.typeIdsByAudit[auditId]
            typeIds?.let {
                it.forEach {
                    it?.let {

                        if (col.featureType.containsKey(it)) {
                            val feature = col.featureType[it]
                            feature?.let {
                                if (it.isNotEmpty()) {

                                    val inner = JsonObject()
                                    inner.addProperty("auditId", auditId)
                                    inner.addProperty("zoneId", it[0].zoneId)
                                    inner.addProperty("typeId", it[0].typeId)
                                    inner.addProperty("modelId", -999)
                                    inner.addProperty("values", joinFields( it.map { it.valueString }))

                                    outgoing.add(it[0].typeId.toString(), inner)
                                }
                            }
                        }

                    }
                }
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
