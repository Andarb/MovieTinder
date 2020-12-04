package com.andarb.movietinder.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.andarb.movietinder.databinding.ActivityMainBinding
import com.andarb.movietinder.viewmodel.MainViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction

class MainActivity : AppCompatActivity(), CardStackListener {
    private val layoutManager: CardStackLayoutManager = CardStackLayoutManager(this, this)
    private val adapter = MovieCardAdapter()
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardstackMovies.layoutManager = layoutManager
        binding.cardstackMovies.adapter = adapter

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.items.observe(this, { items ->
            adapter.items = items
            viewModel.position?.let { layoutManager.scrollToPosition(it) }
        })

        viewModel.retrieveMovies()
    }

    override fun onStop() {
        viewModel.saveScrollPosition(layoutManager.topPosition)
        super.onStop()
    }

    /** Saves user selection on swipe */
    override fun onCardSwiped(direction: Direction?) {
        val isLiked = direction == Direction.Right

        viewModel.saveMovie(layoutManager.topPosition - 1, isLiked)
    }

    /** Unused implementations */
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}
}