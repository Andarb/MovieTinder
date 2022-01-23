package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.repository.MoviePagingSource
import com.andarb.movietinder.model.repository.MovieRepository
import com.andarb.movietinder.util.ClickType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

/**
 * Shared ViewModel for fragments.
 * Downloads a list of movies.
 * Saves/clears movies in local db when requested.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)
    val dbMovies: LiveData<List<Movie>> = repository.retrieveMovies(true)

    val remoteMovies: Flow<PagingData<Movie>> =
        Pager(PagingConfig(pageSize = 20)) { MoviePagingSource() }
            .flow
            .cachedIn(viewModelScope)

    /** Handles clicks on HistoryFragment's RecyclerView buttons */
    fun onClick(movie: Movie, clickType: ClickType) {
        viewModelScope.launch {
            when (clickType) {
                ClickType.LIKE -> {
                    movie.isLiked = !(movie.isLiked)
                    movie.modifiedAt = Date()
                    repository.update(movie)
                }
                ClickType.DELETE -> repository.delete(movie)
            }
        }
    }

    /** Saves selected movies into db */
    fun saveMovie(movie: Movie?, isLiked: Boolean) {
        movie?.let {
            it.isLiked = isLiked
            it.modifiedAt = Date()
            viewModelScope.launch { repository.insert(it) }
        }
    }

    /** Removes selected movies from db */
    fun clearMovies(isLiked: Boolean) {
        viewModelScope.launch { repository.deleteList(isLiked) }
    }
}