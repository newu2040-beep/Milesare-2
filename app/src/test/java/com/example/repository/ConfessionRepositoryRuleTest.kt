package com.example.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.ReactionType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfessionRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createConfession_validPayload_createsDocumentAndReturnsId() = runBlocking {
    val uid = signInTestUser("whisperer@test.com")
    val repo = ConfessionRepository(firestore, auth)

    val result = repo.createPost(
      anonymousName = "Midnight Soul #999",
      anonymousAvatar = "mask",
      category = "Confession",
      content = "I secretly love midnight walks in empty parks.",
      contentWarning = "",
      commentsEnabled = true,
      reactionsEnabled = true
    )

    assertTrue(result.isSuccess)
    val postId = result.getOrThrow()
    assertNotNull(postId)
    assertTrue(postId.isNotEmpty())
  }

  @Test
  fun addComment_authenticatedUser_increasesCommentCount() = runBlocking {
    val uid = signInTestUser("commenter@test.com")
    val repo = ConfessionRepository(firestore, auth)

    val postResult = repo.createPost(
      anonymousName = "Hidden Seeker #123",
      anonymousAvatar = "star",
      category = "Life",
      content = "Sometimes I feel disconnected from everyone.",
      contentWarning = "",
      commentsEnabled = true,
      reactionsEnabled = true
    )
    val postId = postResult.getOrThrow()

    val commentResult = repo.addComment(
      postId = postId,
      content = "You are not alone in feeling that.",
      anonymousName = "Silent Echo #555",
      anonymousAvatar = "moon"
    )

    assertTrue(commentResult.isSuccess)
  }

  @Test
  fun toggleReaction_authenticatedUser_success() = runBlocking {
    val uid = signInTestUser("reactor@test.com")
    val repo = ConfessionRepository(firestore, auth)

    val postResult = repo.createPost(
      anonymousName = "Velvet Shadow #777",
      anonymousAvatar = "bolt",
      category = "Funny",
      content = "I once waved back at someone who was waving at the person behind me.",
      contentWarning = "",
      commentsEnabled = true,
      reactionsEnabled = true
    )
    val postId = postResult.getOrThrow()

    val reactionResult = repo.toggleReaction(postId, ReactionType.FUNNY)
    assertTrue(reactionResult.isSuccess)
  }
}
