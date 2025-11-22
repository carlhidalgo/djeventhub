package com.example.djeventhub.data

import com.example.djeventhub.EventRepository
import com.example.djeventhub.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) {
    private val reviewsCollection = firestore.collection("reviews")

    /**
     * Create a new review for a DJ after an event
     */
    suspend fun createReview(
        eventId: String,
        reviewedUserId: String,
        rating: Int,
        comment: String,
        photoUrls: List<String> = emptyList()
    ): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))
            val currentUser = userRepository.getCurrentUserProfile()
                ?: return Result.failure(IllegalStateException("User profile not found"))

            // Verify that they actually worked together
            val event = eventRepository.getEventById(eventId)
            val verified = event?.selectedDJ == reviewedUserId && event.createdBy == currentUserId

            // Check if review already exists for this event + reviewer combo
            val existingReview = reviewsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("reviewerId", currentUserId)
                .whereEqualTo("reviewedUserId", reviewedUserId)
                .get()
                .await()

            if (!existingReview.isEmpty) {
                return Result.failure(IllegalStateException("Ya escribiste un review para este evento"))
            }

            // Create review
            val review = Review(
                eventId = eventId,
                reviewerId = currentUserId,
                reviewerName = currentUser.displayName,
                reviewerImage = currentUser.profileImageUrl,
                reviewedUserId = reviewedUserId,
                rating = rating.coerceIn(1, 5),
                comment = comment,
                photoUrls = photoUrls,
                verified = verified
            )

            val docRef = reviewsCollection.add(review).await()
            
            // Update user's rating
            updateUserRating(reviewedUserId)

            android.util.Log.d("ReviewRepository", "Review created: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepository", "Error creating review", e)
            Result.failure(e)
        }
    }

    /**
     * Get all reviews for a specific user
     */
    fun observeReviewsForUser(userId: String): Flow<List<Review>> = callbackFlow {
        val subscription = reviewsCollection
            .whereEqualTo("reviewedUserId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ReviewRepository", "Error observing reviews", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val reviews = snapshot?.toObjects(Review::class.java) ?: emptyList()
                android.util.Log.d("ReviewRepository", "Received ${reviews.size} reviews for user $userId")
                trySend(reviews)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Get reviews for a user (one-time fetch)
     */
    suspend fun getReviewsForUser(userId: String): List<Review> {
        return try {
            reviewsCollection
                .whereEqualTo("reviewedUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Review::class.java)
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepository", "Error getting reviews", e)
            emptyList()
        }
    }

    /**
     * Check if user can review (worked together in this event)
     */
    suspend fun canReview(eventId: String, reviewedUserId: String): Boolean {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return false
            val event = eventRepository.getEventById(eventId) ?: return false
            
            // Can review if: I'm the productora, the event has a selected DJ, and it's the user being reviewed
            val canReview = event.createdBy == currentUserId && event.selectedDJ == reviewedUserId

            // Check if already reviewed
            if (canReview) {
                val existingReview = reviewsCollection
                    .whereEqualTo("eventId", eventId)
                    .whereEqualTo("reviewerId", currentUserId)
                    .whereEqualTo("reviewedUserId", reviewedUserId)
                    .get()
                    .await()
                
                return existingReview.isEmpty
            }
            
            false
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepository", "Error checking if can review", e)
            false
        }
    }

    /**
     * Update user's average rating based on all reviews
     */
    private suspend fun updateUserRating(userId: String) {
        try {
            val reviews = getReviewsForUser(userId)
            if (reviews.isNotEmpty()) {
                val averageRating = reviews.map { it.rating }.average()
                val totalRatings = reviews.size

                // Update user document
                firestore.collection("users").document(userId).update(
                    mapOf(
                        "rating" to averageRating,
                        "totalRatings" to totalRatings
                    )
                ).await()

                android.util.Log.d("ReviewRepository", "Updated rating for user $userId: avg=$averageRating, total=$totalRatings")
            }
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepository", "Error updating user rating", e)
        }
    }

    /**
     * Delete a review (only owner can delete)
     */
    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val review = reviewsCollection.document(reviewId).get().await().toObject(Review::class.java)
                ?: return Result.failure(IllegalStateException("Review not found"))

            if (review.reviewerId != currentUserId) {
                return Result.failure(IllegalStateException("You can only delete your own reviews"))
            }

            reviewsCollection.document(reviewId).delete().await()
            
            // Update user's rating after deletion
            updateUserRating(review.reviewedUserId)

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ReviewRepository", "Error deleting review", e)
            Result.failure(e)
        }
    }
}
