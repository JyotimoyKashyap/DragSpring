package com.jyotimoykashyap.dragspring.ui

import android.util.Log
import android.view.View
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyotimoykashyap.dragspring.model.ApiResponse
import com.jyotimoykashyap.dragspring.repository.RestRepository
import com.jyotimoykashyap.dragspring.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class DragViewModel(
    val restRepository: RestRepository, view: View
) : ViewModel(){

    val case: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()

    // variables for coordinates
    private var startX : Float? = null
    private var startY : Float? = null

    // animation variables
    lateinit var ballAnimY: SpringAnimation

    init {
        view.doOnLayout {
            // extract values of x and y when the widget is drawn on the canvas
            startX = it.x
            startY = it.y

            Log.i(TAG, "x : $startX\ny : $startY")

            ballAnimY = SpringAnimation(it, DynamicAnimation.TRANSLATION_Y).apply {
                spring = startY?.let { y ->
                    SpringForce(y).apply {
                        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        stiffness = SpringForce.STIFFNESS_MEDIUM
                    }
                }
            }
        }
    }


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

    companion object {
        const val TAG = "dragviewmodel"
    }

}