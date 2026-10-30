package com.example.tiendatematisada251630.data

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>
    suspend fun signInAnonymously(): Result<String>
}
