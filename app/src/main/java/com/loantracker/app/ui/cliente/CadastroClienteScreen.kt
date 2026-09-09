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
import kotlinx.coroutines.launch

class CadastroClienteViewModel(private val repository: LoanRepository) : ViewModel() {
    fun cadastrar(
        nome: String,
        telefone: String,
        cpf: String,
        endereco: String,
        observacao: String,
        aoConcluir: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.cadastrarCliente(
                Cliente(
                    nome = nome.trim(),
                    telefone = telefone.trim().ifBlank { null },
                    cpf = cpf.trim().ifBlank { null },
                    endereco = endereco.trim().ifBlank { null },
                    observacao = observacao.trim().ifBlank { null }
                )
            )
            aoConcluir(id)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroClienteScreen(
    onVoltar: () -> Unit,
    onClienteCadastrado: (Long) -> Unit
) {
    val viewModel = rememberViewModel { repo -> CadastroClienteViewModel(repo) }

    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var observacao by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar cliente") },
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
                    viewModel.cadastrar(nome, telefone, cpf, endereco, observacao) { id ->
                        onClienteCadastrado(id)
                    }
                },
                enabled = nome.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar cliente")
            }
        }
    }
}
