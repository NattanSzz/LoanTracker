package com.loantracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.ClienteResumo
import com.loantracker.app.data.SituacaoCliente
import com.loantracker.app.data.toBRL
import com.loantracker.app.ui.theme.AlertRed
import com.loantracker.app.ui.theme.CorAmarela
import com.loantracker.app.ui.theme.PrimaryGreen
import com.loantracker.app.ui.theme.WarningOrange
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

fun SituacaoCliente.label(): String = when (this) {
    SituacaoCliente.EM_DIA -> "Em dia"
    SituacaoCliente.VENCE_HOJE -> "Vence hoje"
    SituacaoCliente.VENCE_AMANHA -> "Vence amanhã"
    SituacaoCliente.EM_ATRASO -> "Em atraso"
}

fun SituacaoCliente.cor(): Color = when (this) {
    SituacaoCliente.EM_DIA -> PrimaryGreen
    SituacaoCliente.VENCE_HOJE -> CorAmarela
    SituacaoCliente.VENCE_AMANHA -> WarningOrange
    SituacaoCliente.EM_ATRASO -> AlertRed
}

/**
 * Linha de cliente da tela inicial: nome, próximo vencimento e uma bolinha
 * colorida indicando a situação — sem cartão, sem texto de status, sem total
 * em aberto. O visual minimalista é proposital (mockup do usuário).
 */
@Composable
fun LinhaCliente(resumo: ClienteResumo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = resumo.cliente.nome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            resumo.proximoVencimento?.let { data ->
                Text(
                    text = "Próx: ${data.format(dateFormatter)} - ${resumo.proximoVencimentoValorCents.toBRL()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.size(18.dp).clip(CircleShape).background(resumo.situacao.cor())
                .semantics { contentDescription = resumo.situacao.label() }
        ) {}
    }
}

@Composable
fun InfoStatCard(titulo: String, valor: String, corDestaque: Color = MaterialTheme.colorScheme.primary, subtitulo: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
