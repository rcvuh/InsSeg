package com.pascm.inspseg.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pascm.inspseg.AppContainer
import com.pascm.inspseg.InspSegApplication

/**
 * Fabrica unica de ViewModels. Les entrega las interfaces del dominio
 * (InspectionRepository, AuthRepository), nunca las implementaciones.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { AuthViewModel(container().authRepository, container().repository) }
        initializer { HomeViewModel(container().repository, container().authRepository) }
        initializer { InspectionViewModel(container().repository) }
        initializer { IncidenceViewModel(container().repository) }
    }

    private fun CreationExtras.container(): AppContainer =
        (this[APPLICATION_KEY] as InspSegApplication).container
}
