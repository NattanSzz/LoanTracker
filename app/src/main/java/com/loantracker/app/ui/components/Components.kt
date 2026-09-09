package com.loantracker.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.Cents
import com.loantracker.app.data.ClienteResumo
import com.loantracker.app.data.SituacaoCliente
import com.loantracker.app.data.toBRL
import com.loantracker.app.ui.theme.AlertRed
import com.loantracker.app.ui.theme.PrimaryGreen
import com.loantracker.app.ui.theme.WarningOrange
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

fun SituacaoCliente.label(): String = when (this) {
    SituacaoCliente.EM_DIA -> "Em dia"
    SituacaoCliente.VENCE_HOJE -> "🟡 Vence hoje"
    SituacaoCliente.VENCE_AMANHA -> "🟠 Vence amanhã"
    SituacaoCliente.EM_ATRASO -> "🔴 Em atraso"
}

fun SituacaoCliente.cor(): Color = when (this) {
    SituacaoCliente.EM_DIA -> PrimaryGreen
    SituacaoCliente.VENCE_HOJE -> WarningOrange
    SituacaoCliente.VENCE_AMANHA -> WarningOrange
    SituacaoCliente.EM_ATRASO -> AlertRed
}

@Composable
fun ClienteCard(resumo: ClienteResumo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = resumo.cliente.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${resumo.totalEmAbertoCents.toBRL()} em aberto",
                style = MaterialTheme.typography.bodyMedium
            )
            if (resumo.parcelasVencidas > 0) {
                Text(
                    text = "🔴 ${resumo.parcelasVencidas} parcela(s) em atraso",
                    color = AlertRed,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                resumo.proximoVencimento?.let { data ->
                    Text(
                        text = "Próximo: ${data.format(dateFormatter)} — ${resumo.proximoVencimentoValorCents.toBRL()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text(
                text = resumo.situacao.label(),
                color = resumo.situacao.cor(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun InfoStatCard(titulo: String, valor: String, corDestaque: Color = MaterialTheme.colorScheme.primary, subtitulo: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                color = corDestaque,
                fontWeight = FontWeight.Bold
            )
            subtitulo?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
