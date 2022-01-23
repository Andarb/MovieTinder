package com.andarb.movietinder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.andarb.movietinder.databinding.FragmentSelectionBinding
import com.andarb.movietinder.view.adapters.MovieCardAdapter
import com.andarb.movietinder.viewmodel.MainViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectionFragment : Fragment(), CardStackListener {

    private val adapter = MovieCardAdapter()
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSelectionBinding.inflate(inflater, container, false)
        val layoutManager = CardStackLayoutManager(context, this)
        binding.cardstackMovies.layoutManager = layoutManager
        binding.cardstackMovies.adapter = adapter
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        lifecycleScope.launch {
            viewModel.remoteMovies.collectLatest { adapter.submitData(it) }
        }


        return binding.root
    }

    /** Saves user selection on swipe */
    override fun onCardSwiped(direction: Direction?, swipedPosition: Int) {
        val movie = adapter.peek(swipedPosition)
        val isLiked = direction == Direction.Right

        viewModel.saveMovie(movie, isLiked)
    }

    /** Unused implementations for CardStackView */
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}
}