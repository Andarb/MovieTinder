package com.andarb.movietinder.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ItemEndpointBinding
import com.andarb.movietinder.model.Endpoint
import com.andarb.movietinder.util.notifyChange
import kotlin.properties.Delegates

/**
 * Binds endpoint details of found nearby devices.
 */
class EndpointAdapter(private val clickListener: (Endpoint) -> Unit) :
    RecyclerView.Adapter<EndpointAdapter.ViewHolder>() {

    var items: MutableList<Endpoint> by Delegates.observable(mutableListOf()) { _, oldList, newList ->
        notifyChange(oldList, newList)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemEndpointBinding.bind(itemView)

        fun bind(item: Endpoint) {
            with(binding) {
                textEndpointName.text = item.name
                textEndpointId.text =
                    binding.root.context.getString(R.string.formatting_id, item.id)
                if (item.isConnected) {
                    itemView.setBackgroundColor(Color.CYAN)
                    itemView.findNavController().navigate(R.id.selectionFragmentNav)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_endpoint, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { clickListener(item) }
    }

    override fun getItemCount() = items.size
}