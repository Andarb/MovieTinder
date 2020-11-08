package com.andarb.movietinder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.Movies
import com.andarb.movietinder.model.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _items = MutableLiveData<List<Movie>>()
    val items: LiveData<List<Movie>> get() = _items

    private var _position = 0
    val position: Int get() = _position

    fun retrieveMovies() {
        if (_items.value == null) {
            viewModelScope.launch {
                val client = ApiClient.create()
                val result: Movies = withContext(Dispatchers.IO) { client.getPopularMovies() }

                _items.value = result.movies
            }
        }
    }


    fun savePosition(index: Int) {
        _position = index
    }
}