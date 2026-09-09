package com.loantracker.app.ui.emprestimo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.Cents
import com.loantracker.app.data.Cliente
import com.loantracker.app.data.FrequenciaParcela
import com.loantracker.app.data.LoanRepository
import com.loantracker.app.data.TipoJuros
import com.loantracker.app.domain.InterestCalculator
import com.loantracker.app.domain.ResumoEmprestimo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class NovoEmprestimoState(
    val clienteId: Long? = null,
    val valorEmprestadoCents: Cents = 0,
    val taxaPercentual: Double = 0.0,
    val tipoJuros: TipoJuros = TipoJuros.SIMPLES,
    val quantidadeParcelas: Int = 1,
    val frequencia: FrequenciaParcela = FrequenciaParcela.MENSAL,
    val dataEmprestimo: LocalDate = LocalDate.now(),
    val primeiroVencimento: LocalDate = LocalDate.now().plusMonths(1)
) {
    val pronto: Boolean
        get() = clienteId != null && valorEmprestadoCents > 0 && quantidadeParcelas > 0
}

class CadastroEmprestimoViewModel(private val repository: LoanRepository) : ViewModel() {

    val clientes: StateFlow<List<Cliente>> =
        repository.observarClientes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(NovoEmprestimoState())
    val form: StateFlow<NovoEmprestimoState> = _form

    fun atualizar(transform: (NovoEmprestimoState) -> NovoEmprestimoState) {
        _form.value = transform(_form.value)
    }

    fun calcularResumo(): ResumoEmprestimo? {
        val estado = _form.value
        if (!estado.pronto) return null
        return InterestCalculator.calcular(
            valorEmprestadoCents = estado.valorEmprestadoCents,
            taxaPercentual = estado.taxaPercentual,
            tipo = estado.tipoJuros,
            quantidadeParcelas = estado.quantidadeParcelas,
            dataEmprestimo = estado.dataEmprestimo,
            primeiroVencimento = estado.primeiroVencimento,
            frequencia = estado.frequencia
        )
    }

    fun confirmar(aoConcluir: () -> Unit) {
        val estado = _form.value
        val clienteId = estado.clienteId ?: return
        viewModelScope.launch {
            repository.cadastrarEmprestimo(
                clienteId = clienteId,
                valorEmprestadoCents = estado.valorEmprestadoCents,
                taxaPercentual = estado.taxaPercentual,
                tipo = estado.tipoJuros,
                quantidadeParcelas = estado.quantidadeParcelas,
                frequencia = estado.frequencia,
                dataEmprestimo = estado.dataEmprestimo,
                primeiroVencimento = estado.primeiroVencimento
            )
            aoConcluir()
        }
    }
}
