package com.gemini.energy.service.sync

import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.service.ParseAPI
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.merge
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class Syncer(private val parseAPIService: ParseAPI.ParseAPIService,
             private val col: Collection) {

    private val taskHolder: MutableList<Observable<Unit>> = mutableListOf()
    private val auditList: MutableList<AuditLocalModel> = mutableListOf()
    private lateinit var fragment: AuditListFragment

    fun sync(fragment: AuditListFragment) {

        this.fragment = fragment

        Timber.d("<< SYNC >>")
        Timber.d(col.audit.toString())

        val audit = col.audit

        audit?.forEach { local ->
            val outgoing = JsonObject()
            outgoing.addProperty("auditId", local.auditId.toString())
            outgoing.addProperty("name", local.name)
            outgoing.addProperty("usn", ++local.usn)
            outgoing.addProperty("mod", Date().time.toString())

            Timber.d("PARSE API :: AUDIT - SAVE (POST)")
            Timber.d(outgoing.toString())

            auditList.add(local)
            taskHolder.add(
                    parseAPIService.saveAudit(outgoing)
                            .map { Timber.d(it.toString()) }.toObservable())
        }

        taskHolder.merge()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    auditList.forEach { col.db?.auditDao()?.update(it) }
                }, { it.printStackTrace() }, { download() })

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

                }, { it.printStackTrace() }, { Timber.d("-- DOWNLOAD COMPLETE --"); fragment.refresh() })
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


// ToDo -- Figure out a way to elegantly Refresh the Data !!