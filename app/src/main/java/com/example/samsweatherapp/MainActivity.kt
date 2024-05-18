package com.example.samsweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.samsweatherapp.databinding.ActivityMainBinding
import com.example.samsweatherapp.models.WeatherResponse
import com.example.samsweatherapp.network.WeatherService
import com.example.samsweatherapp.utils.Constants
import com.example.samsweatherapp.utils.NetworkChecker
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient //used to get lat and long of the device.
    private var latitude : Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isLocationEnabled()) {
            Toast.makeText(this, "Turn on the GPS", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            //Toast.makeText(this,"Location is already on!",Toast.LENGTH_SHORT).show()

            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "Location Permissions are denied. Go to settings to allow them",
                                Toast.LENGTH_SHORT
                            ).show()
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
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager // this gets to location manager of the device
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * This method shows an alert dialog that asks the user if they want to be taken to the application settings to enable the required permissions.
     */
    private fun showRationaleForDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off the permissions. Kindly turn it on!")
            .setPositiveButton("Go to Setting") { _, _ ->
                try {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)//This just goes to the general app settings
                    val uri = Uri.fromParts(
                        "package",
                        packageName,
                        null
                    )// this opens up settings page for our particular app.
                    intent.data = uri
                    startActivity(intent)
                } catch (e: Exception) {
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
    @SuppressLint("MissingPermission")//I am suppressing it because there is no need to check for permissions. This method will only get called if the permissions are given in the first place.
    private fun requestLocationData() {
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                Log.i("Lattitude",location?.latitude.toString())
//            }

        val currentLocationRequestBuilder =
            CurrentLocationRequest.Builder() // a location request needs to be built to ask for the current location
        currentLocationRequestBuilder.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        val currentLocationRequest = currentLocationRequestBuilder.build()

        fusedLocationClient.getCurrentLocation(currentLocationRequest, null)
            .addOnSuccessListener { location: Location? ->
                latitude = location!!.latitude
                longitude = location.longitude
                //Log.d("LatLong", "lat = ${location?.latitude}")
                getWeatherForLocation()
            }
    }

    /**
     * This method makes the api call to the server and gets the weather data based on the location of the device.
     */
    private fun getWeatherForLocation(){
        val networkChecker = NetworkChecker()
        if (networkChecker.isNetworkAvailable(this)){
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.BASE_URL)
                .build()

            val service : WeatherService = retrofit.create<WeatherService>(WeatherService::class.java) // this creates an object that maps the endpoints and can be used to make the API calls

            val listCall : Call<WeatherResponse> = service.getWeather(lat = latitude, lon = longitude, appid = Constants.WEATHER_API_KEY, units = Constants.METRIC_UNIT)

            listCall.enqueue(object : Callback<WeatherResponse>{
                override fun onResponse(p0: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful){
                        val weatherList : WeatherResponse? = response.body()//this gets the api call's body
                        Log.i("Response Result",weatherList.toString())
                        binding?.tvHello?.text = weatherList?.toString()
                    } else{
                        val responseCode = response.code()
                        when{
                            responseCode == 400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }
                            responseCode == 404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }

                    Toast.makeText(this@MainActivity,"API RESPONSE CAME",Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(p0: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Error", t.message.toString())
                }

            })
        } else {
            Toast.makeText(this@MainActivity,"No Internet Available",Toast.LENGTH_SHORT).show()
        }
    }
}