@file:Suppress("DEPRECATION")

package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class HomePageActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    val API: String = "2b559f2681dfd958736e2f48d4fca3b8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
        auth = FirebaseAuth.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if(ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            getLocation()
        }

        val alanEkleButton = findViewById<Button>(R.id.alanEkle)
        alanEkleButton.setOnClickListener {
            val intent = Intent(
                this@HomePageActivity,
                AddAreaActivity::class.java
            )
            startActivity(intent)
        }

        val profileButton = findViewById<Button>(R.id.profile)
        profileButton.setOnClickListener {
            val intent = Intent(
                this@HomePageActivity,
                ProfileActivity::class.java
            )
            startActivity(intent)
        }

        val notificationButton = findViewById<Button>(R.id.notification)
        notificationButton.setOnClickListener {
            val intent = Intent(
                this@HomePageActivity,
                NotificationActivity::class.java
            )
            startActivity(intent)
        }

        val relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout)
        relativeLayout.setOnClickListener {
            val intent = Intent(
                this@HomePageActivity,
                AreaActivity::class.java
            )
            startActivity(intent)
        }

        val firebaseUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        firebaseUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.data
                        val username = user?.get("username")
                        val kullaniciAdiTextView = findViewById<TextView>(R.id.kullanici_adi)
                        kullaniciAdiTextView.text = username.toString()
                    } else {
                        // Belge boş
                    }
                }
                .addOnFailureListener { exception ->
                    // Firestore'dan veri alınamadı
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Konum bilgileri başarıyla alındı, burada hava durumu verilerini alabilirsiniz
                    getWeatherData(latitude, longitude)
                } else {
                    // Konum bilgileri alınamadı
                    Toast.makeText(this, "Konum bilgisi bulunamadı.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val urlString = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&lang=tr&appid=$API"

        val request = object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String? {
                return try {
                    val url = URL(urlString)
                    val urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.requestMethod = "GET"
                    urlConnection.connect()

                    val inputStream = urlConnection.inputStream
                    inputStream.bufferedReader().use {
                        it.readText()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                if (result != null) {
                    parseWeatherData(result)
                } else {
                    Toast.makeText(this@HomePageActivity, "Hava durumu verileri alınamadı.", Toast.LENGTH_LONG).show()
                }
            }
        }
        request.execute()
    }

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

            // Hava durumu ikonunu ayarlama
            val imageView = findViewById<ImageView>(R.id.imageView3)
            when (weatherObj.getString("main")) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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


    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            getLocation()
        }
    }
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}