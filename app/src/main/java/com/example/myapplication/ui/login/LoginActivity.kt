package com.example.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.SignInActivity
import com.example.myapplication.SplashActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val login = binding.buttonGirisYap
        auth = Firebase.auth

        binding.GirisKayitOlLink.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        login.setOnClickListener {
            val username = binding.girisEposta.text.toString()
            val password = binding.girisSifre.text.toString()

            if (username == "" || password == "") {
                Toast.makeText(this, "Lütfen email ve şifre giriniz.", Toast.LENGTH_LONG).show()
            } else {
                auth.signInWithEmailAndPassword(username, password)
                    .addOnSuccessListener {
                        val intent = Intent(this, SplashActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}
