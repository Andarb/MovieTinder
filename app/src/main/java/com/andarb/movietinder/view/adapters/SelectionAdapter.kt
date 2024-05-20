package com.andarb.movietinder.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ItemMovieCardBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.download
import com.andarb.movietinder.util.notifyChange
import kotlin.properties.Delegates

/**
 * Binds downloaded movie details for user to choose from.
 */
class SelectionAdapter : RecyclerView.Adapter<SelectionAdapter.ListViewHolder>() {

    var items: List<Movie> by Delegates.observable(emptyList()) { _, oldList, newList ->
        notifyChange(oldList, newList)
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)

        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}