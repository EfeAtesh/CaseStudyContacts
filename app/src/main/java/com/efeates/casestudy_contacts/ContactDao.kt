package com.efeates.casestudy_contacts

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY firstName ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)
    
    @Query("SELECT * FROM contacts WHERE firstName LIKE :searchQuery OR lastName LIKE :searchQuery")
    fun searchContacts(searchQuery: String): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE remoteId = :remoteId")
    suspend fun getContactByRemoteId(remoteId: String): Contact?
}