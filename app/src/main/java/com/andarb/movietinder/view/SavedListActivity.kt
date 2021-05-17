package com.andarb.movietinder.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ActivitySavedListBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.view.adapters.SavedListAdapter
import com.andarb.movietinder.viewmodel.SavedListViewModel
import com.andarb.movietinder.viewmodel.SavedListViewModelFactory

const val EXTRA_ISLIKED = "com.andarb.movietinder.viewmodel.extras.EXTRA_ISLIKED"

/**
 * Displays a previously compiled list of either liked or disliked movies.
 */
class SavedListActivity : AppCompatActivity() {

    private lateinit var viewModel: SavedListViewModel
    private lateinit var viewModelFactory: SavedListViewModelFactory
    private var isLikedExtra = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySavedListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isLikedExtra = intent.getBooleanExtra(EXTRA_ISLIKED, true)
        val adapter = SavedListAdapter { movie: Movie, clickType: ClickType ->
            viewModel.onClick(movie, clickType)
        }

        binding.recyclerviewMovies.adapter = adapter
        binding.recyclerviewMovies.layoutManager = LinearLayoutManager(this)

        viewModelFactory = SavedListViewModelFactory(application, isLikedExtra)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SavedListViewModel::class.java)
        viewModel.items.observe(this, { adapter.items = it })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_savedlist, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_clear -> {
                viewModel.clearMovies(isLikedExtra)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}