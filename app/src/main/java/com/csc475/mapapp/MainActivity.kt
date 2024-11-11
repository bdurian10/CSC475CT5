package com.csc475.mapapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.csc475.mapapp.ui.theme.MapAppTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Initialize map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //set up location request
        setupLocationUpdates()

    }

    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        if(checkPermissions()){
            startLocationUpdates()
        } else{
            requestPermissions()
        }
    }

    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(2000L) // min interval between updates
            .setMaxUpdateDelayMillis(15000L)   // max delay before delivering updates
            .build()

        // Set up the location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateMapWithLocation(location)
                }
            }
        }
    }


        private fun updateMapWithLocation(location: Location){
        val currentLatLng = LatLng(location.latitude, location.longitude)
        mMap.clear() //clear previous marker
        mMap.addMarker(MarkerOptions().position(currentLatLng).title("Updated Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1000
        )
    }

    private fun startLocationUpdates() {
        try {
            // Check if permissions are granted before requesting location updates
            if (checkPermissions()) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    mainLooper
                )
            } else {
                requestPermissions()
            }
        } catch (e: SecurityException) {
            // Handle exception when user revokes permission while app is running
            e.printStackTrace()
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startLocationUpdates()
        } else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        stopLocationUpdates()
    }
}