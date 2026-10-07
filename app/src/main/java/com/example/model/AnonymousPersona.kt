package com.example.model

import kotlin.random.Random

data class AnonymousPersona(
    val alias: String,
    val avatarKey: String
) {
    companion object {
        private val ADJECTIVES = listOf(
            "Midnight", "Velvet", "Silent", "Hidden", "Shadow", "Neon", "Cosmic",
            "Mystic", "Whispering", "Electric", "Secret", "Starlit", "Echoing",
            "Drifting", "Amber", "Cyan", "Lunar", "Solar", "Ghostly", "Phantom",
            "Crimson", "Violet", "Quiet", "Fading", "Astral", "Obscure"
        )

        private val NOUNS = listOf(
            "Soul", "Voice", "Shadow", "Wanderer", "Echo", "Seeker", "Dreamer",
            "Spirit", "Phantom", "Voyager", "Nomad", "Observer", "Whisper",
            "Phoenix", "Sparrow", "Silhouette", "Traveler", "Thinker", "Mist",
            "Stranger", "Poet", "Specter"
        )

        val AVATAR_KEYS = listOf(
            "mask", "ghost", "star", "moon", "sparkles", "bolt", "feather", "cloud"
        )

        fun generateRandom(): AnonymousPersona {
            val adj = ADJECTIVES[Random.nextInt(ADJECTIVES.size)]
            val noun = NOUNS[Random.nextInt(NOUNS.size)]
            val number = Random.nextInt(1000, 9999)
            val avatar = AVATAR_KEYS[Random.nextInt(AVATAR_KEYS.size)]
            return AnonymousPersona(
                alias = "$adj $noun #$number",
                avatarKey = avatar
            )
        }
    }
}
