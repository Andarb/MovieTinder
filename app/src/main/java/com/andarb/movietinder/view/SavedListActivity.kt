package com.andarb.movietinder.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.databinding.ActivitySavedListBinding
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.view.adapters.SavedListAdapter
import com.andarb.movietinder.viewmodel.SavedListViewModel

const val EXTRA_ISLIKED = "com.andarb.movietinder.viewmodel.extras.EXTRA_ISLIKED"

/**
 * Displays a previously compiled list of either liked or disliked movies.
 */
class SavedListActivity : AppCompatActivity() {

    private lateinit var viewModel: SavedListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLikedExtra = intent.getBooleanExtra(EXTRA_ISLIKED, true)
        val binding = ActivitySavedListBinding.inflate(layoutInflater)
        val adapter = SavedListAdapter { pos: Int, clickType: ClickType ->
            viewModel.onClick(pos, clickType)
        }

        setContentView(binding.root)

        binding.recyclerviewMovies.adapter = adapter
        binding.recyclerviewMovies.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this).get(SavedListViewModel::class.java)
        viewModel.items.observe(this, { adapter.items = it })

        viewModel.retrieveList(isLikedExtra)
    }
}