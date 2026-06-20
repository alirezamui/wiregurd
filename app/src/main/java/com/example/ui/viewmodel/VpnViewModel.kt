package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.security.CryptoUtils
import com.example.ui.translation.AppLanguage
import com.example.vpn.ConnectionState
import com.example.vpn.NetworkDiagnosticInfo
import com.example.vpn.VpnManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VpnViewModel(private val repository: VpnRepository) : ViewModel() {

    val profiles: StateFlow<List<VpnProfile>> = repository.allProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedProfile = MutableStateFlow<VpnProfile?>(null)
    val selectedProfile = _selectedProfile.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = VpnManager.connectionState
    val networkInfo: StateFlow<NetworkDiagnosticInfo> = VpnManager.networkInfo
    val errorState: StateFlow<String?> = VpnManager.errorFlow

    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage = _appLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true) // Modern Slate Dark theme by default
    val isDarkMode = _isDarkMode.asStateFlow()
    
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    init {
        // Automatically select the first profile as default if none selected
        viewModelScope.launch {
            profiles.collect { list ->
                if (list.isNotEmpty() && _selectedProfile.value == null) {
                    _selectedProfile.value = list.first()
                }
            }
        }
    }

    fun selectProfile(profile: VpnProfile) {
        _selectedProfile.value = profile
    }

    fun toggleLanguage() {
        _appLanguage.value = if (_appLanguage.value == AppLanguage.ENGLISH) {
            AppLanguage.PERSIAN
        } else {
            AppLanguage.ENGLISH
        }
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun importConfig(name: String, content: String): Boolean {
        return try {
            val trimmedContent = content.trim()
            // Validate configuration using public validation engine as requested
            VpnConfigParser.parse(trimmedContent)
            
            // Secure configuration by encrypting with AES
            val encrypted = CryptoUtils.encrypt(trimmedContent)
            
            val profile = VpnProfile(
                name = name.ifBlank { "Client Profile" },
                encryptedConfig = encrypted
            )
            
            viewModelScope.launch {
                val newId = repository.insertProfile(profile)
                // Select newly imported profile
                _selectedProfile.value = profile.copy(id = newId.toInt())
                _toastEvent.emit("import_success")
            }
            true
        } catch (e: Exception) {
            VpnManager.setError(e.localizedMessage ?: "Invalid configuration file.")
            false
        }
    }

    fun renameProfile(id: Int, newName: String) {
        viewModelScope.launch {
            val profile = repository.getProfileById(id)
            if (profile != null) {
                val updated = profile.copy(name = newName)
                repository.updateProfile(updated)
                if (_selectedProfile.value?.id == id) {
                    _selectedProfile.value = updated
                }
            }
        }
    }

    fun deleteProfile(id: Int) {
        viewModelScope.launch {
            repository.deleteProfile(id)
            if (_selectedProfile.value?.id == id) {
                _selectedProfile.value = null
            }
        }
    }

    fun connectVpn(context: Context) {
        val current = _selectedProfile.value
        if (current == null) {
            VpnManager.setError("No profile selected.")
            return
        }
        VpnManager.startVpn(context, current)
    }

    fun disconnectVpn(context: Context) {
        VpnManager.stopVpn(context)
    }

    fun loadSampleConfig() {
        val sampleConf = """
            [Interface]
            PrivateKey = mE6b85uUvxO9+4vG/JID+Pz87fQ0S9m3o+nOnKxTrHQ=
            Address = 10.254.0.2/32, fd00::2/128
            DNS = 1.1.1.1, 8.8.8.8
            MTU = 1420

            [Peer]
            PublicKey = SSkT1M/uW3oWwL9T7V+nOnKxTrHQYID+Pz87fQ0S9m3=
            Endpoint = 185.112.33.24:51820
            AllowedIPs = 0.0.0.0/0
            PersistentKeepalive = 25
        """.trimIndent()
        
        importConfig("Iran Telecommunication Sample", sampleConf)
    }

    // Factory Class pattern as directed by repository skills
    class Factory(private val repository: VpnRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VpnViewModel::class.java)) {
                return VpnViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
