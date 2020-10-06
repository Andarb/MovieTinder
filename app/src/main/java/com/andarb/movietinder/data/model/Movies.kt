package com.andarb.movietinder.data.model

import com.google.gson.annotations.SerializedName

/* A list of movies retrieved from TMDb */
class Movies(@SerializedName("results") val movies: List<Movie>)
