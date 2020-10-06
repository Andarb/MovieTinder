package com.andarb.movietinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andarb.movietinder.data.api.ApiClient
import com.andarb.movietinder.data.model.Movies
import com.andarb.movietinder.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlobalScope.launch(Dispatchers.Main) {
            val client = ApiClient.create()
            val movieList: Movies? = client.getPopularMovies()

            binding.textId.text = movieList!!.movies[0].id.toString()
            binding.textTitle.text = movieList.movies[0].title
            binding.textDate.text = movieList.movies[0].date
            binding.textOverview.text = movieList.movies[0].overview
            binding.textPoster.text = movieList.movies[0].posterUrl
        }
    }
}