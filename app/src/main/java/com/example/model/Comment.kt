package com.example.model

import com.google.firebase.Timestamp

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val anonymousName: String = "Anonymous",
    val anonymousAvatar: String = "mask",
    val content: String = "",
    val createdAt: Timestamp? = null
)
