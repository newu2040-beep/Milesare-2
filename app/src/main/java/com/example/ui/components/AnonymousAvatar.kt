package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnonymousAvatar(
    avatarKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val (icon, gradient) = when (avatarKey.lowercase()) {
        "ghost" -> Pair(
            Icons.Default.Psychology,
            listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
        )
        "star" -> Pair(
            Icons.Default.Star,
            listOf(Color(0xFFF59E0B), Color(0xFFD97706))
        )
        "moon" -> Pair(
            Icons.Default.Bedtime,
            listOf(Color(0xFF6366F1), Color(0xFF4338CA))
        )
        "sparkles" -> Pair(
            Icons.Default.AutoAwesome,
            listOf(Color(0xFFEC4899), Color(0xFFBE185D))
        )
        "bolt" -> Pair(
            Icons.Default.Bolt,
            listOf(Color(0xFF06B6D4), Color(0xFF0891B2))
        )
        "cloud" -> Pair(
            Icons.Default.Cloud,
            listOf(Color(0xFF14B8A6), Color(0xFF0F766E))
        )
        "feather" -> Pair(
            Icons.Default.Favorite,
            listOf(Color(0xFFF43F5E), Color(0xFFE11D48))
        )
        else -> Pair( // "mask" or fallback
            Icons.Default.VisibilityOff,
            listOf(Color(0xFFA855F7), Color(0xFF7E22CE))
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Anonymous Avatar",
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
