package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.GoldBookkeepingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null
)

class AuthViewModel(private val repository: GoldBookkeepingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _uiState.map { it.currentUser }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val allUsers: Flow<List<UserEntity>> = repository.allUsers

    init {
        // Ensure default users exist and init
        ensureDefaultUsers()
    }

    private fun ensureDefaultUsers() {
        viewModelScope.launch {
            var ferry = repository.loginByIdentifier("ferrynani", "833273")
            if (ferry == null) {
                val ferryUser = UserEntity(
                    username = "ferrynani",
                    fullName = "Ferry Nani (Pemilik & Pengelola)",
                    email = "ferry.nani@gmail.com",
                    phoneWhatsapp = "08124180909",
                    password = "833273",
                    role = UserRole.OWNER,
                    securityType = "PIN",
                    securityPinOrPattern = "833273"
                )
                repository.insertUser(ferryUser)
                ferry = repository.loginByIdentifier("ferrynani", "833273")
            }
            if (_uiState.value.currentUser == null && ferry != null) {
                _uiState.value = _uiState.value.copy(currentUser = ferry)
            }
        }
    }

    fun login(identifier: String, password: String, onComplete: (Boolean) -> Unit) {
        if (identifier.isBlank()) {
            _uiState.value = _uiState.value.copy(loginError = "Silakan masukkan Username, Email, atau No. HP")
            onComplete(false)
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(loginError = "Silakan masukkan kata sandi")
            onComplete(false)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, loginError = null)
        viewModelScope.launch {
            val user = repository.loginByIdentifier(identifier.trim(), password)
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isLoading = false,
                    loginError = null
                )
                repository.logActivity(user.username, user.role.name, "LOGIN", null, "Pengguna berhasil masuk melalui identifier ($identifier)")
                onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginError = "Kredensial atau kata sandi tidak cocok. Anda dapat masuk menggunakan Username, Email, atau No. HP yang terdaftar."
                )
                onComplete(false)
            }
        }
    }

    fun clearLoginError() {
        _uiState.value = _uiState.value.copy(loginError = null)
    }

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            val username = when (role) {
                UserRole.OWNER -> "owner"
                UserRole.ADMIN -> "admin"
                UserRole.OPERATOR -> "operator"
                UserRole.VIEWER -> "viewer"
            }
            var user = repository.login(username, "123")
            if (user == null && role == UserRole.VIEWER) {
                val viewerUser = UserEntity(
                    username = "viewer",
                    fullName = "Pengawas / Tamu (Hanya Melihat)",
                    email = "viewer@emasjaya.com",
                    phoneWhatsapp = "0812-9988-7766",
                    password = "123",
                    role = UserRole.VIEWER
                )
                val id = repository.insertUser(viewerUser)
                user = viewerUser.copy(id = id)
            }
            if (user != null) {
                _uiState.value = _uiState.value.copy(currentUser = user)
                repository.logActivity(user.username, user.role.name, "SWITCH_USER", null, "Beralih role pengguna ke ${user.role.name}")
            }
        }
    }

    fun updateProfile(
        fullName: String,
        email: String,
        phoneWhatsapp: String,
        photoUri: String?,
        actor: String
    ) {
        val current = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val updated = current.copy(
                fullName = fullName.trim(),
                email = email.trim(),
                phoneWhatsapp = phoneWhatsapp.trim(),
                photoUri = photoUri
            )
            repository.updateUser(updated, actor)
            _uiState.value = _uiState.value.copy(currentUser = updated)
        }
    }

    fun updateSecurity(
        securityType: String,
        pinOrPattern: String,
        actor: String
    ) {
        val current = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val updated = current.copy(
                securityType = securityType,
                securityPinOrPattern = pinOrPattern
            )
            repository.updateUser(updated, actor)
            _uiState.value = _uiState.value.copy(currentUser = updated)
        }
    }

    fun changePassword(targetUser: UserEntity, newPassword: String, actor: String) {
        viewModelScope.launch {
            val updated = targetUser.copy(password = newPassword)
            repository.updateUser(updated, actor)
            if (_uiState.value.currentUser?.id == targetUser.id) {
                _uiState.value = _uiState.value.copy(currentUser = updated)
            }
        }
    }

    fun addUser(
        username: String,
        fullName: String,
        role: UserRole,
        pass: String,
        email: String = "",
        phoneWhatsapp: String = "",
        actor: String
    ) {
        viewModelScope.launch {
            val newUser = UserEntity(
                username = username.trim().lowercase(),
                fullName = fullName.trim(),
                email = email.ifBlank { "${username.trim().lowercase()}@emasjaya.com" },
                phoneWhatsapp = phoneWhatsapp.ifBlank { "0812-3456-7890" },
                password = pass,
                role = role,
                isActive = true
            )
            repository.insertUser(newUser)
        }
    }

    fun toggleUserStatus(user: UserEntity, actor: String) {
        viewModelScope.launch {
            val updated = user.copy(isActive = !user.isActive)
            repository.updateUser(updated, actor)
        }
    }

    fun logout() {
        val prevUser = _uiState.value.currentUser
        if (prevUser != null) {
            viewModelScope.launch {
                repository.logActivity(prevUser.username, prevUser.role.name, "LOGOUT", null, "Pengguna keluar")
            }
        }
        _uiState.value = AuthUiState()
    }
}
