package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.MovieRepository
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.util.checkAndRun
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for SavedListActivity.
 * Handles click events set in SavedListAdapter.
 */
class SavedListViewModel(application: Application, isLiked: Boolean) :
    AndroidViewModel(application) {
    private val repository = MovieRepository(application)
    val items: LiveData<List<Movie>> = repository.retrieveMovies(isLiked)

    /** Handles clicks on RecyclerView items' buttons */
    fun onClick(index: Int, clickType: ClickType) {
        items.checkAndRun(index) { movie ->
            viewModelScope.launch {
                when (clickType) {
                    ClickType.LIKE -> {
                        movie.modifiedAt = Date()
                        movie.isLiked = !(movie.isLiked)
                        repository.update(movie)
                    }
                    ClickType.DELETE -> repository.delete(movie)
                }
            }
        }
    }

}