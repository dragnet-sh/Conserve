package com.gemini.energy.service.sync

import android.content.Context
import com.gemini.energy.App
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.service.ParseAPI

class Connection {

    /**
     * Parse API Service Call
     * */
    private val parseAPIService by lazy { ParseAPI.create() }
    private var context: Context? = null

    init { this.context = App.instance }

    fun sync(mListener: Syncer.Listener? = null) {
        val col = Collection.create()
//        val syncer = Syncer(parseAPIService, col, mListener)
//        syncer.sync()
    }

}