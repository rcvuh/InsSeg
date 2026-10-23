package com.pascm.inspseg

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pascm.inspseg.data.local.InspSegDatabase
import com.pascm.inspseg.data.remote.FirestoreDataSource
import com.pascm.inspseg.data.remote.StorageDataSource
import com.pascm.inspseg.data.repository.FirebaseAuthRepository
import com.pascm.inspseg.data.repository.OfflineFirstInspectionRepository
import com.pascm.inspseg.domain.repository.AuthRepository
import com.pascm.inspseg.domain.repository.InspectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Contenedor manual de dependencias. Arma las capas de datos (Room + Firebase) y
 * expone solo las interfaces del dominio a los ViewModels.
 *
 * Si el proyecto no tiene google-services.json, FirebaseApp no se inicializa y la
 * app trabaja en modo local con Room, igual que la version T3.3.
 */
class AppContainer(application: Application) {
    private val database = InspSegDatabase.get(application)
    val firebaseReady: Boolean = FirebaseApp.getApps(application).isNotEmpty()

    private val firestore = if (firebaseReady) FirestoreDataSource(FirebaseFirestore.getInstance()) else null
    private val storage = if (firebaseReady) StorageDataSource(application, FirebaseStorage.getInstance()) else null

    val authRepository: AuthRepository =
        FirebaseAuthRepository(if (firebaseReady) FirebaseAuth.getInstance() else null, firestore)

    val repository: InspectionRepository =
        OfflineFirstInspectionRepository(database, authRepository, firestore, storage)
}

class InspSegApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            // Precarga del catalogo normativo y reintento de lo pendiente de subir.
            container.repository.ensureSeeded()
            runCatching { container.repository.syncPending() }
        }
    }
}
