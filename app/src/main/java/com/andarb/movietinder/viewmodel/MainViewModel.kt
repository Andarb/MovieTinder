package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.MovieRepository
import com.andarb.movietinder.util.checkAndRun
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

    private var _position: Int? = null
    val position: Int? get() = _position

    /** Does a network request for a list of movies */
    init {
        viewModelScope.launch { _items.value = repository.downloadMovies() }
    }

    /** Saves user selection into local db */
    fun saveMovie(index: Int, isLiked: Boolean) {
        _items.checkAndRun(index) { movie ->
            movie.isLiked = isLiked
            movie.modifiedAt = Date()
            viewModelScope.launch { repository.insert(movie) }
        }
    }

    /** Saves position of CardStackView */
    fun saveScrollPosition(index: Int) {
        _position = index
    }
}