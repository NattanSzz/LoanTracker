package com.loantracker.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.toBRL
import com.loantracker.app.ui.components.LinhaCliente
import com.loantracker.app.ui.rememberViewModel
import com.loantracker.app.ui.theme.CorFundoEscuro
import com.loantracker.app.ui.theme.CorSuperficieEscura

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
    val valorPendenteHoje by viewModel.valorPendenteHojeCents.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }
    var busca by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabeçalho escuro: destaque do dia, ação principal e busca.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CorFundoEscuro, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Valor pendente de hoje",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            valorPendenteHoje.toBRL(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpandido = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Adicionar", tint = Color.White)
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRegistrarPagamento,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = Color.Black.copy(alpha = 0.4f),
                            spotColor = Color.Black.copy(alpha = 0.4f)
                        )
                ) {
                    Text("Registrar pagamento", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = busca,
                    onValueChange = {
                        busca = it
                        viewModel.atualizarBusca(it)
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Buscar cliente", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CorSuperficieEscura,
                        unfocusedContainerColor = CorSuperficieEscura,
                        disabledContainerColor = CorSuperficieEscura,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Corpo claro: lista de clientes, minimalista, com divisor fino.
        if (resumos.isEmpty()) {
            Text(
                text = "Nenhum cliente cadastrado ainda. Toque em + para começar.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(20.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(resumos, key = { it.cliente.id }) { resumo ->
                    LinhaCliente(resumo = resumo, onClick = { onAbrirCliente(resumo.cliente.id) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}
