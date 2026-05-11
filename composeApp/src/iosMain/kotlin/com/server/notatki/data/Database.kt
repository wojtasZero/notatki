package com.server.notatki.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import platform.Foundation.NSHomeDirectory



actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/notatki.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory = AppDatabaseConstructor::initialize
    ).setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
}
