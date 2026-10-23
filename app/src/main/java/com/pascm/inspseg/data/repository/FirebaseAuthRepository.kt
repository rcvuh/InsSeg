package com.pascm.inspseg.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.pascm.inspseg.data.mapper.toAuthMessage
import com.pascm.inspseg.data.mapper.toDocument
import com.pascm.inspseg.data.mapper.toDomain
import com.pascm.inspseg.data.remote.FirestoreDataSource
import com.pascm.inspseg.domain.model.AuthResult
import com.pascm.inspseg.domain.model.Inspector
import com.pascm.inspseg.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Autenticacion con Firebase Authentication (correo y contraseña).
 * [auth] es nulo si el proyecto aun no tiene google-services.json: la app sigue
 * funcionando en modo local, sin respaldo en la nube.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth?,
    private val firestore: FirestoreDataSource?
) : AuthRepository {

    override val isAvailable: Boolean get() = auth != null

    override fun currentInspector(): Inspector? = auth?.currentUser?.toDomain()

    override fun observeInspector(): Flow<Inspector?> {
        val firebaseAuth = auth ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDomain()) }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult = call {
        it.signInWithEmailAndPassword(email.trim(), password).await()
    }

    /** Crea la cuenta, guarda el nombre en el perfil y el documento inspectores/{uid}. */
    override suspend fun register(name: String, email: String, password: String): AuthResult = call {
        val user = it.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase no devolvio el usuario creado.")
        user.updateProfile(userProfileChangeRequest { displayName = name.trim() }).await()
        user.reload().await()
        runCatching {
            firestore?.saveInspector(it.currentUser!!.toDomain().toDocument(System.currentTimeMillis()))
        }
    }

    override suspend fun sendPasswordReset(email: String): AuthResult = call {
        it.sendPasswordResetEmail(email.trim()).await()
    }

    override fun signOut() {
        auth?.signOut()
    }

    private suspend fun call(block: suspend (FirebaseAuth) -> Unit): AuthResult {
        val firebaseAuth = auth
            ?: return AuthResult.Failure("Firebase no esta configurado: falta app/google-services.json.")
        return try {
            block(firebaseAuth)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthMessage())
        }
    }
}
