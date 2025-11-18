package com.productions666.overlord

import android.app.Application
import com.productions666.overlord.data.database.DatabaseInitializer
import com.productions666.overlord.data.database.OverlordDatabase

class OverlordApplication : Application() {
    val database by lazy { OverlordDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // Initialize default alarm profiles
        DatabaseInitializer.initialize(this)
    }
}

