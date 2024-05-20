package com.andarb.movietinder.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ItemMatchBinding
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.util.load
import com.andarb.movietinder.util.notifyChange
import kotlin.properties.Delegates

/**
 * Binds details of matched (between 'Nearby' devices) movies.
 */
class MatchesAdapter(private val itemClickListener: (Movie, ClickType) -> Unit) :
    RecyclerView.Adapter<MatchesAdapter.ListViewHolder>() {

    var items: List<Movie> by Delegates.observable(emptyList()) { _, oldList, newList ->
        notifyChange(oldList, newList)
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMatchBinding.bind(itemView)

        fun bind(item: Movie, clickListener: (Movie, ClickType) -> Unit) {
            with(binding) {
                imageMoviePoster.setOnClickListener { clickListener(item, ClickType.LIKE) }
                imageMoviePoster.load(item.posterUrl, item.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)

        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(items[position], itemClickListener)
    }

    override fun getItemCount() = items.size
}