package com.example.model

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val authorId: String = "",
    val anonymousName: String = "Anonymous",
    val anonymousAvatar: String = "mask",
    val category: String = "Confession",
    val content: String = "",
    val contentWarning: String = "",
    val commentsEnabled: Boolean = true,
    val reactionsEnabled: Boolean = true,
    val reactionCount: Int = 0,
    val commentCount: Int = 0,
    val saveCount: Int = 0,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
