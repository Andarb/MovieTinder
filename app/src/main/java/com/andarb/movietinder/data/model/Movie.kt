package com.andarb.movietinder.data.model

import com.google.gson.annotations.SerializedName

/* Movie details retrieved from TMDb */
class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("release_date") val date: String,
    @SerializedName("poster_path") val posterUrl: String?
)