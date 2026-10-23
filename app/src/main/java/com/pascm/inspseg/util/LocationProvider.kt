package com.pascm.inspseg.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Posicion del recorrido, tomada del LocationManager del framework. Se evita una
 * dependencia extra de Play Services.
 *
 * Primero se intenta la ultima posicion conocida, que es inmediata; si el sistema
 * no tiene ninguna (equipo recien encendido, o ninguna app ha pedido ubicacion),
 * se solicita una sola actualizacion con tiempo limite. Sin esa segunda via, un
 * hallazgo podria quedar guardado sin coordenadas.
 */
object LocationProvider {

    private const val FIX_TIMEOUT_MS = 8_000L

    private val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    )

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Ultima posicion conocida, sin esperar. Puede ser nula. */
    fun lastKnown(context: Context): GeoPoint? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService<LocationManager>() ?: return null
        return try {
            providers.asSequence()
                .filter { manager.isProviderEnabled(it) }
                .mapNotNull { manager.getLastKnownLocation(it) }
                .maxByOrNull { it.time }
                ?.let { GeoPoint(it.latitude, it.longitude) }
        } catch (e: SecurityException) {
            null
        }
    }

    /** Ultima posicion conocida y, si no hay, una lectura nueva con tiempo limite. */
    suspend fun current(context: Context): GeoPoint? =
        lastKnown(context) ?: requestSingleFix(context)

    private suspend fun requestSingleFix(context: Context): GeoPoint? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService<LocationManager>() ?: return null
        val provider = providers.firstOrNull { manager.isProviderEnabled(it) } ?: return null

        return withTimeoutOrNull(FIX_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(GeoPoint(location.latitude, location.longitude))
                        }
                    }

                    @Deprecated("Requerido por la interfaz en APIs antiguas")
                    override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) = Unit
                }
                try {
                    manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                } catch (e: SecurityException) {
                    if (continuation.isActive) continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            }
        }
    }
}
