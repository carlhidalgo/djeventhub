package com.example.djeventhub.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository for managing file uploads to Firebase Storage
 */
class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val storageRef: StorageReference = storage.reference

    /**
     * Upload an event image to Firebase Storage
     * @param uri Local URI of the image to upload
     * @param eventId Optional event ID to associate with the image
     * @return Result with download URL on success, or exception on failure
     */
    suspend fun uploadEventImage(uri: Uri, eventId: String? = null): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Generate unique filename
            val filename = "${UUID.randomUUID()}.jpg"
            val imagePath = "events/$currentUserId/$filename"

            val imageRef = storageRef.child(imagePath)

            // Upload file
            imageRef.putFile(uri).await()

            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload an event image with progress tracking
     * @param uri Local URI of the image to upload
     * @param onProgress Callback for upload progress (0.0 to 1.0)
     * @return Result with download URL on success
     */
    suspend fun uploadEventImageWithProgress(
        uri: Uri,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val filename = "${UUID.randomUUID()}.jpg"
            val imagePath = "events/$currentUserId/$filename"
            val imageRef = storageRef.child(imagePath)

            // Upload with progress tracking
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() /
                              taskSnapshot.totalByteCount.toFloat()
                onProgress(progress)
            }

            uploadTask.await()

            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an image from Firebase Storage
     * @param imageUrl The download URL of the image to delete
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
