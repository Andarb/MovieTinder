package com.andarb.movietinder.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ItemMovieEntryBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.util.load
import com.andarb.movietinder.util.notifyChange
import kotlin.properties.Delegates

/**
 * Binds movie details previously saved in db.
 */
class HistoryAdapter(private val itemClickListener: (Movie, ClickType) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.ListViewHolder>() {

    var items: List<Movie> by Delegates.observable(emptyList()) { _, oldList, newList ->
        notifyChange(oldList, newList)
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMovieEntryBinding.bind(itemView)

        fun bind(item: Movie, clickListener: (Movie, ClickType) -> Unit) {
            with(binding) {
                imageEntryLike.setOnClickListener { clickListener(item, ClickType.LIKE) }
                imageEntryDelete.setOnClickListener { clickListener(item, ClickType.DELETE) }
                imageEntryPoster.load(item.posterUrl, item.id)
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
        holder.bind(items[position], itemClickListener)
    }

    override fun getItemCount() = items.size
}