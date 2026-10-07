package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Post
import com.example.model.UserProfile
import com.example.repository.AuthRepository
import com.example.repository.ConfessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val confessionRepository: ConfessionRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = authRepository.observeUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val myPosts: StateFlow<List<Post>> = confessionRepository.observeMyPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedPosts: StateFlow<List<Post>> = confessionRepository.observeSavedPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = confessionRepository.deletePost(postId)
            _statusMessage.value = if (result.isSuccess) "Confession deleted" else "Could not delete confession"
        }
    }

    fun updateBio(newBio: String) {
        val current = userProfile.value ?: return
        viewModelScope.launch {
            authRepository.updateAnonymousPersona(current.anonymousName, current.anonymousAvatar, newBio)
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
