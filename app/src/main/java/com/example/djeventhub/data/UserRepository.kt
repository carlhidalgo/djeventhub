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

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")
    private val storage = FirebaseStorage.getInstance()

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
}