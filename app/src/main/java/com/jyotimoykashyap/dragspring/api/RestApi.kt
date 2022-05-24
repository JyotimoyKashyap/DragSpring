package com.jyotimoykashyap.dragspring.api

import com.jyotimoykashyap.dragspring.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface RestApi {

    @GET("success_case")
    suspend fun getSuccessCase() : Response<ApiResponse>

    @GET("failure_case")
    suspend fun getFailureCase() : Response<ApiResponse>

}