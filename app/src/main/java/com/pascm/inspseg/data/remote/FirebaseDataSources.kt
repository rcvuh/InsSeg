package com.pascm.inspseg.data.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.pascm.inspseg.data.remote.entity.IncidenceDocument
import com.pascm.inspseg.data.remote.entity.InspectionDocument
import com.pascm.inspseg.data.remote.entity.InspectorDocument
import com.pascm.inspseg.domain.rules.CloudSecurityRules
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Acceso a Cloud Firestore. Solo recibe y entrega entidades de Firebase
 * (data/remote/entity); el repositorio hace la conversion con los mappers.
 */
class FirestoreDataSource(private val db: FirebaseFirestore) {

    suspend fun saveInspector(doc: InspectorDocument) {
        db.document(CloudSecurityRules.inspectorPath(doc.uid)).set(doc, SetOptions.merge()).await()
    }

    /** set() con id = id local: reintentar nunca duplica el recorrido. */
    suspend fun saveInspection(uid: String, doc: InspectionDocument) {
        db.document(CloudSecurityRules.inspectionPath(uid, doc.idLocal)).set(doc).await()
    }

    suspend fun saveIncidence(uid: String, doc: IncidenceDocument) {
        db.document(CloudSecurityRules.incidencePath(uid, doc.idLocal)).set(doc).await()
    }

    /** Numero de recorridos del inspector en la nube, en tiempo real. */
    fun observeInspectionCount(uid: String): Flow<Int> = callbackFlow {
        val registration = db.collection("${CloudSecurityRules.inspectorPath(uid)}/recorridos")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) trySend(snapshot.size())
            }
        awaitClose { registration.remove() }
    }

    suspend fun fetchInspections(uid: String): List<InspectionDocument> =
        db.collection("${CloudSecurityRules.inspectorPath(uid)}/recorridos").get().await()
            .toObjects(InspectionDocument::class.java)

    suspend fun fetchIncidences(uid: String): List<IncidenceDocument> =
        db.collection("${CloudSecurityRules.inspectorPath(uid)}/hallazgos").get().await()
            .toObjects(IncidenceDocument::class.java)
}

/** Sube las fotografias de evidencia a Cloud Storage y devuelve su URL de descarga. */
class StorageDataSource(
    private val context: Context,
    private val storage: FirebaseStorage
) {
    /**
     * @return URL publica de descarga, o null si la foto ya no existe o excede el
     * tamaño permitido por [CloudSecurityRules.MAX_PHOTO_BYTES].
     */
    suspend fun uploadEvidence(uid: String, inspectionId: Long, itemId: Long, localUri: String): String? {
        val uri = Uri.parse(localUri)
        val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (size >= 0 && !CloudSecurityRules.photoSizeAllowed(size)) return null
        val ref = storage.reference.child(CloudSecurityRules.evidencePath(uid, inspectionId, itemId))
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
        ref.putFile(uri, metadata).await()
        return ref.downloadUrl.await().toString()
    }
}
