package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.local.MovieDatabase
import kotlinx.coroutines.launch

class SavedListViewModel(application: Application) : AndroidViewModel(application) {
    private var _items = MutableLiveData<List<Movie>>()
    val items: LiveData<List<Movie>> get() = _items

    private val movieDao = MovieDatabase.getDatabase(application).movieDao()

    /** Retrieves movies from local db */
    fun retrieveList(isLiked: Boolean) {
        if (_items.value == null)
            viewModelScope.launch { _items.value = movieDao.getMovies(isLiked) }
    }
}