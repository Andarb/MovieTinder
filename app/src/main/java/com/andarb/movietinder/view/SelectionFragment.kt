package com.andarb.movietinder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.FragmentSelectionBinding
import com.andarb.movietinder.view.adapters.MovieCardAdapter
import com.andarb.movietinder.viewmodel.MainViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        lifecycleScope.launch {
            sharedViewModel.remoteMovies.collectLatest { adapter.submitData(it) }
        }

        return binding.root
    }

    /** Saves user selection on swipe */
    override fun onCardSwiped(direction: Direction?, swipedPosition: Int) {
        val movie = adapter.peek(swipedPosition)
        val isLiked = direction == Direction.Right

        sharedViewModel.saveMovie(movie, isLiked)

        // Proceed to results screen after reaching the end of list
        if (swipedPosition == 4 && findNavController().currentDestination?.id == R.id.selectionFragmentNav) { // TODO set sharedpreference
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