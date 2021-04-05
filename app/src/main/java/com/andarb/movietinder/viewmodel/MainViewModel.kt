package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.repository.MoviePagingSource
import com.andarb.movietinder.model.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for MainActivity.
 * Retrieves a list of movies on launch.
 * Saves movies into local db when requested.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)

    private val _items = MutableLiveData<List<Movie>>()
    val items: LiveData<List<Movie>> get() = _items

    val movies: Flow<PagingData<Movie>> = Pager(PagingConfig(pageSize = 20)) { MoviePagingSource() }
        .flow
        .cachedIn(viewModelScope)

    private var _position: Int? = null
    val position: Int? get() = _position

    /** Saves user selection into local db */
    fun saveMovie(movie: Movie?, isLiked: Boolean) {
        movie?.let {
            it.isLiked = isLiked
            it.modifiedAt = Date()
            viewModelScope.launch { repository.insert(it) }
        }
    }

    /** Saves position of CardStackView */
    fun saveScrollPosition(index: Int) {
        _position = index
    }
}