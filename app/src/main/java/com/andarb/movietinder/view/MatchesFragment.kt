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
import com.andarb.movietinder.model.remote.RemoteEndpoint
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.view.adapters.MatchesAdapter
import com.andarb.movietinder.viewmodel.MainViewModel

/**
 * Shows a list of liked movies that match with a connected 'Nearby' device selections.
 */
class MatchesFragment : Fragment() {

    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentMatchesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchesBinding.inflate(inflater, container, false)
        val adapter = MatchesAdapter { movie: Movie, clickType: ClickType ->
            sharedViewModel.onClick(movie, clickType)
        }

        if (RemoteEndpoint.hasReceivedMatches) {
            displayView(binding.recyclerviewMatches)  // matches received
        } else if (RemoteEndpoint.isConnected) {
            displayView(binding.progressbarMatches) // awaiting matches
        } else {
            displayView(binding.tvIvNotConnected) // not connected to anyone
        }

        binding.recyclerviewMatches.adapter = adapter
        binding.recyclerviewMatches.layoutManager = LinearLayoutManager(context)

        sharedViewModel.remoteMovieIDs.observe(viewLifecycleOwner) { remoteIDs ->
            if (remoteIDs != null) {
                displayView(binding.recyclerviewMatches)

                val localIDs = sharedViewModel.localLikedMovies.map { it.id }
                val matchedIDs: List<Int> = localIDs.intersect(remoteIDs.toSet()).toList()

                val localMovies: List<Movie> = sharedViewModel.localLikedMovies
                val matchedMovies: List<Movie> =
                    localMovies.filter { localMovie ->
                        localMovie.id in matchedIDs
                    }
                adapter.items = matchedMovies

                if (matchedMovies.isEmpty()) displayView(binding.tvIvNoMatches)
            }
        }

        return binding.root
    }

    /** Hides all views except the 'active' one */
    private fun displayView(activeView: View) {
        binding.tvIvNotConnected.visibility = View.GONE
        binding.progressbarMatches.visibility = View.GONE
        binding.tvIvNoMatches.visibility = View.GONE
        activeView.visibility = View.VISIBLE
    }
}