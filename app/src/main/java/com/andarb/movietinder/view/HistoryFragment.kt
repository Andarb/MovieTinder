package com.andarb.movietinder.view

import android.app.AlertDialog
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
import com.andarb.movietinder.util.FilterMovies
import com.andarb.movietinder.view.adapters.HistoryAdapter
import com.andarb.movietinder.viewmodel.MainViewModel

/**
 * Displays a list of previously browsed movies.
 */
class HistoryFragment : Fragment() {

    private val sharedViewModel: MainViewModel by activityViewModels()
    private var selectedFilterChoice = FilterMovies.ALL.index
    private val adapter = HistoryAdapter { movie: Movie, clickType: ClickType ->
        sharedViewModel.onClick(movie, clickType)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        createMenu()

        binding.recyclerviewMovies.adapter = adapter
        binding.recyclerviewMovies.layoutManager = LinearLayoutManager(context)

        sharedViewModel.dbMovies.observe(viewLifecycleOwner) { updateAdapter() }

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
                        sharedViewModel.deleteMovies(adapter.items)
                        true
                    }
                    R.id.action_filter -> {
                        showFilterDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**  Display a dialogue window with radio buttons for filtering a list of movies */
    private fun showFilterDialog() {
        val choices = resources.getStringArray(R.array.dialog_filter_choices)

        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.dialog_filter_title))
            .setSingleChoiceItems(choices, selectedFilterChoice) { dialog, which ->
                selectedFilterChoice = which
                updateAdapter()
                dialog.dismiss()
            }
            .show()
    }

    /**  Update recyclerview adapter based on the chosen filter */
    private fun updateAdapter() {
        when (FilterMovies.values()[selectedFilterChoice]) {
            FilterMovies.ALL -> adapter.items = sharedViewModel.dbMovies.value ?: emptyList()
            FilterMovies.LIKE -> adapter.items =
                sharedViewModel.dbMovies.value?.filter { it.isLiked } ?: emptyList()
            FilterMovies.DISLIKE -> adapter.items =
                sharedViewModel.dbMovies.value?.filter { !(it.isLiked) } ?: emptyList()
        }
    }
}