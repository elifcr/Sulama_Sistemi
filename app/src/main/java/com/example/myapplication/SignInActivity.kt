package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Util.LoginActivity
import com.example.myapplication.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()

        binding.kayitGirisLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val kayitAdsoyad = findViewById<EditText>(R.id.kayitAdsoyad)
        val kayitEposta = findViewById<EditText>(R.id.kayitEposta)
        val kayitsifre = findViewById<EditText>(R.id.kayitsifre)
        val kayitsifretekrar = findViewById<EditText>(R.id.kayitsifretekrar)
        val buttonKayitOl = findViewById<Button>(R.id.buttonKayitOl)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val username = kayitAdsoyad.text.toString()
                val email = kayitEposta.text.toString()
                val password = kayitsifre.text.toString()
                val password2 = kayitsifretekrar.text.toString()

                if (username.isBlank() || email.isBlank() || password.isBlank() || password2.isBlank()) {
                    buttonKayitOl.isEnabled = false
                    if (username.isBlank()) {
                        kayitAdsoyad.error = "Kullanıcı adı boş olamaz!"
                    }
                    if (email.isBlank()) {
                        kayitEposta.error = "E-posta boş olamaz!"
                    }
                    if (password.isBlank()) {
                        kayitsifre.error = "Şifre boş olamaz!"
                    }
                    if (password2.isBlank()) {
                        kayitsifretekrar.error = "Şifre tekrarı boş olamaz!"
                    }

                } else if (password != password2) {
                    buttonKayitOl.isEnabled = false
                    kayitsifretekrar.error = "Şifreler eşleşmiyor!"
                } else {
                    buttonKayitOl.isEnabled = true
                    kayitAdsoyad.error = null
                    kayitEposta.error = null
                    kayitsifre.error = null
                    kayitsifretekrar.error = null
                }
                buttonKayitOl.isEnabled = username.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        password2.isNotBlank() &&
                        password == password2
                if (email.isBlank() || password.isBlank()) {
                    return
                }

                if (username.isBlank() || password2.isBlank()) {
                    Toast.makeText(
                        this@SignInActivity,
                        "Kullanıcı adı ve tekrar şifre alanları boş olamaz!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }
        }
        kayitAdsoyad.addTextChangedListener(textWatcher)
        kayitEposta.addTextChangedListener(textWatcher)
        kayitsifre.addTextChangedListener(textWatcher)
        kayitsifretekrar.addTextChangedListener(textWatcher)

        buttonKayitOl.setOnClickListener {
            val username = kayitAdsoyad.text?.toString() ?: ""
            val email = kayitEposta.text?.toString() ?: ""
            val password = kayitsifretekrar.text?.toString() ?: ""
            val password2 = kayitsifretekrar.text?.toString() ?: ""

            if (username.isBlank() || email.isBlank() || password.isBlank() || password2.isBlank()) {
                Toast.makeText(
                    this,
                    "Kullanıcı adı, e-posta ve şifre alanları boş olamaz!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (password != password2) {
                Toast.makeText(this, "Şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.kayitGirisLink.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        val user = hashMapOf(
                            "username" to username
                        )

                        // Firestore örneği
                        val db = FirebaseFirestore.getInstance()
                        firebaseUser?.uid?.let { uid ->
                            db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener {
                                    // Firestore'a kullanıcı adını kaydettik
                                }
                                .addOnFailureListener {
                                    // Firestore'a kullanıcı adını kaydedemedik
                                }
                        }

                        // Realtime Database örneği
                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("users")
                        firebaseUser?.uid?.let { uid ->
                            reference.child(uid).setValue(user)
                                .addOnSuccessListener {
                                    // Realtime Database'e kullanıcı adını kaydettik
                                }
                                .addOnFailureListener {
                                    // Realtime Database'e kullanıcı adını kaydedemedik
                                }
                        }

                        Toast.makeText(this, "Kayıt işlemi başarılı!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> "Bu e-posta adresi zaten kullanımda."
                            is FirebaseAuthInvalidCredentialsException -> "Geçersiz e-posta adresi veya şifre."
                            else -> "Kayıt işlemi başarısız."
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun goToLogin(view: View) {}
}