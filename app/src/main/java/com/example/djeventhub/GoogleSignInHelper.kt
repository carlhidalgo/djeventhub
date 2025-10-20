package com.example.djeventhub

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

// Helper for Google Sign-In client and credential creation
object GoogleSignInHelper {

    // Returns a configured GoogleSignInClient. The app should provide
    // a string resource `default_web_client_id` (from google-services.json)
    fun getClient(context: Context): GoogleSignInClient {
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            // If the string is not available, use an empty id and the sign-in will fail at runtime.
            ""
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    fun getCredential(idToken: String): AuthCredential {
        return GoogleAuthProvider.getCredential(idToken, null)
    }
}

