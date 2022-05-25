package com.jyotimoykashyap.dragspring.repository

import android.animation.*
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.airbnb.lottie.LottieAnimationView
import com.jyotimoykashyap.dragspring.ui.DragViewModel

/**
 * Repository class for animations
 */
class AnimRepository(
    val view: View,
    val dropView: View,
    val screenHeight: Float
) {

    // variables for coordinates
    private var startX : Float? = null
    private var startY : Float? = null
    private var originX: Float? = null
    private var originY: Float? = null

    // animation variables
    private lateinit var ballAnimY: SpringAnimation
    private lateinit var expandCard: ValueAnimator
    private lateinit var translateCard: ObjectAnimator
    private lateinit var viewSlideDown: ObjectAnimator

    init {
        view.doOnLayout {
            originX = view.x
            originY = view.y
            Log.i(DragViewModel.TAG, "x : $startX\ny : $startY")

            ballAnimY = SpringAnimation(it, DynamicAnimation.TRANSLATION_Y).apply {
                spring = startY?.let { y ->
                    SpringForce(y).apply {
                        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        stiffness = SpringForce.STIFFNESS_MEDIUM
                    }
                }
            }
        }

        dropView.doOnLayout {
            viewSlideDown = ObjectAnimator.ofFloat(dropView, "translationY", screenHeight+500f)
                .apply {
                    interpolator = FastOutSlowInInterpolator()
                    duration = 600
                }
        }
    }


    // text slide up animation
    fun slideUpTextAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "translationY" , 500f, 0f)
            .apply {
                duration = 400
                interpolator = FastOutSlowInInterpolator()
                start()
            }
    }

    // animation on success
    fun animateOnSuccess(
        cardView: View,
        height: Float,
        constraintLayout: ConstraintLayout
    ) {
        // translate the card such that it goes to the middle
        // while expanding its height
        expandCard = ValueAnimator.ofInt(cardView.height, height.toInt() - 300).apply {
            duration = DragViewModel.ANIMATION_DURATION - 100
            interpolator = OvershootInterpolator()
            addUpdateListener {
                val animatedValue = it.animatedValue
                cardView.layoutParams.height = animatedValue as Int
                cardView.requestLayout()
            }
        }

        // translating the card
        translateCard = ObjectAnimator.ofFloat(
            cardView,
            "y",
            cardView.y,
            (constraintLayout.y + 200)
        ).apply {
            duration = DragViewModel.ANIMATION_DURATION - 300
            interpolator = FastOutSlowInInterpolator()
        }

        AnimatorSet().apply {
            playTogether(translateCard, expandCard)
            start()
        }
    }

}