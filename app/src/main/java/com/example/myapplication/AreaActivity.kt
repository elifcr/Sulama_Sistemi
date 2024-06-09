package com.example.myapplication

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AreaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sulamaButton: Button
    private lateinit var switchButton: SwitchCompat
    private var isSulamaRunning = false
    private var isSwitchChecked = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("com.example.myapplication", MODE_PRIVATE)
        sulamaButton = findViewById(R.id.sulamaButton)
        switchButton = findViewById(R.id.switchButton)

        isSulamaRunning = sharedPreferences.getBoolean("isSulamaRunning", false)
        isSwitchChecked = sharedPreferences.getBoolean("isSwitchChecked", false)

        updateButtonText()
        switchButton.isChecked = isSwitchChecked
        updateSwitchText()

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            isSwitchChecked = isChecked
            updateSwitchText()
            savePreferences()
        }

        sulamaButton.setOnClickListener {
            if (!isSwitchChecked) {
                Toast.makeText(this, "Bahçe durumu kapalı, sulama yapılamaz.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isSulamaRunning = !isSulamaRunning
            updateButtonText()
            showToastMessage()
            savePreferences()
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "isSwitchChecked") {
                isSwitchChecked = sharedPreferences.getBoolean(key, false)
                switchButton.isChecked = isSwitchChecked
                updateSwitchText()
            }
        }
    }

    private fun updateButtonText() {
        val startButtonText = "Sulamayı Başlat"
        val stopButtonText = "Sulamayı Durdur"
        if (isSulamaRunning) {
            sulamaButton.text = stopButtonText
        } else {
            sulamaButton.text = startButtonText
        }
    }

    private fun updateSwitchText() {
        if (isSwitchChecked) {
            switchButton.text = "Açık"
        } else {
            switchButton.text = "Kapalı"
        }
    }

    private fun showToastMessage() {
        if (isSulamaRunning) {
            Toast.makeText(this, "Sulama başladı", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sulama durduruldu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isSulamaRunning", isSulamaRunning)
        editor.putBoolean("isSwitchChecked", isSwitchChecked)
        editor.apply()
    }

    fun deleteGardenArea(view: View) {
        val firebaseUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        firebaseUser?.uid?.let { uid ->
            db.collection("bahce")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            db.collection("bahce").document(document.id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Bahçe alanı silindi.", Toast.LENGTH_SHORT).show()
                                    findViewById<TextView>(R.id.relativeLayout).visibility = View.GONE
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Silme işlemi başarısız oldu: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Silinecek bahçe alanı bulunamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Hata: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}