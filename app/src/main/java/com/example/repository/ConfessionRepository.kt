package com.example.repository

import android.content.Context
import com.example.R
import com.example.model.Comment
import com.example.model.Post
import com.example.model.Reaction
import com.example.model.ReactionType
import com.example.model.Report
import com.example.model.SavedPost
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ConfessionRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeFeed(
        categoryFilter: String? = null,
        sortByTrending: Boolean = false
    ): Flow<List<Post>> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        val path = "posts"
        var query: Query = db.collection("posts")

        if (!categoryFilter.isNullOrBlank() && categoryFilter != "All" && categoryFilter != "For You") {
            query = query.whereEqualTo("category", categoryFilter)
        }

        query = if (sortByTrending) {
            query.orderBy("reactionCount", Query.Direction.DESCENDING)
        } else {
            query.orderBy("createdAt", Query.Direction.DESCENDING)
        }

        return query.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Post::class.java).filter { !it.isDeleted && !it.isArchived }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(emptyList())
            }
    }

    fun observePost(postId: String): Flow<Post?> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        val path = "posts/$postId"
        return db.collection("posts").document(postId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(Post::class.java) else null
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, path)
                }
                emit(null)
            }
    }

    suspend fun createPost(
        anonymousName: String,
        anonymousAvatar: String,
        category: String,
        content: String,
        contentWarning: String,
        commentsEnabled: Boolean,
        reactionsEnabled: Boolean
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("posts").document()
        val postId = docRef.id

        val payload = mapOf(
            "postId" to postId,
            "authorId" to uid,
            "anonymousName" to anonymousName,
            "anonymousAvatar" to anonymousAvatar,
            "category" to category,
            "content" to content,
            "contentWarning" to contentWarning,
            "commentsEnabled" to commentsEnabled,
            "reactionsEnabled" to reactionsEnabled,
            "reactionCount" to 0,
            "commentCount" to 0,
            "saveCount" to 0,
            "isArchived" to false,
            "isDeleted" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Result.success(postId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("posts").document(postId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeMyPosts(): Flow<List<Post>> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        val path = "posts"
        return db.collection("posts")
            .whereEqualTo("authorId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Post::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(emptyList())
            }
    }

    fun observeComments(postId: String): Flow<List<Comment>> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        val path = "posts/$postId/comments"
        return db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Comment::class.java)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(emptyList())
            }
    }

    suspend fun addComment(
        postId: String,
        content: String,
        anonymousName: String,
        anonymousAvatar: String
    ): Result<Unit> {
        val uid = requireUserId()
        val postRef = db.collection("posts").document(postId)
        val commentRef = postRef.collection("comments").document()
        val commentId = commentRef.id

        val payload = mapOf(
            "commentId" to commentId,
            "postId" to postId,
            "authorId" to uid,
            "anonymousName" to anonymousName,
            "anonymousAvatar" to anonymousAvatar,
            "content" to content,
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            commentRef.set(payload).await()
            postRef.update("commentCount", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    fun observeUserReaction(postId: String): Flow<Reaction?> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        val path = "posts/$postId/reactions/$uid"
        return db.collection("posts").document(postId).collection("reactions").document(uid)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(Reaction::class.java) else null
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, path)
                }
                emit(null)
            }
    }

    suspend fun toggleReaction(postId: String, reactionType: ReactionType): Result<Unit> {
        val uid = requireUserId()
        val postRef = db.collection("posts").document(postId)
        val reactionRef = postRef.collection("reactions").document(uid)

        return try {
            val snapshot = reactionRef.get().await()
            if (snapshot.exists()) {
                val existingReaction = snapshot.toObject(Reaction::class.java)
                if (existingReaction?.reactionType == reactionType.key) {
                    // Remove reaction
                    reactionRef.delete().await()
                    postRef.update("reactionCount", FieldValue.increment(-1)).await()
                } else {
                    // Update reaction type
                    reactionRef.update(
                        mapOf(
                            "reactionType" to reactionType.key,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } else {
                // Add new reaction
                val payload = mapOf(
                    "reactionId" to uid,
                    "postId" to postId,
                    "userId" to uid,
                    "reactionType" to reactionType.key,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                reactionRef.set(payload).await()
                postRef.update("reactionCount", FieldValue.increment(1)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, reactionRef.path)
            Result.failure(e)
        }
    }

    fun observeSavedPostIds(): Flow<Set<String>> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        val path = "users/$uid/saved_posts"
        return db.collection("users").document(uid).collection("saved_posts")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("postId") }.toSet()
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(emptySet())
            }
    }

    suspend fun toggleSavePost(postId: String): Result<Boolean> {
        val uid = requireUserId()
        val postRef = db.collection("posts").document(postId)
        val saveRef = db.collection("users").document(uid).collection("saved_posts").document(postId)

        return try {
            val snapshot = saveRef.get().await()
            if (snapshot.exists()) {
                saveRef.delete().await()
                postRef.update("saveCount", FieldValue.increment(-1)).await()
                Result.success(false)
            } else {
                val payload = mapOf(
                    "saveId" to postId,
                    "userId" to uid,
                    "postId" to postId,
                    "savedAt" to FieldValue.serverTimestamp()
                )
                saveRef.set(payload).await()
                postRef.update("saveCount", FieldValue.increment(1)).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, saveRef.path)
            Result.failure(e)
        }
    }

    fun observeSavedPosts(): Flow<List<Post>> = flow {
        val uid = auth.currentUser?.uid ?: run {
            emit(emptyList<Post>())
            return@flow
        }
        val savedSnapshot = db.collection("users").document(uid).collection("saved_posts")
            .get().await()
        val postIds = savedSnapshot.documents.mapNotNull { it.getString("postId") }
        if (postIds.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val posts = mutableListOf<Post>()
        for (id in postIds) {
            try {
                val p = db.collection("posts").document(id).get().await().toObject(Post::class.java)
                if (p != null && !p.isDeleted) {
                    posts.add(p)
                }
            } catch (ignored: Exception) {}
        }
        emit(posts.sortedByDescending { it.createdAt?.seconds ?: 0L })
    }

    suspend fun submitReport(targetType: String, targetId: String, reason: String): Result<Unit> {
        val uid = requireUserId()
        val reportRef = db.collection("reports").document()
        val reportId = reportRef.id

        val payload = mapOf(
            "reportId" to reportId,
            "reporterId" to uid,
            "targetType" to targetType,
            "targetId" to targetId,
            "reason" to reason,
            "createdAt" to FieldValue.serverTimestamp()
        )

        return try {
            reportRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reportRef.path)
            Result.failure(e)
        }
    }
}
