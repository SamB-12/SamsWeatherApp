package com.example.samsweatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.samsweatherapp.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    private var binding :ActivityMainBinding ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (!isLocationEnabled()){
            Toast.makeText(this,"Turn on the GPS",Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            //Toast.makeText(this,"Location is already on!",Toast.LENGTH_SHORT).show()

            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()){
                            Toast.makeText(this@MainActivity,"All Permissions Granted! Go Ahead",Toast.LENGTH_SHORT).show()
                            //TODO add showRationaleForDialog
                        }

                        if (report.isAnyPermissionPermanentlyDenied){
                            Toast.makeText(this@MainActivity,"Location Permissions are denied. Go to settings to allow them",Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    /**
     * This method checks if the location service in the device has been turned on.
     *
     * return Boolean   A Boolean value that denotes if the location service has been turned on or not
     */
    private fun isLocationEnabled():Boolean{
        val locationManager :LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showRationaleForDialog(){

    }
}