package com.jyotimoykashyap.dragspring.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jyotimoykashyap.dragspring.repository.RestRepository

class DragViewModelProviderFactory(val restRepository: RestRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DragViewModel(restRepository = restRepository) as T
    }
}