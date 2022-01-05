package com.andarb.movietinder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.view.adapters.EndpointAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream


private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/"
private const val POSTER_SIZE = "w500" // applicable sizes "w92", "w154", "w185", "w342" and "w780"

/** Allows different classes to use same Diffutil functionality as long as they have an [id] field */
interface DiffutilComparison {
    val id: Any
}

/** Downloads, saves to internal storage and displays the movie poster */
fun ImageView.download(imagePath: String?, fileId: Int) {
    val imageView = this

    Glide.with(imageView).asBitmap().load(POSTER_BASE_URL + POSTER_SIZE + imagePath)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                imageView.setImageBitmap(resource)

                val outputStream: FileOutputStream
                try {
                    outputStream = context.openFileOutput(fileId.toString(), Context.MODE_PRIVATE)
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}

/** Loads poster from internal storage if available, otherwise downloads it */
fun ImageView.load(imagePath: String?, fileId: Int) {
    val filePath = File(context.filesDir, fileId.toString()).absolutePath
    val bitmap = BitmapFactory.decodeFile(filePath)

    if (bitmap != null) this.setImageBitmap(bitmap) else this.download(imagePath, fileId)
}

/**
 * Confirms the LiveData object contains a value.
 * Checks if the provided element in the list exists.
 * Finally, applies a passed on lambda onto the confirmed non-null element.
 */
fun LiveData<List<Movie>>.checkAndRun(index: Int, action: (Movie) -> Unit) {
    val item = this.value?.getOrNull(index)

    item?.let { action(it) }
}


/** Implements Diffutil logic for RecyclerView adapters */
fun <T> RecyclerView.Adapter<*>.notifyChange(
    oldList: List<T>,
    newList: List<T>
) where T : DiffutilComparison {
    val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    })

    diff.dispatchUpdatesTo(this)
}

/** Removes an element from the endpoint list and notifies the adapter of the change */
fun EndpointAdapter.removeElement(elementId: String) {
    val index = this.items.indexOfFirst { it.id == elementId }


    if (index != -1) {
        this.items[index].isConnected = false
        this.items.removeAt(index)
        this.notifyItemRemoved(index)
    }
}

/** Marks an endpoint in the list as connected and notifies the adapter of the change */
fun EndpointAdapter.markAsConnected(elementId: String) {
    val index = this.items.indexOfFirst { it.id == elementId }

    if (index != -1) {
        this.items[index].isConnected = true
        this.notifyItemChanged(index)
    }
}