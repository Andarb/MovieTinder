package com.andarb.movietinder.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.databinding.ActivitySavedListBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySavedListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isLikedExtra = intent.getBooleanExtra(EXTRA_ISLIKED, true)
        val adapter = SavedListAdapter { pos: Int, clickType: ClickType ->
            viewModel.onClick(pos, clickType)
        }

        binding.recyclerviewMovies.adapter = adapter
        binding.recyclerviewMovies.layoutManager = LinearLayoutManager(this)

        viewModelFactory = SavedListViewModelFactory(application, isLikedExtra)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SavedListViewModel::class.java)
        viewModel.items.observe(this, { adapter.items = it })
    }
}