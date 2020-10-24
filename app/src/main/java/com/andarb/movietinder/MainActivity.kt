package com.andarb.movietinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andarb.movietinder.data.api.ApiClient
import com.andarb.movietinder.data.model.Movies
import com.andarb.movietinder.databinding.ActivityMainBinding
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardstackMovies.layoutManager = CardStackLayoutManager(this)

        GlobalScope.launch(Dispatchers.Main) {
            val client = ApiClient.create()
            val result: Movies? = client.getPopularMovies()

            if (result != null) binding.cardstackMovies.adapter = MovieCardAdapter(result.movies)
        }
    }
}