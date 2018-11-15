package com.gemini.energy.service.sync

import com.gemini.energy.service.ParseAPI

class Connection {

    /**
     * Parse API Service Call
     * */
    private val parseAPIService by lazy { ParseAPI.create() }

    interface TaskListener {
        fun onPreExecute()
        fun onProgressUpdate(vararg values: Any)
        fun onPostExecute(data: Payload)
        fun onDisconnected()
    }

    companion object Payload {
        var taskType: Int = 0
        var data: Array<Any> = arrayOf()
        var result: Any? = null
        var success: Boolean = true
        var returnType: Int = 0
        var exception: Exception? = null
        var message: String = ""
        var col: Collection? = null
    }




}