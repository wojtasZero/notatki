package com.server.notatki.data

import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    throw UnsupportedOperationException("Room is not supported on Wasm yet")
}
