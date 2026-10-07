package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Comment
import com.example.model.Post
import com.example.model.Reaction
import com.example.model.ReactionType
import com.example.repository.ConfessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostDetailViewModel(
    val postId: String,
    private val confessionRepository: ConfessionRepository
) : ViewModel() {

    val post: StateFlow<Post?> = confessionRepository.observePost(postId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val comments: StateFlow<List<Comment>> = confessionRepository.observeComments(postId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userReaction: StateFlow<Reaction?> = confessionRepository.observeUserReaction(postId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val savedPostIds: StateFlow<Set<String>> = confessionRepository.observeSavedPostIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _commentInput = MutableStateFlow("")
    val commentInput: StateFlow<String> = _commentInput.asStateFlow()

    private val _isSubmittingComment = MutableStateFlow(false)
    val isSubmittingComment: StateFlow<Boolean> = _isSubmittingComment.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setCommentInput(text: String) {
        if (text.length <= 600) {
            _commentInput.value = text
        }
    }

    fun submitComment(anonymousName: String, anonymousAvatar: String) {
        val text = _commentInput.value.trim()
        if (text.isBlank()) return

        _isSubmittingComment.value = true
        viewModelScope.launch {
            val result = confessionRepository.addComment(
                postId = postId,
                content = text,
                anonymousName = anonymousName,
                anonymousAvatar = anonymousAvatar
            )
            _isSubmittingComment.value = false
            if (result.isSuccess) {
                _commentInput.value = ""
            } else {
                _statusMessage.value = "Failed to add comment"
            }
        }
    }

    fun toggleReaction(reactionType: ReactionType) {
        viewModelScope.launch {
            confessionRepository.toggleReaction(postId, reactionType)
        }
    }

    fun toggleSave() {
        viewModelScope.launch {
            val result = confessionRepository.toggleSavePost(postId)
            if (result.isSuccess) {
                _statusMessage.value = if (result.getOrDefault(false)) "Saved to bookmarks" else "Removed from bookmarks"
            }
        }
    }

    fun reportPost(reason: String) {
        viewModelScope.launch {
            val result = confessionRepository.submitReport("post", postId, reason)
            _statusMessage.value = if (result.isSuccess) "Report submitted for review" else "Failed to submit report"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
