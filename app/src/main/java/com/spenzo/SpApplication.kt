package com.spenzo

import android.app.Application
import com.spenzo.data.BudgetStore
import com.spenzo.data.SettingsStore

class SpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsStore.init(this)
        BudgetStore.init(this)
    }
}
