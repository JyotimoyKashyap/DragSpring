package com.jyotimoykashyap.dragspring.repository

import android.animation.*
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
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
    private lateinit var textSlideUp: ObjectAnimator
    private lateinit var fadeIn: ObjectAnimator
    private lateinit var scaleX: ObjectAnimator
    private lateinit var scaleY: ObjectAnimator

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
        textSlideUp = ObjectAnimator.ofFloat(view, "translationY" , 500f, 0f)
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
        constraintLayout: ConstraintLayout,
        credLogo: View,
        lottieAnimationView: LottieAnimationView
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

        brandPopAnim(credLogo)

        // animation for the cred logo brand
        // animate two properties, alpha and scale in x and y
        val fadeInLottie = ObjectAnimator.ofFloat(lottieAnimationView, "alpha", 0f, 1f).apply {
            duration = DragViewModel.ANIMATION_DURATION - 400
            interpolator = FastOutSlowInInterpolator()
            addListener (object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    lottieAnimationView.visibility = View.VISIBLE
                    lottieAnimationView.playAnimation()
                }
            })
        }

        // animate the scale
        val scaleXLottie = ObjectAnimator.ofFloat(lottieAnimationView, "scaleX", 0f, 1f).apply {
            duration = DragViewModel.ANIMATION_DURATION
            interpolator = AnticipateOvershootInterpolator()
        }

        val scaleYLottie = ObjectAnimator.ofFloat(lottieAnimationView, "scaleY", 0f, 1f).apply {
            duration = DragViewModel.ANIMATION_DURATION
            interpolator = AnticipateOvershootInterpolator()
        }

        AnimatorSet().apply {
            playTogether(translateCard, expandCard)
            start()
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)

                    Handler(Looper.getMainLooper()).postDelayed({
                        // reverse the text slide
                        textSlideUp.reverse()
                        // play the scale animation for cred logo
                        AnimatorSet().apply {
                            playTogether(fadeIn, scaleX, scaleY, fadeInLottie, scaleXLottie, scaleYLottie)
                            start()
                        }
                    }, 500)
                }
            })
        }




    }

    fun brandPopAnim(view: View){
        // animation for the cred logo brand
        // animate two properties, alpha and scale in x and y
        fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = DragViewModel.ANIMATION_DURATION - 400
            interpolator = FastOutSlowInInterpolator()
            addListener (object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    view.visibility = View.VISIBLE
                }
            })
        }

        // animate the scale
        scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f).apply {
            duration = DragViewModel.ANIMATION_DURATION
            interpolator = AnticipateOvershootInterpolator()
        }

        scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f).apply {
            duration = DragViewModel.ANIMATION_DURATION
            interpolator = AnticipateOvershootInterpolator()
        }

    }

}