package com.example.model

import com.google.firebase.Timestamp

data class Report(
    val reportId: String = "",
    val reporterId: String = "",
    val targetType: String = "post",
    val targetId: String = "",
    val reason: String = "",
    val createdAt: Timestamp? = null
)
