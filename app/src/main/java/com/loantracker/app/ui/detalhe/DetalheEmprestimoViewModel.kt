package com.loantracker.app.ui.detalhe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.Cliente
import com.loantracker.app.data.Emprestimo
import com.loantracker.app.data.LoanRepository
import com.loantracker.app.data.Parcela
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class DetalheEmprestimoUi(
    val emprestimo: Emprestimo? = null,
    val cliente: Cliente? = null,
    val parcelas: List<Parcela> = emptyList()
) {
    val totalPagoCents get() = parcelas.sumOf { it.valorPagoCents }
    val totalRestanteCents get() = parcelas.sumOf { (it.valorCents - it.valorPagoCents).coerceAtLeast(0) }
    val ultimoVencimento get() = parcelas.maxByOrNull { it.numero }?.vencimento
}

class DetalheEmprestimoViewModel(repository: LoanRepository, emprestimoId: Long) : ViewModel() {

    val estado: StateFlow<DetalheEmprestimoUi> = repository.observarEmprestimo(emprestimoId)
        .flatMapLatest { emprestimo ->
            if (emprestimo == null) {
                flowOf(DetalheEmprestimoUi())
            } else {
                combine(
                    repository.observarCliente(emprestimo.clienteId),
                    repository.observarParcelasDoEmprestimo(emprestimoId)
                ) { cliente, parcelas ->
                    DetalheEmprestimoUi(
                        emprestimo = emprestimo,
                        cliente = cliente,
                        parcelas = parcelas.sortedBy { it.numero }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetalheEmprestimoUi())
}
