package com.loantracker.app.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.PasswordManager
import com.loantracker.app.ui.components.IndicadorSenha
import com.loantracker.app.ui.components.NumericKeypad

@Composable
fun LockScreen(onDesbloqueado: () -> Unit) {
    val context = LocalContext.current
    var entrada by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Digite sua senha",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            IndicadorSenha(quantidadeDigitos = entrada.length)
            Spacer(modifier = Modifier.height(40.dp))
            NumericKeypad(
                onDigito = { digito ->
                    val novaEntrada = entrada + digito
                    entrada = novaEntrada
                    if (PasswordManager.verificarSenha(context, novaEntrada)) {
                        onDesbloqueado()
                    }
                },
                onApagar = {
                    if (entrada.isNotEmpty()) entrada = entrada.dropLast(1)
                }
            )
        }
    }
}
