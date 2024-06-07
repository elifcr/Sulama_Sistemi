package com.example.myapplication.Util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.HomePageActivity
import com.example.myapplication.RestPasswordActivity
import com.example.myapplication.SignInActivity
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

        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val rememberMeChecked = sharedPreferences.getBoolean("REMEMBER_ME", false)
        if (rememberMeChecked) {
            val savedEmail = sharedPreferences.getString("EMAIL", "")
            val savedPassword = sharedPreferences.getString("PASSWORD", "")
            binding.girisEposta.setText(savedEmail)
            binding.girisSifre.setText(savedPassword)
            binding.rememberMeCheckBox.isChecked = true
        }

        binding.GirisKayitOlLink.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, RestPasswordActivity::class.java)
            startActivity(intent)
        }

        login.setOnClickListener {
            val username = binding.girisEposta.text.toString()
            val password = binding.girisSifre.text.toString()

            if (username == "" || password == "") {
                Toast.makeText(this, "Lütfen email ve şifre giriniz.", Toast.LENGTH_LONG).show()
            } else {
                auth.signInWithEmailAndPassword(username, password)
                    .addOnSuccessListener {
                        if (binding.rememberMeCheckBox.isChecked) {
                            editor.putBoolean("REMEMBER_ME", true)
                            editor.putString("EMAIL", username)
                            editor.putString("PASSWORD", password)
                            editor.apply()
                        } else {
                            editor.clear().apply()
                        }
                        val intent = Intent(this, HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
        }


    }
}
