package com.gemini.energy.service.sync

import com.gemini.energy.App
import com.gemini.energy.data.local.dao.AuditDao
import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.data.local.system.AuditDatabase
import timber.log.Timber

/**
 * Read all the Data from the Local DB and Build the Collection
 * -- This happens only once during the start
 * -- This class is to be shared amongst all the others
 * -- At any instance this class shows the State of the Local Database
 * */
class Collection {

    var db: AuditDatabase? = null
    var audit: List<AuditLocalModel>? = listOf()

    private var auditDAO: AuditDao? = null

    private fun audit() {
        val observable = auditDAO?.getAllWithUsn(-1)?.toObservable()
        observable?.subscribe { audit = it }
    }

    fun zone() {}
    fun type() {}
    fun meta() {}

    init {

        Timber.d("Collection :: INIT")
        db = AuditDatabase.newInstance(App.instance)
        auditDAO = db?.auditDao()

        audit()

    }

    companion object {
        fun create(): Collection {
            Timber.d("Collection :: CREATE")
            return Collection()
        }
    }

}
