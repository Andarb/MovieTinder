package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.local.MovieDatabase
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.util.checkAndRun
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for SavedListActivity.
 * Retrieves a list of either liked or disliked movies.
 * Handles click events set in SavedListAdapter.
 */
class SavedListViewModel(application: Application) : AndroidViewModel(application) {
    private var _items = MutableLiveData<List<Movie>>()
    val items: LiveData<List<Movie>> get() = _items

    private val movieDao = MovieDatabase.getDatabase(application).movieDao()

    /** Retrieves movies from local db */
    fun retrieveList(isLiked: Boolean) {
        if (_items.value == null)
            viewModelScope.launch { _items.value = movieDao.getMovies(isLiked) }
    }

    /** Handles clicks on RecyclerView items' buttons */
    fun onClick(index: Int, clickType: ClickType) {
        _items.checkAndRun(index) { movie ->
            viewModelScope.launch {
                when (clickType) {
                    ClickType.LIKE -> {
                        movie.modifiedAt = Date()
                        movie.isLiked = !(movie.isLiked)
                        movieDao.update(movie)
                    }
                    ClickType.DELETE -> movieDao.delete(movie)
                }
            }
        }
    }

}