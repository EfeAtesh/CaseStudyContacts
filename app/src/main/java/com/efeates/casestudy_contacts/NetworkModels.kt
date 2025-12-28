package com.efeates.casestudy_contacts

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: String?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val createdAt: String?
)

data class UserListResponse(
    val users: List<UserResponse>?
)

data class BaseResponse<T>(
    val success: Boolean,
    val messages: List<String>?,
    val data: T?,
    val status: Int
)

data class CreateUserRequest(
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val profileImageUrl: String?
)

data class UploadImageResponse(
    val imageUrl: String?
)
