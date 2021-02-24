package com.andarb.movietinder.model

import android.app.Application
import com.andarb.movietinder.model.local.MovieDatabase
import com.andarb.movietinder.model.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles local and remote data requests for movies.
 */
class MovieRepository(application: Application) {
    private val movieDao = MovieDatabase.getDatabase(application).movieDao()
    private val apiClient = ApiClient.create()

    /** Download movies from TMDb */
    suspend fun downloadMovies(): List<Movie> {
        val result = withContext(Dispatchers.IO) { apiClient.getPopularMovies() }
        return result.movies
    }

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
}