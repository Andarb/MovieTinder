package com.andarb.movietinder.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ItemMovieCardBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.download
import kotlin.properties.Delegates

/**
 * Binds movie details in CardStackView.
 */
class MovieCardAdapter :
    RecyclerView.Adapter<MovieCardAdapter.MovieViewHolder>() {

    var items: List<Movie> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMovieCardBinding.bind(itemView)

        fun bind(item: Movie) {
            with(binding) {
                imageCardPoster.download(item.posterUrl, item.id)
                textCardRating.text = item.rating.toString()
                textCardDate.text = item.date
                textCardOverview.text = item.overview
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)

        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}