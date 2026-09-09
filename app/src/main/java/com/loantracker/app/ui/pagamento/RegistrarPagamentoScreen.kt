package com.loantracker.app.ui.pagamento

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.Parcela
import com.loantracker.app.data.parseToCents
import com.loantracker.app.data.toBRL
import com.loantracker.app.data.toPlainDecimalString
import com.loantracker.app.domain.restanteCents
import com.loantracker.app.ui.components.DateField
import com.loantracker.app.ui.rememberViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarPagamentoScreen(onVoltar: () -> Unit, onPagamentoRegistrado: () -> Unit) {
    val viewModel = rememberViewModel { repo -> RegistrarPagamentoViewModel(repo) }
    val clienteSelecionado by viewModel.clienteSelecionado.collectAsState()
    val clientesFiltrados by viewModel.clientesFiltrados.collectAsState()
    val parcelasPendentes by viewModel.parcelasPendentes.collectAsState()

    var busca by remember { mutableStateOf("") }
    var parcelaSelecionada by remember { mutableStateOf<Parcela?>(null) }
    var valorTexto by remember { mutableStateOf("") }
    var dataPagamento by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar pagamento") },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            parcelaSelecionada != null -> parcelaSelecionada = null
                            clienteSelecionado != null -> viewModel.limparCliente()
                            else -> onVoltar()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when {
                clienteSelecionado == null -> {
                    OutlinedTextField(
                        value = busca,
                        onValueChange = {
                            busca = it
                            viewModel.atualizarBusca(it)
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        label = { Text("Buscar cliente") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    LazyColumn(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(clientesFiltrados, key = { it.id }) { cliente ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.selecionarCliente(cliente.id) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(cliente.nome, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }

                parcelaSelecionada == null -> {
                    Text(
                        text = clienteSelecionado?.nome ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (parcelasPendentes.isEmpty()) {
                        Text("Este cliente não possui parcelas pendentes.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(parcelasPendentes, key = { it.id }) { parcela ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        parcelaSelecionada = parcela
                                        valorTexto = (parcela.valorCents - parcela.valorPagoCents).toPlainDecimalString()
                                        dataPagamento = LocalDate.now()
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Parcela ${parcela.numero}", fontWeight = FontWeight.Bold)
                                        Text(parcela.vencimento.format(dateFormatter))
                                        Text(parcela.restanteCents().toBRL())
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    val parcela = parcelaSelecionada!!
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = valorTexto,
                            onValueChange = { valorTexto = it },
                            label = { Text("Valor pago") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        DateField(
                            label = "Data do pagamento",
                            data = dataPagamento,
                            onDataSelecionada = { dataPagamento = it }
                        )

                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Cliente: ${clienteSelecionado?.nome}")
                                Text("Parcela: ${parcela.numero}")
                                Text("Valor da parcela: ${parcela.valorCents.toBRL()}")
                                Text("Valor pago: ${parseToCents(valorTexto).toBRL()}")
                                Text("Data: ${dataPagamento.format(dateFormatter)}")
                            }
                        }

                        Button(
                            onClick = {
                                val valor = parseToCents(valorTexto)
                                if (valor > 0) {
                                    viewModel.registrarPagamento(parcela.id, valor, dataPagamento, onPagamentoRegistrado)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Registrar pagamento")
                        }
                    }
                }
            }
        }
    }
}
