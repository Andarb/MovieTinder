package com.andarb.movietinder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.databinding.FragmentMatchesBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.view.adapters.MatchesAdapter
import com.andarb.movietinder.viewmodel.MainViewModel

/**
 * Shows a list of liked movies that match with a connected 'Nearby' device selections.
 */
class MatchesFragment : Fragment() {

    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMatchesBinding.inflate(inflater, container, false)

        val adapter = MatchesAdapter { movie: Movie, clickType: ClickType ->
            sharedViewModel.onClick(movie, clickType)
        }
        binding.recyclerviewMatches.adapter = adapter
        binding.recyclerviewMatches.layoutManager = LinearLayoutManager(context)

        sharedViewModel.remoteMovieIds.observe(viewLifecycleOwner) { remoteIds ->
            val localIds = sharedViewModel.selectedMovies.map { it.id }
            val matchedIds: List<Int> = localIds.intersect(remoteIds.toSet()).toList()
            val localMovies: List<Movie> = sharedViewModel.selectedMovies

            val matchedMovies: List<Movie> =
                localMovies.filter { localMovie ->
                    localMovie.id in matchedIds
                }

            adapter.items = matchedMovies
        }

        sharedViewModel.sendMoviesPayload()

        return binding.root
    }
}