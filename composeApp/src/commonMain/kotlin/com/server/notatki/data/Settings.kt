package com.server.notatki.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 0,
    val showToolbar: Boolean = true,
    val fontSize: Int = 20,
    val wrapText: Boolean = true,
    val lastContent: String = ""
)
