package com.jyotimoykashyap.dragspring.ui


import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jyotimoykashyap.dragspring.repository.RestRepository

class DragViewModelProviderFactory(val restRepository: RestRepository, val view: View) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DragViewModel(restRepository = restRepository, view = view) as T
    }
}