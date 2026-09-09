package com.loantracker.app.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.Cents
import com.loantracker.app.data.Cliente
import com.loantracker.app.data.EmprestimoResumo
import com.loantracker.app.data.LoanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PerfilClienteUi(
    val cliente: Cliente? = null,
    val totalEmprestadoCents: Cents = 0,
    val totalRecebidoCents: Cents = 0,
    val totalEmAbertoCents: Cents = 0,
    val emprestimos: List<EmprestimoResumo> = emptyList()
)

class PerfilClienteViewModel(repository: LoanRepository, clienteId: Long) : ViewModel() {

    val estado: StateFlow<PerfilClienteUi> = combine(
        repository.observarCliente(clienteId),
        repository.observarResumosEmprestimos(clienteId)
    ) { cliente, emprestimos ->
        PerfilClienteUi(
            cliente = cliente,
            totalEmprestadoCents = emprestimos.sumOf { it.emprestimo.valorEmprestadoCents },
            totalRecebidoCents = emprestimos.sumOf { it.totalPagoCents },
            totalEmAbertoCents = emprestimos.sumOf { it.totalRestanteCents },
            emprestimos = emprestimos
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PerfilClienteUi())
}
