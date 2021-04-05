package com.andarb.movietinder.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ActivityMainBinding
import com.andarb.movietinder.view.adapters.MovieCardAdapter
import com.andarb.movietinder.viewmodel.MainViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        lifecycleScope.launch {
            viewModel.movies.collectLatest { items ->
                adapter.submitData(items)
                viewModel.position?.let { layoutManager.scrollToPosition(it) }
            }
        }
    }

    override fun onStop() {
        viewModel.saveScrollPosition(layoutManager.topPosition)
        super.onStop()
    }

    /** Saves user selection on swipe */
    override fun onCardSwiped(direction: Direction?) {
        val movie = adapter.peek(layoutManager.topPosition - 1)
        val isLiked = direction == Direction.Right

        viewModel.saveMovie(movie, isLiked)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, SavedListActivity::class.java)

        return when (item.itemId) {
            R.id.menu_liked -> {
                intent.putExtra(EXTRA_ISLIKED, true)
                startActivity(intent)
                true
            }
            R.id.menu_disliked -> {
                intent.putExtra(EXTRA_ISLIKED, false)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Unused implementations */
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}
}