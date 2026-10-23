package com.pascm.inspseg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pascm.inspseg.domain.model.AuthResult
import com.pascm.inspseg.domain.model.Inspector
import com.pascm.inspseg.domain.repository.AuthRepository
import com.pascm.inspseg.domain.repository.InspectionRepository
import com.pascm.inspseg.domain.rules.CredentialRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val registering: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loading: Boolean = false,
    val message: String? = null,
    val firebaseAvailable: Boolean = true
)

/** Pantalla de acceso. La validacion la decide CredentialRules (dominio). */
class AuthViewModel(
    private val auth: AuthRepository,
    private val repository: InspectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(firebaseAvailable = auth.isAvailable))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    /** Sesion actual; la navegacion la usa para decidir entre login y tablero. */
    val inspector: StateFlow<Inspector?> = auth.observeInspector()
        .stateIn(viewModelScope, SharingStarted.Eagerly, auth.currentInspector())

    fun toggleMode() = _state.update {
        it.copy(registering = !it.registering, message = null, nameError = null, emailError = null, passwordError = null)
    }

    fun setName(v: String) = _state.update { it.copy(name = v, nameError = null) }
    fun setEmail(v: String) = _state.update { it.copy(email = v, emailError = null) }
    fun setPassword(v: String) = _state.update { it.copy(password = v, passwordError = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun submit() {
        val s = _state.value
        val check = if (s.registering) CredentialRules.forRegistration(s.name, s.email, s.password)
        else CredentialRules.forSignIn(s.email, s.password)
        if (!check.isValid) {
            _state.update {
                it.copy(nameError = check.nameError, emailError = check.emailError, passwordError = check.passwordError)
            }
            return
        }
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            val result = if (s.registering) auth.register(s.name, s.email, s.password)
            else auth.signIn(s.email, s.password)
            when (result) {
                AuthResult.Success -> {
                    _state.update { AuthUiState(firebaseAvailable = auth.isAvailable) }
                    // Al entrar se respalda lo que quedo pendiente en el telefono.
                    repository.requestSync()
                }
                is AuthResult.Failure -> _state.update { it.copy(loading = false, message = result.message) }
            }
        }
    }

    fun resetPassword() {
        val email = _state.value.email
        CredentialRules.validateEmail(email)?.let { error ->
            _state.update { it.copy(emailError = error) }
            return
        }
        viewModelScope.launch {
            val msg = when (val r = auth.sendPasswordReset(email)) {
                AuthResult.Success -> "Te enviamos un correo para restablecer la contraseña."
                is AuthResult.Failure -> r.message
            }
            _state.update { it.copy(message = msg) }
        }
    }

    fun signOut() = auth.signOut()
}
