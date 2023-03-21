package com.andarb.movietinder.model.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.andarb.movietinder.model.Movie

/**
 * Manages the movies' db table.
 */
@Dao
interface MovieDao {
    @Query("SELECT * FROM movie_table ORDER BY modifiedAt DESC")
    fun getMovies(): LiveData<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie)

    @Update
    suspend fun update(movie: Movie)

    @Delete
    suspend fun delete(movie: Movie)

    @Delete
    suspend fun delete(movies: List<Movie>)
}