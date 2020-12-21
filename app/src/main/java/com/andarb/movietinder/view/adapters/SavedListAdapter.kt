package com.andarb.movietinder.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ItemMovieEntryBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.load
import kotlin.properties.Delegates

/**
 * Binds movie details in RecyclerView.
 */
class SavedListAdapter : RecyclerView.Adapter<SavedListAdapter.ListViewHolder>() {

    var items: List<Movie> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMovieEntryBinding.bind(itemView)

        fun bind(item: Movie) {
            with(binding) {
                imageEntryPoster.load(item.posterUrl)
                textEntryTitle.text = item.title
                textEntryRating.text = item.rating.toString()
                textEntryDate.text = item.date
                textEntryOverview.text = item.overview
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_entry, parent, false)

        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}