package com.loantracker.app.ui.detalhe

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.Parcela
import com.loantracker.app.data.SituacaoParcela
import com.loantracker.app.data.toBRL
import com.loantracker.app.domain.diasEmAtraso
import com.loantracker.app.domain.restanteCents
import com.loantracker.app.domain.situacao
import com.loantracker.app.ui.rememberViewModel
import com.loantracker.app.ui.theme.AlertRed
import com.loantracker.app.ui.theme.PrimaryGreen
import com.loantracker.app.ui.theme.WarningOrange
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun SituacaoParcela.label() = when (this) {
    SituacaoParcela.PENDENTE -> "Pendente"
    SituacaoParcela.PAGA -> "Paga"
    SituacaoParcela.PARCIALMENTE_PAGA -> "Parcialmente paga"
    SituacaoParcela.VENCIDA -> "Vencida"
}

private fun SituacaoParcela.cor(): Color = when (this) {
    SituacaoParcela.PENDENTE -> Color.Gray
    SituacaoParcela.PAGA -> PrimaryGreen
    SituacaoParcela.PARCIALMENTE_PAGA -> WarningOrange
    SituacaoParcela.VENCIDA -> AlertRed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheEmprestimoScreen(emprestimoId: Long, onVoltar: () -> Unit) {
    val viewModel = rememberViewModel { repo -> DetalheEmprestimoViewModel(repo, emprestimoId) }
    val estado by viewModel.estado.collectAsState()
    val emprestimo = estado.emprestimo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do empréstimo") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(estado.cliente?.nome ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (emprestimo != null) {
                            LinhaInfo("Valor emprestado", emprestimo.valorEmprestadoCents.toBRL())
                            LinhaInfo("Taxa de juros", "${emprestimo.taxaJurosPercentual}% por parcela")
                            LinhaInfo("Tipo de juros", if (emprestimo.tipoJuros.name == "SIMPLES") "Simples" else "Composto")
                            LinhaInfo("Data do empréstimo", emprestimo.dataEmprestimo.format(dateFormatter))
                            LinhaInfo("Quantidade de parcelas", emprestimo.quantidadeParcelas.toString())
                            LinhaInfo("Frequência", emprestimo.frequencia.name)
                            LinhaInfo("Primeiro vencimento", emprestimo.primeiroVencimento.format(dateFormatter))
                            estado.ultimoVencimento?.let { LinhaInfo("Último vencimento", it.format(dateFormatter)) }
                            LinhaInfo("Total de juros", emprestimo.totalJurosCents.toBRL())
                            LinhaInfo("Total a receber", emprestimo.totalAReceberCents.toBRL())
                            LinhaInfo("Total recebido", estado.totalPagoCents.toBRL())
                            LinhaInfo("Total restante", estado.totalRestanteCents.toBRL())
                        }
                    }
                }
            }

            item {
                Text("Cronograma de parcelas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(estado.parcelas, key = { it.id }) { parcela ->
                ParcelaCard(parcela)
            }
        }
    }
}

@Composable
private fun ParcelaCard(parcela: Parcela) {
    val situacao = parcela.situacao()
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Parcela ${parcela.numero}", fontWeight = FontWeight.Bold)
            LinhaInfo("Vencimento", parcela.vencimento.format(dateFormatter))
            LinhaInfo("Valor", parcela.valorCents.toBRL())
            LinhaInfo("Pago", parcela.valorPagoCents.toBRL())
            LinhaInfo("Restante", parcela.restanteCents().toBRL())
            Text(
                text = if (situacao == SituacaoParcela.VENCIDA) {
                    "Vencida há ${parcela.diasEmAtraso()} dia(s)"
                } else situacao.label(),
                color = situacao.cor(),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LinhaInfo(rotulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
