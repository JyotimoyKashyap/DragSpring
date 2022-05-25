package com.jyotimoykashyap.dragspring

import android.os.Bundle
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

        viewModel.detectDragOnView(binding.dropView)

        viewModel.isDropped.observe(this, Observer {
            when(it) {
                true -> {
                    // make the api call
                    viewModel.getSuccessCase()
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
                    Toast.makeText(this, "Something went wrong" , Toast.LENGTH_SHORT).show()
                }
            }
        })




    }

    companion object{
        const val TAG = "debugmode"
    }

}