package com.gemini.energy.service.sync

import com.gemini.energy.App
import com.gemini.energy.data.local.dao.AuditDao
import com.gemini.energy.data.local.dao.TypeDao
import com.gemini.energy.data.local.dao.ZoneDao
import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.data.local.model.TypeLocalModel
import com.gemini.energy.data.local.model.ZoneLocalModel
import com.gemini.energy.data.local.system.AuditDatabase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.merge
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Read all the Data from the Local DB and Build the Collection
 * -- This happens only once during the start
 * -- This class is to be shared amongst all the others
 * -- At any instance this class shows the State of the Local Database
 * */
class Collection {

    var db: AuditDatabase? = null
    var audit: List<AuditLocalModel> = listOf()
    var zone: HashMap<Int, List<ZoneLocalModel>> = hashMapOf()
    var type: HashMap<Int, List<TypeLocalModel>> = hashMapOf()

    private var auditDAO: AuditDao? = null
    private var zoneDao: ZoneDao? = null
    private var typeDao: TypeDao? = null

    private fun load() = audit()?.subscribe({ audit = it }, { it.printStackTrace() }, { zone() })

    fun audit() = auditDAO?.getAll()?.toObservable()
    fun zone() {
        val taskHolder: MutableList<Observable<List<ZoneLocalModel>>> = mutableListOf()
        audit.forEach {
            zoneDao?.getAllByAudit(it.auditId)?.let {
                taskHolder.add(it.toObservable())
            }
        }

        taskHolder.merge()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        val auditId = it[0].auditId
                        zone[auditId] = it
                    }
                }, { it.printStackTrace() }, { type() })

    }

    fun type() {
        val taskHolder: MutableList<Observable<List<TypeLocalModel>>> = mutableListOf()
        audit.forEach {
            typeDao?.getAllTypeByAudit(it.auditId)?.let {
                taskHolder.add(it.toObservable())
            }
        }

        taskHolder.merge()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val tmp = it.groupBy { it.zoneId!! }
                    for ((zoneId, localType) in tmp) {
                            type[zoneId] = localType
                    }

                }, { it.printStackTrace() }, {

                    Timber.d("## LOAD - COMPLETE ##")
                    Timber.d("AUDIT -- $audit")
                    Timber.d("ZONE -- $zone")
                    Timber.d("TYPE -- $type")

                })

    }

    fun meta() {}

    init {

        Timber.d("Collection :: INIT")
        db = AuditDatabase.newInstance(App.instance)
        auditDAO = db?.auditDao()
        zoneDao = db?.zoneDao()
        typeDao = db?.auditScopeDao()

        load()
    }

    companion object {
        fun create(): Collection {
            Timber.d("Collection :: CREATE")
            return Collection()
        }
    }

}
