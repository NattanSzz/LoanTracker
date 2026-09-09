package com.loantracker.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loantracker.app.ui.components.ClienteCard
import com.loantracker.app.ui.rememberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCadastrarCliente: () -> Unit,
    onCadastrarEmprestimo: () -> Unit,
    onRegistrarPagamento: () -> Unit,
    onAbrirCliente: (Long) -> Unit
) {
    val viewModel = rememberViewModel { repo -> HomeViewModel(repo) }
    val resumos by viewModel.clientesFiltrados.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }
    var busca by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Início") },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Adicionar")
                        }
                        DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                            DropdownMenuItem(
                                text = { Text("Cadastrar cliente") },
                                onClick = {
                                    menuExpandido = false
                                    onCadastrarCliente()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cadastrar empréstimo") },
                                onClick = {
                                    menuExpandido = false
                                    onCadastrarEmprestimo()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Button(
                onClick = onRegistrarPagamento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar pagamento")
            }

            Box(modifier = Modifier.padding(vertical = 12.dp)) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = {
                        busca = it
                        viewModel.atualizarBusca(it)
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text("Buscar cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (resumos.isEmpty()) {
                Text(
                    text = "Nenhum cliente cadastrado ainda. Toque em + para começar.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(resumos, key = { it.cliente.id }) { resumo ->
                        ClienteCard(resumo = resumo, onClick = { onAbrirCliente(resumo.cliente.id) })
                    }
                }
            }
        }
    }
}
