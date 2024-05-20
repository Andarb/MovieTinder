package com.andarb.movietinder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.andarb.movietinder.R
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
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        if (RemoteEndpoint.hasSentMatches) {
            displayView(binding.recyclerviewMatches)  // matches received
        } else if (RemoteEndpoint.isConnected) {
            displayView(binding.progressbarMatches) // awaiting matches
        } else {
            displayView(binding.tvIvNotConnected) // not connected to anyone
            actionBar?.title = getString(R.string.action_bar_title_no_matches)
        }

        binding.recyclerviewMatches.adapter = adapter
        binding.recyclerviewMatches.layoutManager = GridLayoutManager(requireActivity(), 2)

        sharedViewModel.remoteMovieIDs.observe(viewLifecycleOwner) { remoteIDs ->
            if (remoteIDs != null) {
                val matchedDevice =
                    RemoteEndpoint.deviceName.ifEmpty { RemoteEndpoint.lastDeviceName }
                actionBar?.title = getString(R.string.action_bar_title_has_matches, matchedDevice)

                displayView(binding.recyclerviewMatches)
                sharedViewModel.nearbyClient.matchesBadge.isVisible = false

                val localIDs = sharedViewModel.localLikedMovies.map { it.id }
                val matchedIDs: List<Int> = localIDs.intersect(remoteIDs.toSet()).toList()

                val localMovies: List<Movie> = sharedViewModel.localLikedMovies
                val matchedMovies: List<Movie> =
                    localMovies.filter { localMovie ->
                        localMovie.id in matchedIDs
                    }
                if (matchedMovies.isEmpty()) displayView(binding.tvIvNoMatches)
                adapter.items = matchedMovies

            } else if (!RemoteEndpoint.hasSentMatches && !RemoteEndpoint.isConnected) { // friend disconnected while fragment is active
                displayView(binding.tvIvNotConnected)
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