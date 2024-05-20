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
import com.andarb.movietinder.model.remote.RemoteEndpoint
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
    val localLikedMovies: MutableList<Movie> = mutableListOf()

    val remoteMovieIDs: MutableLiveData<List<Int>?> by lazy { MutableLiveData<List<Int>?>() }
    val remoteMovies: MutableLiveData<List<Movie>> by lazy { MutableLiveData<List<Movie>>() }
    val nearbyDevices: MutableLiveData<Endpoints> = MutableLiveData(Endpoints(mutableListOf()))
    val nearbyClient: NearbyClient =
        NearbyClient(application, remoteMovieIDs, remoteMovies, nearbyDevices)

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

    /** Saves the selected movie to local storage */
    fun saveMovie(movie: Movie?, isLiked: Boolean) {
        movie?.let {
            it.isLiked = isLiked
            it.modifiedAt = LocalDate.now()
            if (isLiked) localLikedMovies.add(movie)
            viewModelScope.launch { movieRepository.insert(it) }
        }
    }

    /** Delete selected movies from local storage */
    fun deleteMovies(movies: List<Movie>) {
        viewModelScope.launch { movieRepository.delete(movies) }
    }

    /** Send a list of liked movie ids to the connected 'Nearby' device */
    fun shareLikedMovies() {
        val movieIds = localLikedMovies.map { it.id }

        // 'e' = empty payload, 'i' = movie id payload
        val stringPayload: String = 'i' + movieIds.joinToString(",")

        val bytesPayload = Payload.fromBytes(stringPayload.toByteArray())
        nearbyClient.connections.sendPayload(RemoteEndpoint.deviceId, bytesPayload)
    }

    /** Host sends the downloaded movies to the connected 'Nearby' device */
    fun shareDownloadedMovies(movies: List<Movie>) {
        val stringPayload = 'm' + Json.encodeToString(movies) // 'm' identifies "movie" payload
        val bytesPayload = Payload.fromBytes(stringPayload.toByteArray())

        nearbyClient.connections.sendPayload(RemoteEndpoint.deviceId, bytesPayload)
    }
}