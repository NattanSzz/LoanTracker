package com.loantracker.app.ui.info

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.toBRL
import com.loantracker.app.ui.components.InfoStatCard
import com.loantracker.app.ui.rememberViewModel
import com.loantracker.app.ui.theme.AlertRed
import com.loantracker.app.ui.theme.PrimaryGreen
import com.loantracker.app.ui.theme.WarningOrange
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen() {
    val viewModel = rememberViewModel { repo -> InfoViewModel(repo) }
    val info by viewModel.info.collectAsState()
    val mensagemBackup by viewModel.mensagemBackup.collectAsState()
    var mostrarClientesEmAtraso by remember { mutableStateOf(false) }
    var mostrarConfirmacaoRestaurar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportarBackup(context, it) } }

    val importarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importarBackup(context, it) } }

    LaunchedEffect(mensagemBackup) {
        mensagemBackup?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagemBackup()
        }
    }

    if (mostrarConfirmacaoRestaurar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoRestaurar = false },
            title = { Text("Restaurar backup") },
            text = { Text("Isso vai substituir todos os dados atuais do aplicativo pelos dados do arquivo escolhido. Essa ação não pode ser desfeita. Deseja continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacaoRestaurar = false
                    importarLauncher.launch(arrayOf("application/json"))
                }) { Text("Restaurar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoRestaurar = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Informações") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val atual = info
        if (atual == null) return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { InfoStatCard("Total emprestado este ano", atual.totalEmprestadoAnoCents.toBRL()) }
            item { InfoStatCard("Total recebido este ano", atual.totalRecebidoAnoCents.toBRL(), corDestaque = PrimaryGreen) }
            item { InfoStatCard("Juros recebidos no ano", atual.jurosRecebidosAnoCents.toBRL(), corDestaque = PrimaryGreen) }
            item { InfoStatCard("Total em aberto", atual.totalEmAbertoCents.toBRL(), corDestaque = WarningOrange) }

            item {
                Text("Situação das parcelas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                InfoStatCard(
                    "Parcelas vencidas",
                    "${atual.parcelasVencidasQtd} parcela(s)",
                    corDestaque = AlertRed,
                    subtitulo = atual.parcelasVencidasValorCents.toBRL()
                )
            }
            item {
                InfoStatCard(
                    "Vencem hoje",
                    "${atual.venceHojeQtd} parcela(s)",
                    corDestaque = WarningOrange,
                    subtitulo = atual.venceHojeValorCents.toBRL()
                )
            }
            item {
                InfoStatCard(
                    "Vencem amanhã",
                    "${atual.venceAmanhaQtd} parcela(s)",
                    corDestaque = WarningOrange,
                    subtitulo = atual.venceAmanhaValorCents.toBRL()
                )
            }
            item {
                InfoStatCard(
                    "Próximos 7 dias",
                    "${atual.proximos7DiasQtd} parcela(s)",
                    subtitulo = atual.proximos7DiasValorCents.toBRL()
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { mostrarClientesEmAtraso = !mostrarClientesEmAtraso },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Clientes em atraso", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${atual.clientesEmAtraso.size} cliente(s)",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AlertRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (mostrarClientesEmAtraso) {
                items(atual.clientesEmAtraso, key = { it.cliente.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(item.cliente.nome, fontWeight = FontWeight.Bold)
                            Text("${item.parcelasVencidasQtd} parcela(s) vencida(s)")
                            Text(item.valorVencidoCents.toBRL(), color = AlertRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Salve uma cópia dos seus dados (clientes, empréstimos, parcelas e pagamentos) onde quiser: Google Drive, Downloads, pendrive, etc.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                exportarLauncher.launch("controle_emprestimos_backup_${LocalDate.now()}.json")
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Exportar") }
                        OutlinedButton(
                            onClick = { mostrarConfirmacaoRestaurar = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Restaurar") }
                    }
                }
            }
        }
    }
}
