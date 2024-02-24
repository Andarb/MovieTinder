package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.andarb.movietinder.model.Endpoints
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.remote.NearbyClient
import com.andarb.movietinder.model.repository.MovieRepository
import com.andarb.movietinder.util.ClickType
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

/**
 * Shared ViewModel for fragments.
 * Downloads a list of movies.
 * Saves/clears movies in local db when requested.
 * Sends off selected movie to a connected 'Nearby' device
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val movieRepository = MovieRepository(application)
    val dbMovies: LiveData<List<Movie>> = movieRepository.retrieveDb()
    val selectedMovies: MutableList<Movie> = mutableListOf()

    val nearbyMovieIds: MutableLiveData<List<Int>> by lazy { MutableLiveData<List<Int>>() }
    val nearbyMovies: MutableLiveData<List<Movie>> by lazy { MutableLiveData<List<Movie>>() }
    val nearbyDevices: MutableLiveData<Endpoints> = MutableLiveData(Endpoints(mutableListOf()))
    val nearbyClient: NearbyClient =
        NearbyClient(application, nearbyMovieIds, nearbyMovies, nearbyDevices)

    /** Returns downloaded movies from TMDb */
    fun remoteMovies() =
        liveData(Dispatchers.IO) { emit(movieRepository.retrieveOnline(1)) }

    /** Handles clicks on HistoryFragment's RecyclerView buttons */
    fun onClick(movie: Movie, clickType: ClickType) {
        viewModelScope.launch {
            when (clickType) {
                ClickType.LIKE -> {
                    movie.isLiked = !(movie.isLiked)
                    movie.modifiedAt = LocalDate.now()
                    movieRepository.update(movie)
                }

                ClickType.DELETE -> movieRepository.delete(movie)
            }
        }
    }

    /** Saves the selected movie into db */
    fun saveMovie(movie: Movie?, isLiked: Boolean) {
        movie?.let {
            it.isLiked = isLiked
            it.modifiedAt = LocalDate.now()
            if (isLiked) selectedMovies.add(movie)
            viewModelScope.launch { movieRepository.insert(it) }
        }
    }


    /** Delete selected movies from db */
    fun deleteMovies(movies: List<Movie>) {
        viewModelScope.launch { movieRepository.delete(movies) }
    }

    /** Send a list of selected movie ids to a connected 'Nearby' device */
    fun sendMatchedMovies() {
        val movieIds = selectedMovies.map { it.id }
        val deviceId = nearbyDevices.value?.connectedId

        if (deviceId != null) {
            val stringPayload = 'i' + movieIds.joinToString(",") // 'i' identifies "id" payload
            val bytesPayload = Payload.fromBytes(stringPayload.toByteArray())
            nearbyClient.connections.sendPayload(deviceId, bytesPayload)
        }
    }

    /** Send host movie selection to a connected 'Nearby' device */
    fun sendMovieSelection(movies: List<Movie>) {
        val deviceId = nearbyDevices.value?.connectedId

        if (deviceId != null) {
            val stringPayload = 'm' + Json.encodeToString(movies) // 'm' identifies "movie" payload
            val bytesPayload = Payload.fromBytes(stringPayload.toByteArray())
            nearbyClient.connections.sendPayload(deviceId, bytesPayload)
        }
    }
}