package com.jyotimoykashyap.dragspring

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = RestRepository()
        val viewModelProviderFactory = DragViewModelProviderFactory(repository, binding.dragView)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)
            .get(DragViewModel::class.java)

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
                    }
                }
                is Resource.Loading -> {
                    // implement loader here
                }
                is Resource.Error -> {
                    // failure case here
                    binding.caseTextview.text = "failure"
                    // reverse all the animations

                }
            }
        })

        // handle switch material
        binding.switchMaterial.setOnCheckedChangeListener{buttonView, isChecked ->
            when(isChecked) {
                true -> {
                    // change the network call
                }
                false -> {
                    // change the network call
                }
                else -> { }
            }

        }


    }

    fun makeApiCall() {
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