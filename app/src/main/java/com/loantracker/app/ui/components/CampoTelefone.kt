package com.loantracker.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.loantracker.app.data.PhoneUtils

/**
 * Campo de telefone: guarda só os dígitos de DDD + número (sem código do
 * país) e mostra a máscara "(XX) XXXXX-XXXX" / "(XX) XXXX-XXXX" quando dá 10
 * ou 11 dígitos. Aceita colar um número com "-", "(" ou até com o "+55" na
 * frente (copiado do próprio WhatsApp) — tudo isso é filtrado
 * automaticamente.
 */
@Composable
fun CampoTelefone(
    digitos: String,
    onDigitosChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = digitos,
        onValueChange = { novoTexto -> onDigitosChange(PhoneUtils.normalizarEntrada(novoTexto)) },
        label = { Text("Telefone") },
        visualTransformation = TelefoneVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}
