package com.example.samsweatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.samsweatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    private var binding :ActivityMainBinding ?= null
    private lateinit var fusedLocationClient : FusedLocationProviderClient //used to get lat and long of the device.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied){
                            Toast.makeText(this@MainActivity,"Location Permissions are denied. Go to settings to allow them",Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRationaleForDialogForPermissions() //this takes the user to show dialog to get permissions
                    }

                }).onSameThread().check()//never forget this haha!
        }
    }

    /**
     * This method checks if the location service in the device has been turned on.
     *
     * return Boolean   A Boolean value that denotes if the location service has been turned on or not
     */
    private fun isLocationEnabled():Boolean{
        val locationManager :LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager // this gets to location manager of the device
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * This method shows an alert dialog that asks the user if they want to be taken to the application settings to enable the required permissions.
     */
    private fun showRationaleForDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off the permissions. Kindly turn it on!")
            .setPositiveButton("Go to Setting"){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)//This just goes to the general app settings
                    val uri = Uri.fromParts("package", packageName, null)// this opens up settings page for our particular app.
                    intent.data = uri
                    startActivity(intent)
                } catch (e:Exception){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * This method gets the current location of the device.
     *
     * The method only gets called if the appropriate permissions are granted and the location service is turned on in the device
     */
    private fun requestLocationData(){
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                Log.i("Lattitude",location?.latitude.toString())
//            }

        val currentLocationRequestBuilder = CurrentLocationRequest.Builder() // a location request needs to be built to ask for the current location
        currentLocationRequestBuilder.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        val currentLocationRequest = currentLocationRequestBuilder.build()

        fusedLocationClient.getCurrentLocation(currentLocationRequest,null).addOnSuccessListener { location:Location? ->
            Log.d("LatLong","lat = ${location?.latitude}")
        }
    }
}