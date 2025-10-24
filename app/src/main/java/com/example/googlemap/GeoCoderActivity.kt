package com.example.googlemap

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale

data class State(val name: String, val latLng: LatLng)

class GeoCoderActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val states = listOf(
        State("Select a State", LatLng(20.5937, 78.9629)), // Default India view
        State("Andhra Pradesh", LatLng(16.5062, 80.6480)),
        State("Arunachal Pradesh", LatLng(27.09, 93.62)),
        State("Assam", LatLng(26.14, 91.77)),
        State("Bihar", LatLng(25.59, 85.14)),
        State("Chhattisgarh", LatLng(21.28, 81.63)),
        State("Goa", LatLng(15.29, 74.12)),
        State("Gujarat", LatLng(22.31, 71.19)),
        State("Haryana", LatLng(29.06, 76.09)),
        State("Himachal Pradesh", LatLng(31.10, 77.17)),
        State("Jharkhand", LatLng(23.34, 85.31))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_coder)

        // Existing Geocoding functionality
        val addressInput = findViewById<EditText>(R.id.addressInput)
        val geoButton = findViewById<Button>(R.id.button)
        val reverseGeoButton = findViewById<Button>(R.id.reverseGeoButton)
        val outputText = findViewById<TextView>(R.id.output)
        val geocoder = Geocoder(this, Locale.getDefault())
        setupGeocodingButtons(addressInput, geoButton, reverseGeoButton, outputText, geocoder)

        // New Map and Spinner functionality
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val statesSpinner = findViewById<Spinner>(R.id.statesSpinner)
        val stateNames = states.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stateNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statesSpinner.adapter = adapter

        statesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (::mMap.isInitialized) {
                    val selectedState = states[position]
                    mMap.clear() // Clear previous markers
                    mMap.addMarker(MarkerOptions().position(selectedState.latLng).title(selectedState.name))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedState.latLng, 7f))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Set default map position
        val defaultState = states[0]
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultState.latLng, 5f))
    }

    private fun setupGeocodingButtons(
        addressInput: EditText,
        geoButton: Button,
        reverseGeoButton: Button,
        outputText: TextView,
        geocoder: Geocoder
    ) {
        geoButton.setOnClickListener {
            val address = addressInput.text.toString()
            if (address.isNotEmpty()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocationName(address, 1) { locations ->
                            if (locations.isNotEmpty()) {
                                val lat = locations[0].latitude
                                val lon = locations[0].longitude
                                runOnUiThread {
                                    outputText.text = "Latitude: $lat, Longitude: $lon"
                                    updateMap(lat, lon, address)
                                }
                            } else {
                                runOnUiThread {
                                    outputText.text = "No location found"
                                }
                            }
                        }
                    } else {
                        Thread {
                            try {
                                @Suppress("DEPRECATION")
                                val location = geocoder.getFromLocationName(address, 1)
                                runOnUiThread {
                                    if (!location.isNullOrEmpty()) {
                                        val lat = location[0].latitude
                                        val lon = location[0].longitude
                                        outputText.text = "Latitude: $lat, Longitude: $lon"
                                        updateMap(lat, lon, address)
                                    } else {
                                        outputText.text = "No location found"
                                    }
                                }
                            } catch (e: IOException) {
                                runOnUiThread {
                                    outputText.text = "Geocoding failed: ${e.message}"
                                }
                            }
                        }.start()
                    }
                } catch (e: IOException) {
                    outputText.text = "Geocoding failed: ${e.message}"
                }
            } else {
                Toast.makeText(this, "Enter an address", Toast.LENGTH_SHORT).show()
            }
        }

        reverseGeoButton.setOnClickListener {
            val lat = 28.6139
            val lon = 77.2090
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            runOnUiThread {
                                outputText.text = "Address: ${address.getAddressLine(0)}"
                            }
                        } else {
                            runOnUiThread {
                                outputText.text = "No address found"
                            }
                        }
                    }
                } else {
                    Thread {
                        try {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                            runOnUiThread {
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    outputText.text = "Address: ${address.getAddressLine(0)}"
                                } else {
                                    outputText.text = "No address found"
                                }
                            }
                        } catch (e: IOException) {
                            runOnUiThread {
                                outputText.text = "Reverse geocoding failed: ${e.message}"
                            }
                        }
                    }.start()
                }
            } catch (e: Exception) {
                outputText.text = "An error occurred: ${e.message}"
            }
        }
    }

    private fun updateMap(lat: Double, lon: Double, title: String) {
        if (::mMap.isInitialized) {
            val location = LatLng(lat, lon)
            mMap.clear() // Remove previous markers
            mMap.addMarker(MarkerOptions().position(location).title(title))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
        }
    }
}
