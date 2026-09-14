package com.loantracker.app.ui.detalhe

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.Parcela
import com.loantracker.app.data.PhoneUtils
import com.loantracker.app.data.SituacaoParcela
import com.loantracker.app.data.TipoJuros
import com.loantracker.app.data.toBRL
import com.loantracker.app.domain.diasEmAtraso
import com.loantracker.app.domain.restanteCents
import com.loantracker.app.domain.situacao
import com.loantracker.app.ui.rememberViewModel
import com.loantracker.app.ui.theme.AlertRed
import com.loantracker.app.ui.theme.PrimaryGreen
import com.loantracker.app.ui.theme.WarningOrange
import java.time.LocalDate
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

private fun TipoJuros.label() = when (this) {
    TipoJuros.SIMPLES -> "Simples"
    TipoJuros.ALUGUEL -> "Aluguel"
    TipoJuros.COMPOSTO -> "Composto"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheEmprestimoScreen(emprestimoId: Long, onVoltar: () -> Unit) {
    val viewModel = rememberViewModel { repo -> DetalheEmprestimoViewModel(repo, emprestimoId) }
    val estado by viewModel.estado.collectAsState()
    val emprestimo = estado.emprestimo
    var mostrarConfirmacaoQuitar by remember { mutableStateOf(false) }

    val ehAluguel = emprestimo?.tipoJuros == TipoJuros.ALUGUEL

    if (mostrarConfirmacaoQuitar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoQuitar = false },
            title = { Text("Quitar empréstimo") },
            text = { Text("Todas as parcelas serão marcadas como pagas e nenhuma parcela nova será gerada. Deseja continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacaoQuitar = false
                    viewModel.quitarEmprestimo()
                }) { Text("Quitar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoQuitar = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(emprestimo?.titulo?.ifBlank { "Detalhes do empréstimo" } ?: "Detalhes do empréstimo") },
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
                            LinhaInfo("Taxa de juros", "${emprestimo.taxaJurosPercentual}%")
                            LinhaInfo("Tipo de juros", emprestimo.tipoJuros.label())
                            if (emprestimo.descontoPorParcelaCents > 0) {
                                LinhaInfo("Desconto por parcela", "- ${emprestimo.descontoPorParcelaCents.toBRL()}")
                            }
                            LinhaInfo("Data do empréstimo", emprestimo.dataEmprestimo.format(dateFormatter))
                            if (!ehAluguel) {
                                LinhaInfo("Quantidade de parcelas", emprestimo.quantidadeParcelas.toString())
                            }
                            LinhaInfo("Frequência", emprestimo.frequencia.name)
                            LinhaInfo("Primeiro vencimento", emprestimo.primeiroVencimento.format(dateFormatter))
                            if (!ehAluguel) {
                                estado.ultimoVencimento?.let { LinhaInfo("Último vencimento", it.format(dateFormatter)) }
                                LinhaInfo("Total de juros", emprestimo.totalJurosCents.toBRL())
                                LinhaInfo("Total a receber", emprestimo.totalAReceberCents.toBRL())
                            } else {
                                LinhaInfo("Valor do aluguel (por período)", emprestimo.valorParcelaCents.toBRL())
                            }
                            LinhaInfo("Total recebido", estado.totalPagoCents.toBRL())
                            if (!ehAluguel) {
                                LinhaInfo("Total restante", estado.totalRestanteCents.toBRL())
                            }
                        }
                    }
                }
            }

            item {
                Text("Cronograma de parcelas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(estado.parcelas, key = { it.id }) { parcela ->
                ParcelaCard(parcela, estado.cliente?.telefone)
            }

            if (ehAluguel && emprestimo?.quitadoManualmente == false) {
                item {
                    Button(
                        onClick = { mostrarConfirmacaoQuitar = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Quitar empréstimo")
                    }
                }
            }
        }
    }
}

@Composable
private fun ParcelaCard(parcela: Parcela, telefoneCliente: String?) {
    val hoje = remember { LocalDate.now() }
    val situacao = parcela.situacao(hoje)
    val context = LocalContext.current

    val mostrarWhatsApp = situacao != SituacaoParcela.PAGA &&
        (situacao == SituacaoParcela.VENCIDA || parcela.vencimento == hoje || parcela.vencimento == hoje.plusDays(1))

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Parcela ${parcela.numero}", fontWeight = FontWeight.Bold)
            LinhaInfo("Vencimento", parcela.vencimento.format(dateFormatter))
            LinhaInfo("Valor", parcela.valorCents.toBRL())
            LinhaInfo("Pago", parcela.valorPagoCents.toBRL())
            LinhaInfo("Restante", parcela.restanteCents().toBRL())
            Text(
                text = if (situacao == SituacaoParcela.VENCIDA) {
                    "Vencida há ${parcela.diasEmAtraso(hoje)} dia(s)"
                } else situacao.label(),
                color = situacao.cor(),
                fontWeight = FontWeight.Bold
            )

            if (mostrarWhatsApp) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        val numero = PhoneUtils.paraWhatsApp(telefoneCliente)
                        if (numero == null) {
                            Toast.makeText(context, "Telefone inválido ou não cadastrado para este cliente.", Toast.LENGTH_SHORT).show()
                        } else {
                            val mensagem = construirMensagemLembrete(parcela, situacao, hoje)
                            val url = "https://wa.me/$numero?text=${Uri.encode(mensagem)}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            }
        }
    }
}

/** Lembrete curto e simples — só valor, data e situação, sem detalhes do empréstimo. */
private fun construirMensagemLembrete(parcela: Parcela, situacao: SituacaoParcela, hoje: LocalDate): String {
    val valor = parcela.restanteCents().toBRL()
    val data = parcela.vencimento.format(dateFormatter)
    return when {
        situacao == SituacaoParcela.VENCIDA -> {
            val dias = parcela.diasEmAtraso(hoje)
            "Olá! Passando pra lembrar que o pagamento de $valor (venceu em $data) está em atraso há $dias dia(s)."
        }
        parcela.vencimento == hoje -> "Olá! Só um lembrete: o pagamento de $valor vence hoje ($data)."
        parcela.vencimento == hoje.plusDays(1) -> "Olá! Passando pra lembrar que o pagamento de $valor vence amanhã ($data)."
        else -> "Olá! Lembrete de pagamento: $valor, com vencimento em $data."
    }
}

@Composable
private fun LinhaInfo(rotulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
