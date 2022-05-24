package com.jyotimoykashyap.dragspring.repository

import com.jyotimoykashyap.dragspring.api.RetrofitInstance

class RestRepository {
    suspend fun getSuccessCase() = RetrofitInstance.api.getSuccessCase()
    suspend fun getFailureCase() = RetrofitInstance.api.getFailureCase()
}