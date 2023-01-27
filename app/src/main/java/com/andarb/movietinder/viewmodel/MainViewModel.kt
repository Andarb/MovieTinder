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
import com.andarb.movietinder.model.Endpoints
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.remote.NearbyClient
import com.andarb.movietinder.model.repository.MoviePagingSource
import com.andarb.movietinder.model.repository.MovieRepository
import com.andarb.movietinder.util.ClickType
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

/**
 * Shared ViewModel for fragments.
 * Downloads a list of movies.
 * Saves/clears movies in local db when requested.
 * Sends off selected movie to a connected 'Nearby' device
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)
    val dbMovies: LiveData<List<Movie>> = repository.retrieveMovies(true)

    val selectedMovies: MutableList<Movie> = mutableListOf()
    val remoteMovieIds: MutableLiveData<List<Int>> by lazy { MutableLiveData<List<Int>>() }

    val nearbyDevices: MutableLiveData<Endpoints> = MutableLiveData(Endpoints(mutableListOf()))

    val nearbyClient: NearbyClient = NearbyClient(application, remoteMovieIds, nearbyDevices)

    val remoteMovies: Flow<PagingData<Movie>> =
        Pager(PagingConfig(pageSize = 15)) { MoviePagingSource() }
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

    /** Saves the selected movie into db */
    fun saveMovie(movie: Movie?, isLiked: Boolean) {
        movie?.let {
            it.isLiked = isLiked
            it.modifiedAt = Date()
            if (isLiked) selectedMovies.add(movie)
            viewModelScope.launch { repository.insert(it) }
        }
    }

    /** Removes all movies from db */
    fun clearMovies(isLiked: Boolean) {
        viewModelScope.launch { repository.deleteList(isLiked) }
    }

    /** Send a list of selected movies to a connected 'Nearby' device */
    fun sendMoviesPayload() {
        val movieIds = selectedMovies.map { it.id }
        val deviceId = nearbyDevices.value?.connectedId

        if (deviceId != null) {
            val bytesPayload = Payload.fromBytes(movieIds.joinToString(",").toByteArray())
            nearbyClient.connections.sendPayload(deviceId, bytesPayload)
        }

    }
}