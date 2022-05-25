package com.jyotimoykashyap.dragspring.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyotimoykashyap.dragspring.model.ApiResponse
import com.jyotimoykashyap.dragspring.repository.RestRepository
import com.jyotimoykashyap.dragspring.util.Resource
import kotlinx.coroutines.delay
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
    private var originX: Float? = null
    private var originY: Float? = null

    // animation variables
    private lateinit var ballAnimY: SpringAnimation
    private lateinit var viewSlideDown: ObjectAnimator

    init {

        view.doOnLayout {
            originX = view.x
            originY = view.y
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

    @SuppressLint("ClickableViewAccessibility", "Recycle")
    fun detectDragOnView(dropView: View, screenHeight: Float) = viewModelScope.launch {
        startX = view.x
        startY = view.y

        // set object animators
        val credSlideY = ObjectAnimator.ofFloat(view, "translationY", screenHeight+500f)
            .apply {
                interpolator = FastOutSlowInInterpolator()
                duration = 600
            }
        viewSlideDown = ObjectAnimator.ofFloat(dropView, "translationY", screenHeight+500f)
            .apply {
                interpolator = FastOutSlowInInterpolator()
                duration = 600
            }

        view.setOnTouchListener{_, event ->
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y

                    // TODO: also vibrate the button

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



                        // after dropping it I have to animate them to slide down so as the loader is visible
                        Handler(Looper.getMainLooper()).postDelayed({
                            view.visibility = View.INVISIBLE
                            viewSlideDown.start()
                        }, 600)


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

    fun reverseOnFailure(dragView: View) = viewModelScope.launch {
        // get the cred button to the previous location
        Handler(Looper.getMainLooper()).postDelayed({
            dragView.visibility = View.VISIBLE
            view.x = originX!!
            view.y = originY!!

            // animate two properties, alpha and scale in x and y
            val fadeIn = ObjectAnimator.ofFloat(dragView, "alpha", 0f, 1f).apply {
                duration = ANIMATION_DURATION - 400
                interpolator = FastOutSlowInInterpolator()
                addListener (object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        dragView.visibility = View.VISIBLE
                    }
                })
            }

            // animate the scale
            val scaleX = ObjectAnimator.ofFloat(dragView, "scaleX", 0f, 1f).apply {
                duration = ANIMATION_DURATION
                interpolator = FastOutSlowInInterpolator()
            }

            val scaleY = ObjectAnimator.ofFloat(dragView, "scaleY", 0f, 1f).apply {
                duration = ANIMATION_DURATION
                interpolator = FastOutSlowInInterpolator()
            }

            AnimatorSet().apply {
                playTogether(fadeIn, scaleX, scaleY)
                start()
            }

            viewSlideDown.reverse()

        }, ANIMATION_DURATION)
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
                Log.d("networkcall", it.success.toString())
                return Resource.Success(it)
            }
        }

        return Resource.Error(response.message())
    }

    companion object {
        const val TAG = "dragviewmodel"
        const val ANIMATION_DURATION = 600L
    }

}