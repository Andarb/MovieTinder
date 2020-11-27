package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.Movies
import com.andarb.movietinder.model.local.MovieDatabase
import com.andarb.movietinder.model.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _items = MutableLiveData<List<Movie>>()
    val items: LiveData<List<Movie>> get() = _items

    private var _position: Int? = null
    val position: Int? get() = _position

    val movieDao = MovieDatabase.getDatabase(application).movieDao()

    fun retrieveMovies() {
        if (_items.value == null) {
            viewModelScope.launch {
                val client = ApiClient.create()
                val result: Movies = withContext(Dispatchers.IO) { client.getPopularMovies() }

                _items.value = result.movies
            }
        }
    }

    fun saveMovie(index: Int, isLiked: Boolean) {
        val item = _items.value?.getOrNull(index)

        item?.let {
            it.isLiked = isLiked
            it.createdAt = Date()
            viewModelScope.launch(Dispatchers.IO) { movieDao.insert(it) }
        }
    }

    fun saveScrollPosition(index: Int) {
        _position = index
    }
}