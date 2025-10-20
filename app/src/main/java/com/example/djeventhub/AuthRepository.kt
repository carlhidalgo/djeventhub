package com.example.djeventhub

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Repository wrapping FirebaseAuth operations
class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    suspend fun registerWithEmail(email: String, password: String): FirebaseUser? =
        suspendCancellableCoroutine { cont ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    cont.resume(result.user)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    suspend fun loginWithEmail(email: String, password: String): FirebaseUser? =
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    cont.resume(result.user)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    suspend fun loginWithCredential(credential: AuthCredential): FirebaseUser? =
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    cont.resume(result.user)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun currentUser(): FirebaseUser? = firebaseAuth.currentUser
}

