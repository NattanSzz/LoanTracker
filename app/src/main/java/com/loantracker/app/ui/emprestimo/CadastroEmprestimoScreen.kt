package com.loantracker.app.ui.emprestimo

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.loantracker.app.data.FrequenciaParcela
import com.loantracker.app.data.TipoJuros
import com.loantracker.app.data.parseToCents
import com.loantracker.app.data.toBRL
import com.loantracker.app.ui.components.DateField
import com.loantracker.app.ui.components.SeletorDropdown
import com.loantracker.app.ui.rememberViewModel
import java.time.format.DateTimeFormatter

private fun FrequenciaParcela.label() = when (this) {
    FrequenciaParcela.DIARIA -> "Diária"
    FrequenciaParcela.SEMANAL -> "Semanal"
    FrequenciaParcela.QUINZENAL -> "Quinzenal"
    FrequenciaParcela.MENSAL -> "Mensal"
}

private fun TipoJuros.label() = when (this) {
    TipoJuros.SIMPLES -> "Juros simples"
    TipoJuros.COMPOSTO -> "Juros compostos"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroEmprestimoScreen(
    clientePreSelecionadoId: Long?,
    onVoltar: () -> Unit,
    onEmprestimoCadastrado: () -> Unit
) {
    val viewModel = rememberViewModel { repo -> CadastroEmprestimoViewModel(repo) }
    val clientes by viewModel.clientes.collectAsState()
    val form by viewModel.form.collectAsState()
    var mostrandoResumo by remember { mutableStateOf(false) }
    var valorTexto by remember { mutableStateOf("") }
    var taxaTexto by remember { mutableStateOf("") }
    var parcelasTexto by remember { mutableStateOf("1") }

    androidx.compose.runtime.LaunchedEffect(clientePreSelecionadoId) {
        if (clientePreSelecionadoId != null && form.clienteId == null) {
            viewModel.atualizar { it.copy(clienteId = clientePreSelecionadoId) }
        }
    }

    val clienteSelecionado = clientes.firstOrNull { it.id == form.clienteId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mostrandoResumo) "Confirmar empréstimo" else "Cadastrar empréstimo") },
                navigationIcon = {
                    IconButton(onClick = { if (mostrandoResumo) mostrandoResumo = false else onVoltar() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (!mostrandoResumo) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SeletorDropdown(
                    label = "Cliente",
                    opcoes = clientes,
                    selecionado = clienteSelecionado,
                    textoDe = { it.nome },
                    onSelecionar = { cliente -> viewModel.atualizar { it.copy(clienteId = cliente.id) } }
                )

                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = {
                        valorTexto = it
                        viewModel.atualizar { form -> form.copy(valorEmprestadoCents = parseToCents(it)) }
                    },
                    label = { Text("Valor emprestado") },
                    placeholder = { Text("R$ 1.000,00") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = taxaTexto,
                    onValueChange = {
                        taxaTexto = it
                        viewModel.atualizar { form -> form.copy(taxaPercentual = it.replace(",", ".").toDoubleOrNull() ?: 0.0) }
                    },
                    label = { Text("Juros (%)") },
                    placeholder = { Text("10") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Tipo de juros", style = MaterialTheme.typography.bodyMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TipoJuros.entries.forEachIndexed { index, tipo ->
                        SegmentedButton(
                            selected = form.tipoJuros == tipo,
                            onClick = { viewModel.atualizar { it.copy(tipoJuros = tipo) } },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(tipo.label())
                        }
                    }
                }

                OutlinedTextField(
                    value = parcelasTexto,
                    onValueChange = {
                        parcelasTexto = it
                        viewModel.atualizar { form -> form.copy(quantidadeParcelas = it.toIntOrNull() ?: 1) }
                    },
                    label = { Text("Quantidade de parcelas") },
                    placeholder = { Text("5") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                SeletorDropdown(
                    label = "Frequência das parcelas",
                    opcoes = FrequenciaParcela.entries,
                    selecionado = form.frequencia,
                    textoDe = { it.label() },
                    onSelecionar = { freq -> viewModel.atualizar { it.copy(frequencia = freq) } }
                )

                DateField(
                    label = "Data do empréstimo",
                    data = form.dataEmprestimo,
                    onDataSelecionada = { data -> viewModel.atualizar { it.copy(dataEmprestimo = data) } }
                )

                DateField(
                    label = "Primeiro vencimento",
                    data = form.primeiroVencimento,
                    onDataSelecionada = { data -> viewModel.atualizar { it.copy(primeiroVencimento = data) } }
                )

                Button(
                    onClick = { mostrandoResumo = true },
                    enabled = form.pronto,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)
                ) {
                    Text("Ver resumo")
                }
            }
        } else {
            val resumo = viewModel.calcularResumo()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinhaResumo("Cliente", clienteSelecionado?.nome ?: "-")
                LinhaResumo("Valor emprestado", form.valorEmprestadoCents.toBRL())
                LinhaResumo("Juros", "${form.taxaPercentual}% por parcela")
                LinhaResumo("Tipo", form.tipoJuros.label())
                LinhaResumo("Parcelas", form.quantidadeParcelas.toString())
                LinhaResumo("Frequência", form.frequencia.label())
                LinhaResumo("Data do empréstimo", form.dataEmprestimo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                LinhaResumo("Primeiro vencimento", form.primeiroVencimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                LinhaResumo("Total a receber", resumo?.totalAReceberCents?.toBRL() ?: "-", destaque = true)
                LinhaResumo("Valor de cada parcela", resumo?.valorParcelaCents?.toBRL() ?: "-", destaque = true)

                Button(
                    onClick = { viewModel.confirmar(onEmprestimoCadastrado) },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp)
                ) {
                    Text("Cadastrar empréstimo")
                }
            }
        }
    }
}

@Composable
private fun LinhaResumo(rotulo: String, valor: String, destaque: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = rotulo, style = MaterialTheme.typography.bodySmall)
        Text(
            text = valor,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Normal
        )
    }
}
