package com.pascm.inspseg.domain.rules

/**
 * Reglas de validacion de credenciales. Viven en el dominio para que la UI solo
 * muestre los mensajes y el repositorio nunca reciba datos invalidos.
 */
object CredentialRules {

    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_NAME_LENGTH = 5
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    data class Result(
        val emailError: String? = null,
        val passwordError: String? = null,
        val nameError: String? = null
    ) {
        val isValid: Boolean get() = emailError == null && passwordError == null && nameError == null
    }

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Escribe tu correo."
        !EMAIL.matches(email.trim()) -> "El correo no tiene un formato valido."
        else -> null
    }

    /** Al menos 8 caracteres con letras y numeros (se exige al registrar). */
    fun validateNewPassword(password: String): String? = when {
        password.length < MIN_PASSWORD_LENGTH -> "Usa al menos $MIN_PASSWORD_LENGTH caracteres."
        password.none { it.isLetter() } || password.none { it.isDigit() } ->
            "Combina letras y numeros."
        else -> null
    }

    fun validateName(name: String): String? =
        if (name.trim().length < MIN_NAME_LENGTH) "Escribe tu nombre completo." else null

    fun forSignIn(email: String, password: String) = Result(
        emailError = validateEmail(email),
        passwordError = if (password.isEmpty()) "Escribe tu contraseña." else null
    )

    fun forRegistration(name: String, email: String, password: String) = Result(
        emailError = validateEmail(email),
        passwordError = validateNewPassword(password),
        nameError = validateName(name)
    )
}
