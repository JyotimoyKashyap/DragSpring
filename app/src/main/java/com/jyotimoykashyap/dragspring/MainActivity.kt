package com.jyotimoykashyap.dragspring

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jyotimoykashyap.dragspring.databinding.ActivityMainBinding
import com.jyotimoykashyap.dragspring.repository.AnimRepository
import com.jyotimoykashyap.dragspring.repository.RestRepository
import com.jyotimoykashyap.dragspring.ui.DragViewModel
import com.jyotimoykashyap.dragspring.ui.DragViewModelProviderFactory
import com.jyotimoykashyap.dragspring.util.Resource

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DragViewModel

    var needReset : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide the loader
        binding.loader.visibility = View.INVISIBLE


        // init the repository classes
        val repository = RestRepository()
        val animRepository = AnimRepository(
            binding.dragView,
            binding.dropView,
            resources.displayMetrics.heightPixels.toFloat()
        )


        val viewModelProviderFactory = DragViewModelProviderFactory(
            repository,
            binding.dragView,
            animRepository
        )

        viewModel = ViewModelProvider(this, viewModelProviderFactory)
            .get(DragViewModel::class.java)

        Log.i("screen" , "display height: ")
        viewModel.detectDragOnView(
            binding.dropView,
            resources.displayMetrics.heightPixels.toFloat(),
            baseContext.getSystemService(VIBRATOR_SERVICE) as Vibrator
        )

        viewModel.isDropped.observe(this, Observer {
            when(it) {
                true -> {
                    // make the api call
                    makeApiCall()
                    binding.caseTextview.visibility = View.VISIBLE
                }
                false -> {
                    binding.caseTextview.visibility = View.INVISIBLE
                }
                else -> { }
            }
        })

        viewModel.case.observe(this, Observer {
            when(it) {
                is Resource.Success -> {
                    it.data?.let {
                        Log.d(TAG, it.success.toString())
                        needReset = true
                        binding.caseTextview.text = if(it.success) "success" else "failure"
                        binding.switchMaterial.isEnabled = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            Toast.makeText(this, "Please click on the card to reset", Toast.LENGTH_LONG).show()
                        }, 6000)
                        viewModel.slideUpTextAnimation(binding.caseTextview)
                        viewModel.animateOnSuccess(
                            binding.caseCard,
                            resources.displayMetrics.heightPixels.toFloat() - 200f,
                            binding.constraintLayout,
                            binding.credBrand,
                            binding.splashLottie
                        )
                        binding.loader.visibility = View.INVISIBLE
                    }
                }
                is Resource.Loading -> {
                    needReset = false
                    // implement loader here
                    binding.run {
                        loader.visibility = View.VISIBLE
                        loader.playAnimation()
                    }

                }
                is Resource.Error -> {
                    // failure case here
                    Toast.makeText(this, "Something went wrong!" ,Toast.LENGTH_SHORT).show()
                    needReset = false
                    binding.caseTextview.text = "failure"
                    viewModel.slideUpTextAnimation(binding.caseTextview)
                    // reverse all the animations
                    viewModel.reverseOnFailure(binding.dragView, binding.loader)
                    binding.loader.visibility = View.INVISIBLE
                }
            }
        })

        // handle switch material
        binding.switchMaterial.setOnCheckedChangeListener{buttonView, isChecked ->
            binding.caseTextview.text = " "
        }

        binding.caseCard.setOnClickListener{
            if(needReset){
//                viewModel.reset(binding.dragView)
//                needReset = false
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }


    }

    private fun makeApiCall() {
        if(binding.switchMaterial.isChecked) {
            // failure case
            viewModel.getFailureCase()
        }else {
            // success case
            viewModel.getSuccessCase()
        }
    }

    companion object{
        const val TAG = "debugmode"
    }

}