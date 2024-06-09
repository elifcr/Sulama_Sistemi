package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomePageActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private val API: String = "2b559f2681dfd958736e2f48d4fca3b8"
    private lateinit var switchButton: SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences
    private var isSwitchChecked = false

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        switchButton = findViewById(R.id.switchButton)

        sharedPreferences = getSharedPreferences("com.example.myapplication", MODE_PRIVATE)
        isSwitchChecked = sharedPreferences.getBoolean("isSwitchChecked", false)
        switchButton.isChecked = isSwitchChecked
        updateSwitchText()

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            isSwitchChecked = isChecked
            updateSwitchText()
            savePreferences()
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "isSwitchChecked") {
                isSwitchChecked = sharedPreferences.getBoolean(key, false)
                switchButton.isChecked = isSwitchChecked
                updateSwitchText()
            }
        }

        auth = FirebaseAuth.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            getLocation()
        }

        val alanEkleButton = findViewById<Button>(R.id.alanEkle)
        alanEkleButton.setOnClickListener {
            startActivity(Intent(this@HomePageActivity, AddAreaActivity::class.java))
        }

        val profileButton = findViewById<Button>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this@HomePageActivity, ProfileActivity::class.java))
        }

        val relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout)
        relativeLayout.setOnClickListener {
            startActivity(Intent(this@HomePageActivity, AreaActivity::class.java))
        }


        fetchUserData()
    }

    private fun updateSwitchText() {
        if (isSwitchChecked) {
            switchButton.text = "Açık"
        } else {
            switchButton.text = "Kapalı"
        }
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isSwitchChecked", isSwitchChecked)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomePageActivity", "onResume çağrıldı")
        isSwitchChecked = sharedPreferences.getBoolean("isSwitchChecked", false)
        switchButton.isChecked = isSwitchChecked
        updateSwitchText()
        fetchBahceData()
    }

    private fun fetchUserData() {
        val firebaseUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        firebaseUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.data
                        val username = user?.get("username")
                        findViewById<TextView>(R.id.kullanici_adi).text = username.toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchBahceData() {
        val firebaseUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        firebaseUser?.uid?.let { uid ->
            db.collection("bahce")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val bahceAdi = document.getString("bahceAdi")
                            val alan = document.getString("alan")
                            val relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout)
                            val bahceAdiTextView = findViewById<TextView>(R.id.arkaBahce)
                            val alanTextView = findViewById<TextView>(R.id.alan)

                            relativeLayout.visibility = View.VISIBLE
                            bahceAdiTextView.text = bahceAdi
                            alanTextView.text = alan
                        }
                    } else {
                        findViewById<RelativeLayout>(R.id.relativeLayout).visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Hata: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    getWeatherData(latitude, longitude)
                } else {
                    Toast.makeText(this, "Konum bilgisi bulunamadı.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val urlString = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&lang=tr&appid=$API"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val inputStream = urlConnection.inputStream
                val result = inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    parseWeatherData(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomePageActivity, "Hava durumu verileri alınamadı.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun parseWeatherData(json: String) {
        try {
            val jsonObj = JSONObject(json)
            val main = jsonObj.getJSONObject("main")
            val temp = main.getString("temp")
            val humidity = main.getString("humidity")
            val weatherArray = jsonObj.getJSONArray("weather")
            val weatherObj = weatherArray.getJSONObject(0)
            val description = weatherObj.getString("description")
            val city = jsonObj.getString("name")
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
            val formattedDate = dateFormat.format(calendar.time)

            findViewById<TextView>(R.id.textViewH).text = "$city, $formattedDate"
            findViewById<TextView>(R.id.textView3).text = "${temp}°C"
            findViewById<TextView>(R.id.textView4).text = description
            findViewById<TextView>(R.id.textView5).text = "Nem Oranı: %$humidity"

            val imageView = findViewById<ImageView>(R.id.imageView3)
            when (weatherObj.getString("main")) {
                // Hava durumu koşullarına göre imageView güncelleme mantığı ekleyin
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Uygulamanın konum bilgisine erişim izni verilmedi.", Toast.LENGTH_LONG).show()
                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                settingsIntent.data = uri
                startActivity(settingsIntent)
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}