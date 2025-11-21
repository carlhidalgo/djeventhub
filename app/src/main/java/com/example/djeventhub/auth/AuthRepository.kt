package com.example.djeventhub.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    private suspend fun <T> awaitTask(task: Task<T>): T = suspendCancellableCoroutine { cont ->
        task.addOnCompleteListener { t ->
            if (t.isSuccessful) {
                cont.resume(t.result as T)
            } else {
                cont.resumeWithException(t.exception ?: Exception("Unknown task exception"))
            }
        }
    }

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        val result = awaitTask(auth.signInWithEmailAndPassword(email, password))
        return (result as com.google.firebase.auth.AuthResult).user
    }

    suspend fun signUpWithEmail(email: String, password: String): FirebaseUser? {
        val result = awaitTask(auth.createUserWithEmailAndPassword(email, password))
        return (result as com.google.firebase.auth.AuthResult).user
    }

    suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser? {
        val result = awaitTask(auth.signInWithCredential(credential))
        return (result as com.google.firebase.auth.AuthResult).user
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid
}
