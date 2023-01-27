package com.andarb.movietinder.model.remote

import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.Movies
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrieves a list of popular movies or detailed information of a specific movie.
 */
interface RetrofitService {

    @GET(MOST_POPULAR_PATH)
    suspend fun getPopularMovies(@Query(PAGE_QUERY) page: Int): Movies

    @GET(MOVIE_ID_PATH_MASK)
    suspend fun getMovieDetails(
        @Path(MOVIE_ID_PATH) movieId: String,
        @Query(APPEND_QUERY) appendValue: String
    ): Movie

    companion object {
        const val MOVIE_ID_PATH_MASK = "{movie_id}"
        const val MOVIE_ID_PATH = "movie_id"
        const val APPEND_QUERY = "append_to_response"
        const val PAGE_QUERY = "page"
        const val MOST_POPULAR_PATH = "popular"
    }
}