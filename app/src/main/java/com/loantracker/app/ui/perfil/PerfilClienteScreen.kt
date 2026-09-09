package com.loantracker.app.ui.perfil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.toBRL
import com.loantracker.app.ui.rememberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilClienteScreen(
    clienteId: Long,
    onVoltar: () -> Unit,
    onNovoEmprestimo: (Long) -> Unit,
    onAbrirEmprestimo: (Long) -> Unit
) {
    val viewModel = rememberViewModel { repo -> PerfilClienteViewModel(repo, clienteId) }
    val estado by viewModel.estado.collectAsState()
    val cliente = estado.cliente

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cliente?.nome ?: "Cliente") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNovoEmprestimo(clienteId) }) {
                Icon(Icons.Filled.Add, contentDescription = "Novo empréstimo")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        cliente?.telefone?.let { Text("Telefone: $it") }
                        cliente?.cpf?.let { Text("CPF: $it") }
                        cliente?.endereco?.let { Text("Endereço: $it") }
                        cliente?.observacao?.let { Text("Obs: $it") }
                        if (cliente?.telefone == null && cliente?.cpf == null && cliente?.endereco == null && cliente?.observacao == null) {
                            Text("Sem dados adicionais cadastrados.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Resumo financeiro", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        LinhaValor("Total emprestado", estado.totalEmprestadoCents.toBRL())
                        LinhaValor("Total recebido", estado.totalRecebidoCents.toBRL())
                        LinhaValor("Total em aberto", estado.totalEmAbertoCents.toBRL())
                    }
                }
            }

            item {
                Text("Empréstimos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (estado.emprestimos.isEmpty()) {
                item { Text("Nenhum empréstimo cadastrado para este cliente ainda.") }
            }

            items(estado.emprestimos, key = { it.emprestimo.id }) { resumo ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onAbrirEmprestimo(resumo.emprestimo.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinhaValor("Emprestado", resumo.emprestimo.valorEmprestadoCents.toBRL())
                        LinhaValor("Total a receber", resumo.emprestimo.totalAReceberCents.toBRL())
                        LinhaValor("Recebido", resumo.totalPagoCents.toBRL())
                        LinhaValor("Restante", resumo.totalRestanteCents.toBRL())
                        Text(
                            text = when {
                                resumo.quitado -> "Quitado"
                                resumo.parcelasEmAtraso > 0 -> "${resumo.parcelasEmAtraso} parcela(s) em atraso"
                                else -> "Em andamento"
                            },
                            fontWeight = FontWeight.Bold,
                            color = when {
                                resumo.quitado -> MaterialTheme.colorScheme.primary
                                resumo.parcelasEmAtraso > 0 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaValor(rotulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
