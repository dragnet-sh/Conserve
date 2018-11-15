package com.gemini.energy.service.sync

/**
 * Read all the Data from the Local DB and Build the Collection
 * -- This happens only once during the start
 * -- This class is to be shared amongst all the others
 * -- At any instance this class shows the State of the Local Database
 * */
class Collection {

    var feature: Feature? = null

    fun audit() {}
    fun zone() {}
    fun type() {}
    fun meta() {}

    fun sync() {}

}


class Feature {}
class Audit {}
class Zone {}
class Type {}
