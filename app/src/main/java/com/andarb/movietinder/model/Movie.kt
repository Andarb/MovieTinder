package com.andarb.movietinder.model

import com.google.gson.annotations.SerializedName

/* Movie details retrieved from TMDb */
class Movie(
    val id: Int,
    val title: String,
    val overview: String,

    @SerializedName("vote_average") val rating: Float,
    @SerializedName("release_date") val date: String,
    @SerializedName("poster_path") val posterUrl: String?
)