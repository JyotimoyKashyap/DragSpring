package com.jyotimoykashyap.dragspring

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.view.WindowCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jyotimoykashyap.dragspring.databinding.ActivityMainBinding
import com.jyotimoykashyap.dragspring.repository.RestRepository
import com.jyotimoykashyap.dragspring.ui.DragViewModel
import com.jyotimoykashyap.dragspring.ui.DragViewModelProviderFactory
import com.jyotimoykashyap.dragspring.util.Resource

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DragViewModel

    var firstTimeOpen : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the original coordinates


        val repository = RestRepository()
        val viewModelProviderFactory = DragViewModelProviderFactory(repository, binding.dragView)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)
            .get(DragViewModel::class.java)

        Log.i("screen" , "display height: ")
        viewModel.detectDragOnView(binding.dropView, resources.displayMetrics.heightPixels.toFloat())

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
                        binding.caseTextview.text = if(it.success) "success" else "failure"
                        viewModel.animateOnSuccess(
                            binding.caseCard,
                            resources.displayMetrics.heightPixels.toFloat() - 200f,
                            binding.constraintLayout
                        )
                    }
                }
                is Resource.Loading -> {
                    // implement loader here
                }
                is Resource.Error -> {
                    // failure case here
                    binding.caseTextview.text = "failure"
                    // reverse all the animations
                    viewModel.reverseOnFailure(binding.dragView)

                }
            }
        })

        // handle switch material
        binding.switchMaterial.setOnCheckedChangeListener{buttonView, isChecked ->
            when(isChecked) {
                true -> {
                    // change color of the thumb
                }
                false -> {
                    // change color of the thumb
                }
                else -> { }
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