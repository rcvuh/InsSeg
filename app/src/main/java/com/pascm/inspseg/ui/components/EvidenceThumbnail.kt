package com.pascm.inspseg.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pascm.inspseg.ui.theme.OutlineStrong
import com.pascm.inspseg.ui.theme.SurfaceHigh
import com.pascm.inspseg.ui.theme.TextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Filtro monocromatico aplicado a toda imagen de la app.
 *
 * La evidencia fotografica se muestra en escala de grises a proposito: mantiene
 * la interfaz en un solo registro visual y deja el color disponible unicamente
 * para el estado de seguridad, que es lo que debe saltar a la vista.
 */
val GrayscaleFilter: ColorFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply { setToSaturation(0f) }
)

@Composable
fun EvidenceThumbnail(
    uri: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 72.dp
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri)).use { stream ->
                    // Se submuestrea: la miniatura no necesita la resolucion completa.
                    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .background(SurfaceHigh, RoundedCornerShape(10.dp))
            .border(1.dp, OutlineStrong, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Fotografia de evidencia del hallazgo",
                contentScale = ContentScale.Crop,
                colorFilter = GrayscaleFilter,
                modifier = Modifier
                    .size(size)
                    .background(SurfaceHigh, RoundedCornerShape(10.dp))
            )
        } else {
            Icon(
                imageVector = Icons.Filled.BrokenImage,
                contentDescription = "Evidencia no disponible",
                tint = TextTertiary
            )
        }
    }
}
