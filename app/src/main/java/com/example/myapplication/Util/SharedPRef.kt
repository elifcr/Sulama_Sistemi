package com.example.myapplication.Util

import android.content.Context

object SharedPRef {
    private val PREF_NAME = "MyAppPreferences"
    private val PREF_FIRST_TIME_LAUNCH = "isFirstTimeLaunch"

    fun isFirstTimeLaunch(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(PREF_FIRST_TIME_LAUNCH, true)
    }

    fun setFirstTimeLaunch(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_FIRST_TIME_LAUNCH, false)
        editor.apply()
    }
}