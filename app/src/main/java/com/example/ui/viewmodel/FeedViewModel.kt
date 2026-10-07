package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Post
import com.example.model.ReactionType
import com.example.repository.ConfessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FeedTab(val label: String) {
    FOR_YOU("For You"),
    LATEST("Latest"),
    TRENDING("Trending")
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(
    private val confessionRepository: ConfessionRepository
) : ViewModel() {

    val categories = listOf(
        "All", "Confession", "Secret", "Love", "Relationships",
        "Friendship", "Family", "College", "Work", "Life",
        "Mental Thoughts", "Funny", "Mystery", "Advice"
    )

    private val _selectedTab = MutableStateFlow(FeedTab.FOR_YOU)
    val selectedTab: StateFlow<FeedTab> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _feedPosts = combine(_selectedTab, _selectedCategory) { tab, cat ->
        Pair(tab, cat)
    }.flatMapLatest { (tab, cat) ->
        val isTrending = tab == FeedTab.TRENDING
        confessionRepository.observeFeed(
            categoryFilter = if (cat == "All") null else cat,
            sortByTrending = isTrending
        )
    }

    val posts: StateFlow<List<Post>> = combine(_feedPosts, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.content.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.anonymousName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedPostIds: StateFlow<Set<String>> = confessionRepository.observeSavedPostIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun selectTab(tab: FeedTab) {
        _selectedTab.value = tab
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleReaction(postId: String, reactionType: ReactionType) {
        viewModelScope.launch {
            confessionRepository.toggleReaction(postId, reactionType)
        }
    }

    fun toggleSave(postId: String) {
        viewModelScope.launch {
            val result = confessionRepository.toggleSavePost(postId)
            if (result.isSuccess) {
                _statusMessage.value = if (result.getOrDefault(false)) "Saved to bookmarks" else "Removed from bookmarks"
            }
        }
    }

    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            val result = confessionRepository.submitReport("post", postId, reason)
            _statusMessage.value = if (result.isSuccess) "Thank you. Report received for review." else "Failed to submit report"
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = confessionRepository.deletePost(postId)
            _statusMessage.value = if (result.isSuccess) "Confession deleted" else "Could not delete confession"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
