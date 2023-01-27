package com.andarb.movietinder.model.remote

import com.andarb.movietinder.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Connects to The Movie Database (TMDb) using the unique [BuildConfig.API_KEY].
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/movie/"

    fun create(): RetrofitService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildOkHttpClient())
            .build()

        return retrofit.create(RetrofitService::class.java)
    }

    // OkHttpClient interceptor adds an API key to all queries
    private fun buildOkHttpClient(): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor { chain ->
            val request = chain.request().newBuilder()
            val originalHttpUrl = chain.request().url
            val url = originalHttpUrl.newBuilder()
                .addQueryParameter("api_key", BuildConfig.API_KEY).build()
            request.url(url)
            return@addInterceptor chain.proceed(request.build())
        }
        return okHttpBuilder.build()
    }

}