package com.andarb.movietinder.util

import android.widget.ImageView
import com.bumptech.glide.Glide

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/"
private const val POSTER_SIZE = "w500" // applicable sizes "w92", "w154", "w185", "w342" and "w780"

/** Simplifies downloading and displaying movie posters */
fun ImageView.load(filePath: String?) {
    Glide.with(this).load(POSTER_BASE_URL + POSTER_SIZE + filePath).into(this)
}