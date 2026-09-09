package com.loantracker.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.ClienteResumo
import com.loantracker.app.data.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repository: LoanRepository) : ViewModel() {

    private val busca = MutableStateFlow("")

    fun atualizarBusca(texto: String) {
        busca.value = texto
    }

    val clientesFiltrados: StateFlow<List<ClienteResumo>> =
        combine(repository.observarResumosClientes(), busca) { resumos, filtro ->
            if (filtro.isBlank()) resumos
            else resumos.filter { it.cliente.nome.contains(filtro, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
