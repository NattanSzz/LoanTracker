package com.loantracker.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.PasswordManager
import com.loantracker.app.ui.components.IndicadorSenha
import com.loantracker.app.ui.components.NumericKeypad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinirSenhaScreen(
    onVoltar: () -> Unit,
    onSenhaSalva: () -> Unit
) {
    val context = LocalContext.current
    var senha by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Definir senha") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Digite a nova senha numérica", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            IndicadorSenha(quantidadeDigitos = senha.length)
            Spacer(modifier = Modifier.height(32.dp))
            NumericKeypad(
                onDigito = { digito -> senha += digito },
                onApagar = { if (senha.isNotEmpty()) senha = senha.dropLast(1) }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    PasswordManager.definirSenha(context, senha)
                    onSenhaSalva()
                },
                enabled = senha.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalizar")
            }
        }
    }
}
