package com.pascm.inspseg.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Almacenamiento local de las fotografias de evidencia. */
object EvidenceFiles {

    private fun folder(context: Context): File =
        File(context.filesDir, "evidencias").apply { if (!exists()) mkdirs() }

    /** Crea el destino de una nueva captura y devuelve su Uri de FileProvider. */
    fun newEvidenceUri(context: Context, itemId: Long): Uri {
        val file = File(folder(context), "hallazgo_${itemId}_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
