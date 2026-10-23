package com.pascm.inspseg.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Paleta InspSeg. Apariencia oscura permanente sobre negro puro.
 *
 * Los ratios anotados son contra el fondo base #000000 y cumplen el minimo 4.5:1
 * para texto y el objetivo 7:1 recomendado para texto pequeño en modo oscuro.
 *
 * Regla de uso: la interfaz es monocromatica. El color solo aparece para
 * comunicar estado de seguridad, y cada color significa una sola cosa
 * (guia de Color: "evitar usar el mismo color para significar cosas distintas").
 */

// --- Base y superficies. Escalonadas en luminancia para dar profundidad ---
val Void = Color(0xFF000000)          // Fondo de la app (OLED)
val SurfaceBase = Color(0xFF0D0D0F)   // Tarjetas sobre el fondo
val SurfaceRaised = Color(0xFF16161A) // Hojas modales y elementos elevados
val SurfaceHigh = Color(0xFF202026)   // Chips, campos y estados presionados
val OutlineStrong = Color(0xFF3A3A42) // Bordes de controles interactivos
val OutlineSubtle = Color(0xFF26262C) // Separadores

// --- Texto ---
val TextPrimary = Color(0xFFF5F5F7)   // 19.0:1
val TextSecondary = Color(0xFFA9A9B2) // 8.4:1
val TextTertiary = Color(0xFF83838D)  // 5.1:1  (uso limitado a metadatos)

// --- Estados de seguridad. Un color, un significado ---
val CriticalRed = Color(0xFFFF6B5E)   // 7.5:1  Paro critico / no conforme alto
val WarningAmber = Color(0xFFFFB84D)  // 12.2:1 Reinspeccion / riesgo medio
val SafeGreen = Color(0xFF3ED598)     // 11.2:1 Conforme
val InfoSteel = Color(0xFF8FB8FF)     // 9.6:1  Referencia normativa

// --- Contenedores tenues para fondos de aviso (texto encima siempre claro) ---
val CriticalContainer = Color(0xFF2B0F0C)
val WarningContainer = Color(0xFF2A1D06)
val SafeContainer = Color(0xFF07231A)
val InfoContainer = Color(0xFF0D1626)
