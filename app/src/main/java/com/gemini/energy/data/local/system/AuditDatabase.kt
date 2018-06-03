package com.gemini.energy.data.local.system

import android.arch.persistence.room.*
import android.content.Context
import com.gemini.energy.data.local.dao.AuditDao
import com.gemini.energy.data.local.dao.PreAuditDao
import com.gemini.energy.data.local.dao.ZoneDao
import com.gemini.energy.data.local.model.AuditLocalModel
import com.gemini.energy.data.local.model.PreAuditLocalModel
import com.gemini.energy.data.local.model.ZoneLocalModel
import com.gemini.energy.data.local.util.Converters


@Database(
        entities = [AuditLocalModel::class, ZoneLocalModel::class, PreAuditLocalModel::class],
        version = 4, exportSchema = false)
@TypeConverters(Converters::class)

abstract class AuditDatabase : RoomDatabase() {

    abstract fun auditDao(): AuditDao
    abstract fun preAuditDao(): PreAuditDao
    abstract fun zoneDao(): ZoneDao

    companion object {
        fun newInstance(context: Context): AuditDatabase {
            return Room.databaseBuilder(context, AuditDatabase::class.java, "geo-audit.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // **** This is only for Development ****
                    .build()
        }
    }
}