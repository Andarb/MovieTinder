package com.andarb.movietinder.model.repository

import android.app.Application
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.Movies
import com.andarb.movietinder.model.local.MovieDatabase
import com.andarb.movietinder.model.remote.RetrofitClient

/**
 * Handles local data requests for movie details.
 */
class MovieRepository(application: Application) {
    private val movieDao = MovieDatabase.getDatabase(application).movieDao()
    private val apiClient = RetrofitClient.create()

    suspend fun retrieveOnline(page: Int): Movies = apiClient.getPopularMovies(page)

    fun retrieveDb() = movieDao.getMovies()

    suspend fun insert(movie: Movie) {
        movieDao.insert(movie)
    }

    suspend fun update(movie: Movie) {
        movieDao.update(movie)
    }

    suspend fun delete(movie: Movie) {
        movieDao.delete(movie)
    }

    suspend fun delete(movies: List<Movie>) {
        movieDao.delete(movies)
    }
}