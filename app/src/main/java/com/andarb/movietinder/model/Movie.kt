package com.andarb.movietinder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Movie details retrieved from TMDb API and deserialized via GSON are stored here.
 * Also used as an Entity for a local db. Properties [isLiked] and [createdAt] are unique to db.
 */
@Entity(tableName = "movie_table")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("vote_average") val rating: Float,
    @SerializedName("release_date") val date: String,
    @SerializedName("poster_path") val posterUrl: String?,
    var isLiked: Boolean,
    var createdAt: Date
)