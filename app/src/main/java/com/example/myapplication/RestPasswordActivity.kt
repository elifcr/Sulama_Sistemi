package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRestPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class RestPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRestPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.buttonReset.setOnClickListener {
            val email = binding.emailEditText.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "Lütfen e-posta adresinizi girin.", Toast.LENGTH_LONG).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Şifre sıfırlama e-postası gönderildi.", Toast.LENGTH_LONG).show()
                        finish()
                    }.addOnFailureListener { exception ->
                        if (exception is FirebaseAuthException) {
                            when (exception.errorCode) {
                                "ERROR_USER_NOT_FOUND" -> {
                                    Toast.makeText(this, "Kayıtlı olmayan bir e-posta adresi girdiniz.", Toast.LENGTH_LONG).show()
                                }
                                else -> {
                                    Toast.makeText(this, "Hata: ${exception.errorCode}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, exception.localizedMessage ?: "Bilinmeyen bir hata oluştu.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
        binding.geriEposta.setOnClickListener {
            onBackPressed()
        }
    }
}