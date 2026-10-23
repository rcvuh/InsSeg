package com.pascm.inspseg.data.mapper

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.pascm.inspseg.domain.model.Inspector

/** FirebaseUser (SDK) -> Inspector (dominio). */
fun FirebaseUser.toDomain() = Inspector(
    uid = uid,
    email = email.orEmpty(),
    displayName = displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty().substringBefore('@')
)

/** Excepciones de Firebase -> mensajes en español para la UI. */
fun Throwable.toAuthMessage(): String = when (this) {
    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo o fue deshabilitada."
    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
    is FirebaseAuthUserCollisionException -> "Ese correo ya tiene una cuenta. Inicia sesion."
    is FirebaseAuthWeakPasswordException -> "Firebase considera debil la contraseña. Usa una mas larga."
    is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera unos minutos."
    is FirebaseNetworkException -> "Sin conexion. Revisa tu red e intenta de nuevo."
    else -> message ?: "Ocurrio un error inesperado."
}
