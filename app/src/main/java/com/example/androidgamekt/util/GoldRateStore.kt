package com.example.androidgamekt.util

import android.content.Context

object GoldRateStore {
    private const val PREFS = "gold_rate_prefs"
    private const val KEY_RATE = "rate"
    private const val KEY_TS = "ts"

    fun save(context: Context, rate: Double) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_TS, System.currentTimeMillis())
            .putString(KEY_RATE, rate.toString())
            .apply()
    }

    fun load(context: Context): Pair<Double, Long> {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val rate = sp.getString(KEY_RATE, null)?.toDoubleOrNull() ?: 0.0
        val ts = sp.getLong(KEY_TS, 0L)
        return rate to ts
    }
}


