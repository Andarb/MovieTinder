package com.andarb.movietinder.view

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.FragmentHistoryBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.view.adapters.HistoryAdapter
import com.andarb.movietinder.viewmodel.MainViewModel

/**
 * Displays a list of previously browsed movies.
 */
class HistoryFragment : Fragment() {

    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        createMenu()

        val adapter = HistoryAdapter { movie: Movie, clickType: ClickType ->
            sharedViewModel.onClick(movie, clickType)
        }

        binding.recyclerviewMovies.adapter = adapter
        binding.recyclerviewMovies.layoutManager = LinearLayoutManager(context)

        sharedViewModel.dbMovies.observe(viewLifecycleOwner) { adapter.items = it }

        return binding.root
    }

    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_history, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_clear -> {
                        sharedViewModel.clearMovies(true)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}