package ru.wanket.opengappsupdater

import android.content.Context
import android.preference.PreferenceManager

class Settings(context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val dayInSeconds = 86400L // day = 86400 sec

    var checkUpdateTime: Long
    get() {
        return preferences.getLong("checkUpdateTime", dayInSeconds)
    }
    set(value) {
        preferences.edit().putLong("checkUpdateTime", value).apply()
    }

    var isFirstLaunch: Boolean
    get() {
        return preferences.getBoolean("isFirstLaunch", true)
    }
    set(value) {
        preferences.edit().putBoolean("isFirstLaunch", value).apply()
    }
}
