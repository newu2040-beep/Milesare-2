package com.example.model

import com.google.firebase.Timestamp

data class SavedPost(
    val saveId: String = "",
    val userId: String = "",
    val postId: String = "",
    val savedAt: Timestamp? = null
)
