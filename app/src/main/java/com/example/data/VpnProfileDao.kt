package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnProfileDao {
    @Query("SELECT * FROM vpn_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<VpnProfile>>

    @Query("SELECT * FROM vpn_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Int): VpnProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VpnProfile): Long

    @Update
    suspend fun updateProfile(profile: VpnProfile)

    @Query("DELETE FROM vpn_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Int)
}
