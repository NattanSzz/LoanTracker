package com.loantracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Bolinhas indicando quantos dígitos já foram digitados (limitado visualmente a 12). */
@Composable
fun IndicadorSenha(quantidadeDigitos: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(quantidadeDigitos.coerceAtMost(12)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/** Teclado numérico 0-9 + apagar, sem limite de dígitos digitáveis. */
@Composable
fun NumericKeypad(
    onDigito: (Char) -> Unit,
    onApagar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val linhas = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        linhas.forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                linha.forEach { digito ->
                    TeclaNumerica(texto = digito.toString(), onClick = { onDigito(digito) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(modifier = Modifier.size(64.dp))
            TeclaNumerica(texto = "0", onClick = { onDigito('0') })
            TeclaNumerica(onClick = onApagar) {
                Icon(Icons.Filled.Backspace, contentDescription = "Apagar")
            }
        }
    }
}

@Composable
private fun TeclaNumerica(
    texto: String? = null,
    onClick: () -> Unit,
    conteudo: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (texto != null) {
                Text(texto, style = MaterialTheme.typography.headlineSmall)
            }
            conteudo?.invoke()
        }
    }
}
