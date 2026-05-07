package com.server.notatki.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Settings::class], version = 3)
abstract class AppDatabase : RoomDatabase(), DB {
    abstract fun noteDao(): NoteDao
    abstract fun settingsDao(): SettingsDao
}

interface DB {
    fun clearAllTables() {}
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun getDatabase(): AppDatabase {
    return getDatabaseBuilder()
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .fallbackToDestructiveMigration(true)
        .build()
}
