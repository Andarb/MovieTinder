package com.andarb.movietinder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andarb.movietinder.model.Endpoint
import com.andarb.movietinder.model.Endpoints
import com.andarb.movietinder.model.remote.RemoteEndpoint
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream


const val POSTER_URL = "https://image.tmdb.org/t/p/w500"

/** Allows different classes to use same Diffutil functionality as long as they have an [id] field */
interface DiffutilComparison {
    val id: Any
}

/** Downloads, saves to internal storage and displays the movie poster */
fun ImageView.download(imagePath: String?, fileId: Int) {
    val imageView = this

    Glide.with(imageView).asBitmap().load(POSTER_URL + imagePath)
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

/** Removes an element from the endpoint list and notifies observer */
fun MutableLiveData<Endpoints>.removeElement(elementId: String) {
    val oldList = this.value
    val oldEndpoints = oldList?.endpoints

    if (!oldEndpoints.isNullOrEmpty()) {
        val index = oldEndpoints.indexOfFirst { it.id == elementId }

        if (index != -1) {
            oldEndpoints.removeAt(index)
            this.value = oldList
        }
    } else {
        this.value?.endpoints = mutableListOf()
        this.value = oldList
    }
}

/** Adds an element to the endpoint list and notifies observer */
fun MutableLiveData<Endpoints>.addElement(element: Endpoint) {
    val oldList = this.value
    val oldEndpoints = oldList?.endpoints

    oldEndpoints?.add(element)
    this.value = oldList
}

/** Marks an endpoint in the list as connected and notifies observer */
fun MutableLiveData<Endpoints>.markConnected(elementId: String) {
    val oldList = this.value
    val oldEndpoints = oldList?.endpoints

    if (!oldEndpoints.isNullOrEmpty()) {
        val index = oldEndpoints.indexOfFirst { it.id == elementId }

        if (index != -1) {
            RemoteEndpoint.apply {
                isConnected = true
                deviceId = elementId
                deviceName = oldEndpoints[index].name
            }
            this.value = oldList
        }
    }
}