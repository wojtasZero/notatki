package com.server.notatki.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        name = "notatki.db",
        factory = AppDatabaseConstructor::initialize
    ).setDriver(
        WebWorkerSQLiteDriver(
            worker = Worker("worker.js")
        )
    )
}
