package com.andarb.movietinder.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andarb.movietinder.model.Movie

/**
 * Manages the liked/disliked movies' table.
 */
@Dao
interface MovieDao {
    @Query("SELECT * FROM movie_table WHERE isLiked = :isLiked ORDER BY createdAt DESC")
    suspend fun getMovies(isLiked: Boolean): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie)

    @Query("DELETE FROM movie_table")
    suspend fun deleteAll()
}