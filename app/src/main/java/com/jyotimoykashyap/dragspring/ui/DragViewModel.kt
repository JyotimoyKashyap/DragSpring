package com.jyotimoykashyap.dragspring.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyotimoykashyap.dragspring.model.ApiResponse
import com.jyotimoykashyap.dragspring.repository.RestRepository
import com.jyotimoykashyap.dragspring.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class DragViewModel(
    val restRepository: RestRepository
) : ViewModel(){

    val case: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()


    fun getSuccessCase() = viewModelScope.launch {
        case.postValue(Resource.Loading())

        // we then make the api call
        val response = restRepository.getSuccessCase()
        case.postValue(handleResponse(response))
    }

    fun getFailureCase() = viewModelScope.launch {
        case.postValue(Resource.Loading())

        // we then make the api call
        val response = restRepository.getFailureCase()
        case.postValue(handleResponse(response))
    }



    private fun handleResponse(response: Response<ApiResponse>) : Resource<ApiResponse>{
        if(response.isSuccessful){
            response.body()?.let {
                return Resource.Success(it)
            }
        }

        return Resource.Error(response.message())
    }

}