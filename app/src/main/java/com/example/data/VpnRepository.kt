package com.example.data

import kotlinx.coroutines.flow.Flow

class VpnRepository(private val vpnProfileDao: VpnProfileDao) {
    val allProfiles: Flow<List<VpnProfile>> = vpnProfileDao.getAllProfiles()

    suspend fun getProfileById(id: Int): VpnProfile? {
        return vpnProfileDao.getProfileById(id)
    }

    suspend fun insertProfile(profile: VpnProfile): Long {
        return vpnProfileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: VpnProfile) {
        vpnProfileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(id: Int) {
        vpnProfileDao.deleteProfile(id)
    }
}
