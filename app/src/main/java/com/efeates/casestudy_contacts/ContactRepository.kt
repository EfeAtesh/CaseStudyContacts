package com.efeates.casestudy_contacts

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val contactDao: ContactDao,
    private val userApi: UserApi
) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()

    suspend fun syncWithServer() {
        try {
            val response = userApi.getAllUsers()
            if (response.isSuccessful) {
                response.body()?.data?.users?.forEach { user ->
                    val existing = contactDao.getContactByRemoteId(user.id ?: "")
                    contactDao.insertContact(
                        Contact(
                            id = existing?.id ?: 0,
                            remoteId = user.id,
                            firstName = user.firstName ?: "",
                            lastName = user.lastName ?: "",
                            phoneNumber = user.phoneNumber ?: "",
                            profileImageUrl = user.profileImageUrl,
                            image = existing?.image
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveContact(
        contact: Contact?,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        selectedImage: ByteArray?
    ): Boolean {
        var profileImageUrl = contact?.profileImageUrl

        // 1. Image Upload
        if (selectedImage != null && selectedImage != contact?.image) {
            val part = MultipartBody.Part.createFormData(
                "image", "profile.jpg",
                selectedImage.toRequestBody("image/*".toMediaTypeOrNull())
            )
            val uploadRes = userApi.uploadImage(part)
            if (uploadRes.isSuccessful) profileImageUrl = uploadRes.body()?.data?.imageUrl
        }

        // 2. Server Sync
        val req = CreateUserRequest(firstName, lastName, phoneNumber, profileImageUrl)
        val res = if (contact?.remoteId != null) {
            userApi.updateUser(contact.remoteId, req)
        } else {
            userApi.createUser(req)
        }

        // 3. Local Save
        return if (res.isSuccessful) {
            contactDao.insertContact(
                Contact(
                    id = contact?.id ?: 0,
                    remoteId = res.body()?.data?.id ?: contact?.remoteId,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    image = selectedImage,
                    profileImageUrl = res.body()?.data?.profileImageUrl ?: profileImageUrl
                )
            )
            true
        } else false
    }

    suspend fun deleteContact(contact: Contact) {
        contact.remoteId?.let { userApi.deleteUser(it) }
        contactDao.deleteContact(contact)
    }
}
