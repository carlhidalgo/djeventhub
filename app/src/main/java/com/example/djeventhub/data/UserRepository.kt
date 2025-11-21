package com.example.djeventhub.data

import android.net.Uri
import com.example.djeventhub.models.User
import com.example.djeventhub.models.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")

    // Get current user profile
    suspend fun getCurrentUserProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            usersCollection.document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Observe current user profile in real-time
    fun observeCurrentUserProfile(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val subscription = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }

        awaitClose { subscription.remove() }
    }

    // Create user profile after registration
    suspend fun createUserProfile(
        uid: String,
        email: String,
        userType: UserType,
        displayName: String
    ): Result<Unit> {
        return try {
            val user = User(
                uid = uid,
                email = email,
                userType = userType,
                displayName = displayName
            )
            usersCollection.document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user profile
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload profile image to Firebase Storage and return download URL
    suspend fun uploadProfileImage(uri: Uri): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No auth user"))
        return try {
            val ref = storage.reference.child("users/$uid/profile.jpg")
            // putFile
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user by ID
    suspend fun getUserById(uid: String): User? {
        return try {
            usersCollection.document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Search DJs by location (for Productoras)
    suspend fun searchDJsByLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 50.0
    ): List<User> {
        return try {
            // Simple search by user type (geo queries require GeoFire library)
            // For MVP, just return all DJs
            usersCollection
                .whereEqualTo("userType", UserType.DJ.name)
                .get()
                .await()
                .toObjects(User::class.java)
                .filter { user ->
                    // Basic distance filter (simplified)
                    user.latitude != null && user.longitude != null
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get all DJs (for Productoras)
    suspend fun getAllDJs(): List<User> {
        return try {
            usersCollection
                .whereEqualTo("userType", UserType.DJ.name)
                .get()
                .await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add rating to a user
    suspend fun addRating(userId: String, rating: Int): Result<Unit> {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            val current = userDoc.toObject(User::class.java)
            if (current == null) return Result.failure(Exception("Usuario no encontrado"))

            val newTotal = current.totalRatings + 1
            val newRating = ((current.rating * current.totalRatings) + rating) / newTotal.toDouble()
            val newEventsCompleted = current.eventsCompleted + 1

            usersCollection.document(userId).update(
                mapOf(
                    "rating" to newRating,
                    "totalRatings" to newTotal,
                    "eventsCompleted" to newEventsCompleted
                ) as Map<String, Any>
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}