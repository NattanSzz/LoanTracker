package com.loantracker.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun <T> SeletorDropdown(
    label: String,
    opcoes: List<T>,
    selecionado: T?,
    textoDe: (T) -> String,
    onSelecionar: (T) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selecionado?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text(label)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expandido = true
                }
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = {
                expandido = false
            }
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = {
                        Text(textoDe(opcao))
                    },
                    onClick = {
                        onSelecionar(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}
