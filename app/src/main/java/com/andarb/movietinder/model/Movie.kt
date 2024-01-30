package com.andarb.movietinder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andarb.movietinder.util.DiffutilComparison
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDate
import java.util.*

/**
 * Movie details retrieved from TMDb API and deserialized via GSON are stored here.
 * Also used as an Entity for a local db. Properties [isLiked] and [modifiedAt] are unique to db.
 */
@Serializable
@Entity(tableName = "movie_table")
data class Movie(
    @PrimaryKey override val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("vote_average") val rating: Float,
    @SerializedName("release_date") val date: String,
    @SerializedName("poster_path") val posterUrl: String?,

    var isLiked: Boolean,
    @Transient var modifiedAt: LocalDate = LocalDate.of(1900, 1, 1)
) : DiffutilComparison