package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AreaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_area)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val switchButton = findViewById<SwitchCompat>(R.id.switchButton)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            val context = this@AreaActivity

            if (isChecked) {
                switchButton.text = "Açık"
            } else {
                switchButton.text = "Kapalı"
            }
        }
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