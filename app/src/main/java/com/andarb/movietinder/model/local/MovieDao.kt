package com.andarb.movietinder.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andarb.movietinder.model.Movie

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie_table WHERE isLiked = 1 ORDER BY createdAt DESC")
    fun getLikedMovies(): LiveData<List<Movie>>

    @Query("SELECT * FROM movie_table WHERE isLiked = 0 ORDER BY createdAt DESC")
    fun getDislikedMovies(): LiveData<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movie: Movie)

    @Query("DELETE FROM movie_table")
    fun deleteAll()
}