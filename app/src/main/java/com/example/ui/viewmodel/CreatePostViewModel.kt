package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AnonymousPersona
import com.example.repository.ConfessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val confessionRepository: ConfessionRepository,
    initialPersona: AnonymousPersona? = null
) : ViewModel() {

    val categories = listOf(
        "Confession", "Secret", "Story", "Question", "Experience",
        "Advice", "Relationship", "Life", "Mental Thoughts", "Funny", "Mystery"
    )

    private val _currentPersona = MutableStateFlow(initialPersona ?: AnonymousPersona.generateRandom())
    val currentPersona: StateFlow<AnonymousPersona> = _currentPersona.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Confession")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _contentWarning = MutableStateFlow("")
    val contentWarning: StateFlow<String> = _contentWarning.asStateFlow()

    private val _commentsEnabled = MutableStateFlow(true)
    val commentsEnabled: StateFlow<Boolean> = _commentsEnabled.asStateFlow()

    private val _reactionsEnabled = MutableStateFlow(true)
    val reactionsEnabled: StateFlow<Boolean> = _reactionsEnabled.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setContent(text: String) {
        if (text.length <= 2000) {
            _content.value = text
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setContentWarning(warning: String) {
        if (warning.length <= 60) {
            _contentWarning.value = warning
        }
    }

    fun toggleComments() {
        _commentsEnabled.value = !_commentsEnabled.value
    }

    fun toggleReactions() {
        _reactionsEnabled.value = !_reactionsEnabled.value
    }

    fun rerollPersona() {
        _currentPersona.value = AnonymousPersona.generateRandom()
    }

    fun setCustomPersona(alias: String, avatarKey: String) {
        _currentPersona.value = AnonymousPersona(alias, avatarKey)
    }

    fun publishPost(onSuccess: (String) -> Unit) {
        val text = _content.value.trim()
        if (text.isBlank()) {
            _errorMessage.value = "Confession content cannot be empty"
            return
        }

        _isSubmitting.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = confessionRepository.createPost(
                anonymousName = _currentPersona.value.alias,
                anonymousAvatar = _currentPersona.value.avatarKey,
                category = _selectedCategory.value,
                content = text,
                contentWarning = _contentWarning.value.trim(),
                commentsEnabled = _commentsEnabled.value,
                reactionsEnabled = _reactionsEnabled.value
            )

            _isSubmitting.value = false
            if (result.isSuccess) {
                // Reset form
                _content.value = ""
                _contentWarning.value = ""
                onSuccess(result.getOrThrow())
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to post confession"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
