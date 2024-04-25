package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WalkthroughActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_walkthrough)

        if(isFirstTimeLaunch()){
            WalkthroughActivity()
            setFirstTimeLaunch(false)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private val PREF_NAME = "MyAppPreferences"
    private val PREF_FIRST_TIME_LAUNCH = "isFirstTimeLaunch"
    private fun isFirstTimeLaunch(): Boolean {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(PREF_FIRST_TIME_LAUNCH, true)
    }
    private fun setFirstTimeLaunch(isFirstTime: Boolean) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_FIRST_TIME_LAUNCH, isFirstTime)
        editor.apply()
    }
}