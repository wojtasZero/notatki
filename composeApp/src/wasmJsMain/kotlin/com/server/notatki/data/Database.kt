package com.server.notatki.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import org.dany.worker.createSQLiteWasmWorker

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        name = "notatki.db",
        factory = AppDatabaseConstructor::initialize
    ).setDriver(createSQLiteWasmWorker())
}
