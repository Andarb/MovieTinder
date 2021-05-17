package com.andarb.movietinder.model.repository

import android.app.Application
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.local.MovieDatabase

/**
 * Handles local data requests for movies.
 */
class MovieRepository(application: Application) {
    private val movieDao = MovieDatabase.getDatabase(application).movieDao()

    /** Retrieve saved liked/disliked movies from local db */
    fun retrieveMovies(isLiked: Boolean) = movieDao.getMovies(isLiked)

    suspend fun insert(movie: Movie) {
        movieDao.insert(movie)
    }

    suspend fun update(movie: Movie) {
        movieDao.update(movie)
    }

    suspend fun delete(movie: Movie) {
        movieDao.delete(movie)
    }

    suspend fun deleteList(isLiked: Boolean) {
        movieDao.deleteList(isLiked)
    }
}