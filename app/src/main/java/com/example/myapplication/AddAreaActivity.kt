package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddAreaActivity : AppCompatActivity() {

    private lateinit var bahceAdiEditText: EditText
    private lateinit var alanEditText: EditText
    private lateinit var ekleButton: Button
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_area)

        firestore = FirebaseFirestore.getInstance()

        bahceAdiEditText = findViewById(R.id.BahceAdi)
        alanEditText = findViewById(R.id.Alan)
        ekleButton = findViewById(R.id.buttonGirisYap)

        ekleButton.setOnClickListener {
            val bahceAdi = bahceAdiEditText.text.toString()
            val alan = alanEditText.text.toString()
            val bahceBilgileri = hashMapOf(
                "bahceAdi" to bahceAdi,
                "alan" to alan
            )

            firestore.collection("users")
                .add(bahceBilgileri)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Bahçe başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}