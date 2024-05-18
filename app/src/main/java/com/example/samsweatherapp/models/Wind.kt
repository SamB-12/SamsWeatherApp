package com.example.samsweatherapp.models

import java.io.Serializable

data class Wind(
    val speed: Float,
    val deg: Int,
    val gust: Float
): Serializable
