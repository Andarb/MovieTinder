package com.andarb.movietinder.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.andarb.movietinder.databinding.ActivityMainBinding
import com.andarb.movietinder.viewmodel.MainViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager

class MainActivity : AppCompatActivity() {
    private val layoutManager: CardStackLayoutManager = CardStackLayoutManager(this)
    private val adapter = MovieCardAdapter()
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardstackMovies.layoutManager = layoutManager
        binding.cardstackMovies.adapter = adapter

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.items.observe(this, {
            adapter.items = it
            layoutManager.scrollToPosition(viewModel.position)
        })

        viewModel.retrieveMovies()
    }

    override fun onStop() {
        viewModel.savePosition(layoutManager.topPosition)
        super.onStop()
    }
}