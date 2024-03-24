package com.andarb.movietinder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.R
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

        sharedViewModel.nearbyMovieIds.observe(viewLifecycleOwner) { ids ->
            if (ids != null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_matches_received),
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressbarMatches.visibility = View.GONE

                val localIds = sharedViewModel.selectedMovies.map { it.id }
                val matchedIds: List<Int> = localIds.intersect(ids.toSet()).toList()
                val localMovies: List<Movie> = sharedViewModel.selectedMovies

                val matchedMovies: List<Movie> =
                    localMovies.filter { localMovie ->
                        localMovie.id in matchedIds
                    }

                adapter.items = matchedMovies
                if (matchedMovies.isEmpty()) binding.tvIvNoMatches.visibility = View.VISIBLE
            }
        }

        if (sharedViewModel.nearbyDevices.value?.connectedId.isNullOrBlank()) {
            binding.tvIvNoMatches.visibility = View.GONE
            binding.tvIvNotConnected.visibility = View.VISIBLE
        } else {
            binding.tvIvNotConnected.visibility = View.GONE
            binding.progressbarMatches.visibility = View.VISIBLE
            sharedViewModel.sendMatchedMovies()
        }

        return binding.root
    }
}