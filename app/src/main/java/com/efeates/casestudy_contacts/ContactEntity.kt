package com.efeates.casestudy_contacts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: String? = null,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val image: ByteArray? = null,
    val profileImageUrl: String? = null
)