package com.loantracker.app.ui.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.Cliente
import com.loantracker.app.data.LoanRepository
import com.loantracker.app.ui.rememberViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditarClienteViewModel(
    private val repository: LoanRepository,
    clienteId: Long
) : ViewModel() {

    val cliente: StateFlow<Cliente?> =
        repository.observarCliente(clienteId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun salvar(
        clienteAtual: Cliente,
        nome: String,
        telefone: String,
        cpf: String,
        endereco: String,
        observacao: String,
        aoConcluir: () -> Unit
    ) {
        viewModelScope.launch {
            repository.atualizarCliente(
                clienteAtual.copy(
                    nome = nome.trim(),
                    telefone = telefone.trim().ifBlank { null },
                    cpf = cpf.trim().ifBlank { null },
                    endereco = endereco.trim().ifBlank { null },
                    observacao = observacao.trim().ifBlank { null }
                )
            )
            aoConcluir()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarClienteScreen(
    clienteId: Long,
    onVoltar: () -> Unit,
    onClienteSalvo: () -> Unit
) {
    val viewModel = rememberViewModel { repo -> EditarClienteViewModel(repo, clienteId) }
    val cliente by viewModel.cliente.collectAsState()

    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var observacao by remember { mutableStateOf("") }
    var dadosCarregados by remember { mutableStateOf(false) }

    LaunchedEffect(cliente) {
        val atual = cliente
        if (atual != null && !dadosCarregados) {
            nome = atual.nome
            telefone = atual.telefone ?: ""
            cpf = atual.cpf ?: ""
            endereco = atual.endereco ?: ""
            observacao = atual.observacao ?: ""
            dadosCarregados = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar cliente") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = telefone,
                onValueChange = { telefone = it },
                label = { Text("Telefone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = cpf,
                onValueChange = { cpf = it },
                label = { Text("CPF") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = endereco,
                onValueChange = { endereco = it },
                label = { Text("Endereço") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = observacao,
                onValueChange = { observacao = it },
                label = { Text("Observação") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                minLines = 3
            )
            Button(
                onClick = {
                    cliente?.let { atual ->
                        viewModel.salvar(atual, nome, telefone, cpf, endereco, observacao, onClienteSalvo)
                    }
                },
                enabled = nome.isNotBlank() && cliente != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar alterações")
            }
        }
    }
}
