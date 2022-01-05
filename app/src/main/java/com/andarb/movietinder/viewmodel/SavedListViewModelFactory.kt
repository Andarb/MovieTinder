package com.andarb.movietinder.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * ViewModelFactory for SavedListViewModel.
 * Allows the passing of an extra boolean parameter on initialization of the ViewModel.
 */
class SavedListViewModelFactory(
    private val application: Application,
    private val isLiked: Boolean
) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedListViewModel(application, isLiked) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}