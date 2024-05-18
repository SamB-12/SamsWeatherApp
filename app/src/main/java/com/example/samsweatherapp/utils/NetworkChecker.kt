package com.example.samsweatherapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkChecker {

    /**
     * This method checks if the device has network capabilities and has the ability to connect to the internet.
     *
     * return Boolean  A Boolean value representing if the internet is available or not
     */
    fun isNetworkAvailable(context: Context) : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false //we create the network. if empty, we return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)?: return false//we check for network capabilities

        return when{//returns true when the device is connected to internet through wifi, sim, ethernet
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false //when there is no internet
        }
    }
}