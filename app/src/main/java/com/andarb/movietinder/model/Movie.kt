package com.andarb.movietinder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

/* Movie details retrieved from TMDb, some of which might be stored in the local database */
@Entity(tableName = "movie_table")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    var isLiked: Boolean,
    var createdAt: Date,

    @SerializedName("vote_average") val rating: Float,
    @SerializedName("release_date") val date: String,
    @SerializedName("poster_path") val posterUrl: String?
)