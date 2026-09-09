package com.loantracker.app.ui.pagamento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.Cents
import com.loantracker.app.data.Cliente
import com.loantracker.app.data.LoanRepository
import com.loantracker.app.data.Parcela
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class RegistrarPagamentoViewModel(private val repository: LoanRepository) : ViewModel() {

    private val busca = MutableStateFlow("")
    private val clienteSelecionadoId = MutableStateFlow<Long?>(null)

    val clientesFiltrados: StateFlow<List<Cliente>> =
        combine(repository.observarClientes(), busca) { clientes, filtro ->
            if (filtro.isBlank()) emptyList()
            else clientes.filter { it.nome.contains(filtro, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clienteSelecionado: StateFlow<Cliente?> =
        combine(repository.observarClientes(), clienteSelecionadoId) { clientes, id ->
            clientes.firstOrNull { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val parcelasPendentes: StateFlow<List<Parcela>> =
        clienteSelecionadoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observarParcelasPendentesDoCliente(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun atualizarBusca(texto: String) {
        busca.value = texto
    }

    fun selecionarCliente(id: Long) {
        clienteSelecionadoId.value = id
        busca.value = ""
    }

    fun limparCliente() {
        clienteSelecionadoId.value = null
    }

    fun registrarPagamento(parcelaId: Long, valorPagoCents: Cents, data: LocalDate, aoConcluir: () -> Unit) {
        viewModelScope.launch {
            repository.registrarPagamento(parcelaId, valorPagoCents, data)
            aoConcluir()
        }
    }
}
