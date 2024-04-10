package com.andarb.movietinder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.FragmentSelectionBinding
import com.andarb.movietinder.model.remote.RemoteEndpoint
import com.andarb.movietinder.view.adapters.MovieCardAdapter
import com.andarb.movietinder.viewmodel.MainViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction


/**
 * Presents a selection of movies for the user to choose from.
 */
class SelectionFragment : Fragment(), CardStackListener {

    private lateinit var binding: FragmentSelectionBinding
    private val adapter = MovieCardAdapter()
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectionBinding.inflate(inflater, container, false)
        val layoutManager = CardStackLayoutManager(context, this)
        binding.cardstackMovies.layoutManager = layoutManager
        binding.cardstackMovies.adapter = adapter

        // Clear the last session
        sharedViewModel.localLikedMovies.clear()
        sharedViewModel.remoteMovieIDs.value = null

        val preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val movieCountPref = preferences.getInt(
            getString(R.string.preferences_movie_count_key),
            getString(R.string.preferences_default_movie_count).toInt()
        )

        sharedViewModel.apply {
            if (RemoteEndpoint.hasInitiatedConnection) {
                remoteMovies.observe(viewLifecycleOwner) {
                    adapter.items = it
                }
            } else {
                remoteMovies().observe(viewLifecycleOwner) {
                    val trimmedList = it.movies.subList(0, movieCountPref)
                    shareDownloadedMovies(trimmedList)
                    adapter.items = trimmedList
                }
            }
        }

        return binding.root
    }

    /** Saves user selection on swipe */
    override fun onCardSwiped(direction: Direction?, swipedPosition: Int) {
        if (!RemoteEndpoint.isConnected) findNavController().navigateUp()
        val movie = adapter.items[swipedPosition]
        val isLiked = direction == Direction.Right
        val finalPosition = adapter.itemCount - 1

        sharedViewModel.saveMovie(movie, isLiked)

        // Share your choices, and proceed to results screen after reaching the end of movie list
        if (swipedPosition == finalPosition && findNavController().currentDestination?.id == R.id.selectionFragmentNav) {
            sharedViewModel.shareLikedMovies()
            findNavController().navigate(R.id.action_selectionFragment_to_matchesFragment)
        }
    }

    /** Unused implementations for CardStackView */
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}
}