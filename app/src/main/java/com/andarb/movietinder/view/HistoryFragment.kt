package com.andarb.movietinder.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.FragmentHistoryBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.view.adapters.SavedListAdapter
import com.andarb.movietinder.viewmodel.MainViewModel

/**
 * Displays a previously compiled list of either liked or disliked movies.
 */
class HistoryFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        val adapter = SavedListAdapter { movie: Movie, clickType: ClickType ->
            viewModel.onClick(movie, clickType)
        }

        binding.recyclerviewMovies.adapter = adapter
        binding.recyclerviewMovies.layoutManager = LinearLayoutManager(context)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.dbMovies.observe(this, { adapter.items = it })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                viewModel.clearMovies(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}