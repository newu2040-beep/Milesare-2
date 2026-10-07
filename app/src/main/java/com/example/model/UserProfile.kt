package com.example.model

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val anonymousName: String = "Anonymous #0000",
    val anonymousAvatar: String = "mask",
    val bio: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
