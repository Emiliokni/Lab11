package com.example.tiendatematisada251630.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUserId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInAnonymously(): Result<String> {
        return runCatching {
            val user = auth.currentUser
            if (user != null) {
                user.uid
            } else {
                val result = auth.signInAnonymously().await()
                result.user?.uid ?: throw IllegalStateException("No se pudo obtener el UID anónimo")
            }
        }
    }
}
