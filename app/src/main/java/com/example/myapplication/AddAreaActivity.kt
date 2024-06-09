package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddAreaActivity : AppCompatActivity() {

    private lateinit var bahceAdiEditText: EditText
    private lateinit var alanEditText: EditText
    private lateinit var ekleButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_area)

        val cities = listOf(
            "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin",
            "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale",
            "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum",
            "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Isparta", "Mersin",
            "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli",
            "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş",
            "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas",
            "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak",
            "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan",
            "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)

        var autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autoComplete)
        autoCompleteTextView.setAdapter(adapter)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        bahceAdiEditText = findViewById(R.id.BahceAdi)
        alanEditText = findViewById(R.id.Alan)
        autoCompleteTextView = findViewById(R.id.autoComplete)
        ekleButton = findViewById(R.id.buttonGirisYap)

        ekleButton.setOnClickListener {
            val bahceAdi = bahceAdiEditText.text.toString()
            val alan = alanEditText.text.toString()
            val sehir = autoCompleteTextView.text.toString()
            val userId = auth.currentUser?.uid

            val bahceBilgileri = hashMapOf(
                "bahceAdi" to bahceAdi,
                "alan" to alan,
                "sehir" to sehir,
                "userId" to userId
            )

            firestore.collection("bahce")
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