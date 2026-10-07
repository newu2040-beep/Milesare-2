package com.example.ui.viewmodel

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AnonymousPersona
import com.example.model.UserProfile
import com.example.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    val userProfile: StateFlow<UserProfile?> = authRepository.observeUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun signInWithGoogle(activity: Activity, credentialManager: CredentialManager) {
        _isLoading.value = true
        _authError.value = null
        authRepository.onGoogleSignInClicked(
            activity = activity,
            credentialManager = credentialManager,
            onAuthSuccess = {
                _isLoading.value = false
            },
            onAuthError = { errorMsg ->
                _isLoading.value = false
                _authError.value = errorMsg
            },
            scope = viewModelScope,
            onAuthCancelled = {
                _isLoading.value = false
            }
        )
    }

    fun signOut(credentialManager: CredentialManager, onSignOutComplete: () -> Unit) {
        authRepository.signOut(credentialManager, onSignOutComplete, viewModelScope)
    }

    fun updatePersona(alias: String, avatarKey: String, bio: String) {
        viewModelScope.launch {
            authRepository.updateAnonymousPersona(alias, avatarKey, bio)
        }
    }

    fun rerollPersona() {
        val newPersona = AnonymousPersona.generateRandom()
        viewModelScope.launch {
            authRepository.updateAnonymousPersona(
                alias = newPersona.alias,
                avatarKey = newPersona.avatarKey,
                bio = userProfile.value?.bio ?: ""
            )
        }
    }

    fun clearError() {
        _authError.value = null
    }
}
