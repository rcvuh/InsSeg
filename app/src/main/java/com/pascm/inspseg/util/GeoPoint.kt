package com.pascm.inspseg.util

import java.util.Locale

/** Coordenada asociada a un recorrido o a un hallazgo. */
data class GeoPoint(val latitude: Double, val longitude: Double) {
    fun format(): String = String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
}
