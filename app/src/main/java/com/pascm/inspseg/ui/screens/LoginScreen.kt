package com.pascm.inspseg.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pascm.inspseg.domain.rules.CredentialRules
import com.pascm.inspseg.ui.theme.InfoSteel
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.WarningAmber
import com.pascm.inspseg.ui.viewmodel.AuthViewModel

/**
 * Acceso del inspector con Firebase Authentication (correo y contraseña).
 * La pantalla solo pinta el estado; las reglas de las credenciales estan en
 * CredentialRules (dominio) y la llamada a Firebase en el repositorio.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onContinueOffline: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen, vertical = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Icon(
            imageVector = Icons.Filled.HealthAndSafety,
            contentDescription = null,
            tint = InfoSteel,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Inspeccion y seguridad",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = if (state.registering) "Crea tu cuenta de inspector. Tus recorridos se respaldan en Firebase."
            else "Inicia sesion para respaldar tus recorridos y evidencias en la nube.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        if (state.registering) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre del inspector") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = { Text(state.nameError.orEmpty()) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
        }
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::setEmail,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            singleLine = true,
            isError = state.emailError != null,
            supportingText = { Text(state.emailError.orEmpty()) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::setPassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.passwordError != null,
            supportingText = {
                Text(
                    state.passwordError
                        ?: if (state.registering) "Minimo ${CredentialRules.MIN_PASSWORD_LENGTH} caracteres con letras y numeros." else ""
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        )

        state.message?.let { Text(it, color = WarningAmber, style = MaterialTheme.typography.bodyMedium) }
        if (!state.firebaseAvailable) {
            Text(
                "Firebase no esta configurado en esta compilacion (falta google-services.json).",
                color = WarningAmber,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = viewModel::submit,
            enabled = !state.loading && state.firebaseAvailable,
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.minTouchTarget)
        ) {
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.registering) "Crear cuenta" else "Iniciar sesion")
            }
        }
        TextButton(onClick = viewModel::toggleMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (state.registering) "Ya tengo cuenta" else "Crear cuenta de inspector")
        }
        if (!state.registering) {
            TextButton(onClick = viewModel::resetPassword, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Olvide mi contraseña")
            }
        }
        TextButton(onClick = onContinueOffline, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Continuar sin conexion (solo en este telefono)", color = TextSecondary)
        }
    }
}
