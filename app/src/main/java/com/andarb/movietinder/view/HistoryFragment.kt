package com.andarb.movietinder.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.andarb.movietinder.R
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.util.FilterMovies
import com.andarb.movietinder.view.composables.HistoryContent
import com.andarb.movietinder.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Displays a list of previously browsed movies.
 */
class HistoryFragment : Fragment() {

    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var selectedFilter: MutableState<Int>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        createMenu()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val movies = sharedViewModel.dbMovies.observeAsState(initial = emptyList())
                selectedFilter = remember { mutableStateOf(FilterMovies.ALL.index) }

                // Filter preference, then group and sort movies by date last interacted
                val filteredMovies = filterMovies(movies.value)
                val sortedMovies =
                    (filteredMovies.groupBy { it.modifiedAt }).toSortedMap(Comparator.reverseOrder())

                val clickListener: (Movie, ClickType) -> Unit =
                    { movie: Movie, clickType: ClickType ->
                        sharedViewModel.onClick(
                            movie,
                            clickType
                        )
                    }

                HistoryContent(sortedMovies, clickListener, resources)
            }
        }
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
                        showClearDialog()
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

    /**  Return a chosen, filtered movie list */
    private fun filterMovies(unfiltered: List<Movie>?): List<Movie> {
        return unfiltered.let { movies ->
            when (FilterMovies.entries[selectedFilter.value]) {
                FilterMovies.ALL -> movies
                FilterMovies.LIKE -> movies?.filter { it.isLiked }
                FilterMovies.DISLIKE -> movies?.filter { !(it.isLiked) }
            }
        } ?: emptyList()
    }

    /**  Display a dialogue window with radio buttons for filtering a list of movies */
    private fun showFilterDialog() {
        val choices = resources.getStringArray(R.array.dialog_filter_choices)

        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.dialog_filter_title))
            .setSingleChoiceItems(choices, selectedFilter.value) { dialog, which ->
                selectedFilter.value = which
                dialog.dismiss()
            }
            .show()
    }

    /**  Display a dialogue window to confirm erasure of selected movies from history */
    private fun showClearDialog() {
        MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme)
            .setTitle(R.string.dialog_confirm)
            .setMessage(R.string.dialog_erase_history)
            .setPositiveButton(R.string.dialog_erase_history_yes) { dialog, id ->
                sharedViewModel.apply { deleteMovies(filterMovies(dbMovies.value)) }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, id ->
                dialog.dismiss()
            }.show()
    }
}