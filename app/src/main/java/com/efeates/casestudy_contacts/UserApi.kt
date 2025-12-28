package com.efeates.casestudy_contacts

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("api/User/GetAll")
    suspend fun getAllUsers(
        @Header("ApiKey") apiKey: String = "31e15a54-3c8a-4068-a4c0-8e830d34ff9c"
    ): Response<BaseResponse<UserListResponse>>

    @GET("api/User/{id}")
    suspend fun getUser(
        @Path("id") id: String,
        @Header("ApiKey") apiKey: String = "31e15a54-3c8a-4068-a4c0-8e830d34ff9c"
    ): Response<BaseResponse<UserResponse>>

    @POST("api/User")
    suspend fun createUser(
        @Body request: CreateUserRequest,
        @Header("ApiKey") apiKey: String = "31e15a54-3c8a-4068-a4c0-8e830d34ff9c"
    ): Response<BaseResponse<UserResponse>>

    @PUT("api/User/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: CreateUserRequest,
        @Header("ApiKey") apiKey: String = "31e15a54-3c8a-4068-a4c0-8e830d34ff9c"
    ): Response<BaseResponse<UserResponse>>

    @DELETE("api/User/{id}")
    suspend fun deleteUser(
        @Path("id") id: String,
        @Header("ApiKey") apiKey: String = "31e15a54-3c8a-4068-a4c0-8e830d34ff9c"
    ): Response<BaseResponse<Unit>>

    @Multipart
    @POST("api/User/UploadImage")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Header("ApiKey") apiKey: String = "31e15a54-3c8a-4068-a4c0-8e830d34ff9c"
    ): Response<BaseResponse<UploadImageResponse>>
}
