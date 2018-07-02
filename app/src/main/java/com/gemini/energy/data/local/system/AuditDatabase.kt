package com.gemini.energy.data.local.system

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.gemini.energy.data.local.dao.AuditDao
import com.gemini.energy.data.local.dao.FeatureDao
import com.gemini.energy.data.local.dao.TypeDao
import com.gemini.energy.data.local.dao.ZoneDao
import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.data.local.model.FeatureLocalModel
import com.gemini.energy.data.local.model.TypeLocalModel
import com.gemini.energy.data.local.model.ZoneLocalModel
import com.gemini.energy.data.local.util.Converters


@Database(
        entities = [

            AuditLocalModel::class,
            ZoneLocalModel::class,
            TypeLocalModel::class,
            FeatureLocalModel::class

        ],

        version = 10, exportSchema = false)
@TypeConverters(Converters::class)

abstract class AuditDatabase : RoomDatabase() {

    abstract fun auditDao(): AuditDao
    abstract fun zoneDao(): ZoneDao
    abstract fun auditScopeDao(): TypeDao
    abstract fun featureDao(): FeatureDao

    companion object {
        fun newInstance(context: Context): AuditDatabase {
            return Room.databaseBuilder(context, AuditDatabase::class.java, "geo-audit.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // **** This is only for Development ****
                    .build()
        }
    }
}