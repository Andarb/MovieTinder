package com.andarb.movietinder.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ItemMovieCardBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.download

/**
 * Binds downloaded movie details for user to choose from.
 */
class MovieCardAdapter :
    PagingDataAdapter<Movie, MovieCardAdapter.MovieViewHolder>(Comparator) {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMovieCardBinding.bind(itemView)

        fun bind(item: Movie?) {
            item?.let {
                with(binding) {
                    imageCardPoster.download(it.posterUrl, it.id)
                    textCardRating.text = it.rating.toString()
                    textCardDate.text = it.date
                    textCardOverview.text = it.overview
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)

        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object Comparator : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }
}