package com.andarb.movietinder.model.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.remote.RetrofitClient

/**
 * Handles paging and remote data requests for movie details.
 */
class MoviePagingSource : PagingSource<Int, Movie>() {
    private val apiClient = RetrofitClient.create()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val nextPage = params.key ?: 1
            val result = apiClient.getPopularMovies(nextPage)

            LoadResult.Page(
                data = result.movies,
                prevKey = if (nextPage == 1) null else nextPage - 1,
                nextKey = result.page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // This loads starting from previous page, but since PagingConfig.initialLoadSize spans
            // multiple pages, the initial load will still load items centered around
            // anchorPosition. This also prevents needing to immediately launch prepend due to
            // prefetchDistance.
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

}