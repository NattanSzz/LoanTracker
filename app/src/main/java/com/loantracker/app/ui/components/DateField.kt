package com.loantracker.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Campo de data somente leitura que abre o seletor ao ser tocado.
 *
 * IMPORTANTE: um OutlinedTextField com readOnly=true ainda intercepta o
 * toque para posicionar o cursor, então um `.clickable` direto nele não
 * é acionado de forma confiável. A correção é sobrepor uma camada
 * transparente e clicável por cima do campo (Box), que captura o toque
 * antes que ele chegue ao campo de texto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, data: LocalDate, onDataSelecionada: (LocalDate) -> Unit) {
    var mostrarDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = data.format(displayFormatter),
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { mostrarDialog = true }
        )
    }

    if (mostrarDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val novaData = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onDataSelecionada(novaData)
                    }
                    mostrarDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialog = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
