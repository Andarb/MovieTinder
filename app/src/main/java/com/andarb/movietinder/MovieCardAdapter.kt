package com.andarb.movietinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.data.model.Movie
import com.andarb.movietinder.databinding.ItemMovieCardBinding

class MovieCardAdapter(private val movies: List<Movie>) :
    RecyclerView.Adapter<MovieCardAdapter.MovieViewHolder>() {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMovieCardBinding.bind(itemView)

        fun bind(item: Movie) {
            with(binding) {
                imagePoster.load(item.posterUrl)
                textRating.text = item.rating.toString()
                textDate.text = item.date
                textOverview.text = item.overview
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)

        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount() = movies.size
}