package com.jyotimoykashyap.dragspring.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
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
    val restRepository: RestRepository, val view: View
) : ViewModel(){

    val case: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val isDropped: MutableLiveData<Boolean> = MutableLiveData(false)

    // variables for coordinates
    private var startX : Float? = null
    private var startY : Float? = null

    // animation variables
    private lateinit var ballAnimY: SpringAnimation

    init {
        view.doOnLayout {
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

    @SuppressLint("ClickableViewAccessibility")
    fun detectDragOnView(dropView: View) = viewModelScope.launch {
        startX = view.x
        startY = view.y

        view.setOnTouchListener{_, event ->
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y

                    ballAnimY.cancel()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    view.y += event.y - startY!!
                    true
                }
                MotionEvent.ACTION_UP -> {
                    Log.i(TAG, "starty : $startY\nviewy : ${view.y}" )
                    // start the animation
                    if(dropView.y + 150f > view.y &&
                        dropView.y - 150f < view.y){

                        // fling to the drop position
                        view.y = dropView.y
                        isDropped.postValue(true)

                        Toast.makeText(view.context, "Reached" , Toast.LENGTH_SHORT).show()

                    }else{
                        isDropped.postValue(false)
                        ballAnimY.start()
                    }

                    true
                }
                else -> false
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