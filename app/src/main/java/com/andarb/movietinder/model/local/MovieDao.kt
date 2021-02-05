package com.andarb.movietinder.model.local

import androidx.room.*
import com.andarb.movietinder.model.Movie

/**
 * Manages the liked/disliked movies' table.
 */
@Dao
interface MovieDao {
    @Query("SELECT * FROM movie_table WHERE isLiked = :isLiked ORDER BY modifiedAt DESC")
    suspend fun getMovies(isLiked: Boolean): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie)

    @Update
    suspend fun update(movie: Movie)

    @Delete
    suspend fun delete(movie: Movie)

    @Query("DELETE FROM movie_table")
    suspend fun deleteAll()
}