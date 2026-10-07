package com.example.model

import com.google.firebase.Timestamp

data class Reaction(
    val reactionId: String = "",
    val postId: String = "",
    val userId: String = "",
    val reactionType: String = "love",
    val createdAt: Timestamp? = null
)
