package com.andarb.movietinder.model.remote

import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.Movies
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/* Uses The Movie Database (TMDb) API */
interface ApiService {

    @GET(MOST_POPULAR_PATH)
    suspend fun getPopularMovies(): Movies

    @GET(MOVIE_ID_PATH_MASK)
    suspend fun getMovieDetails(
        @Path(MOVIE_ID_PATH) movieId: String,
        @Query(APPEND_QUERY) appendValue: String
    ): Movie

    companion object {
        const val MOVIE_ID_PATH_MASK = "{movie_id}"
        const val MOVIE_ID_PATH = "movie_id"
        const val APPEND_QUERY = "append_to_response"
        const val MOST_POPULAR_PATH = "popular"
    }
}