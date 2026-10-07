package com.example.model

enum class ReactionType(val key: String, val emoji: String, val label: String) {
    LOVE("love", "❤️", "Love"),
    RELATABLE("relatable", "💭", "Relatable"),
    FUNNY("funny", "😂", "Funny"),
    SAD("sad", "😢", "Sad"),
    WOW("wow", "😮", "Wow"),
    ANGRY("angry", "😡", "Angry");

    companion object {
        fun fromKey(key: String): ReactionType {
            return entries.firstOrNull { it.key == key } ?: LOVE
        }
    }
}
