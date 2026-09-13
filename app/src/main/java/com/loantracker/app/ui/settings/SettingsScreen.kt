package com.loantracker.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loantracker.app.data.PasswordManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onVoltar: () -> Unit,
    onIrParaDefinirSenha: () -> Unit
) {
    val context = LocalContext.current
    // Recalculado sempre que esta tela é composta — o NavHost recompõe este
    // destino do zero ao voltar para ele, então isto reflete corretamente
    // uma senha recém-criada ou alterada.
    val temSenha = remember { PasswordManager.temSenha(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
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
                .padding(16.dp)
        ) {
            Button(onClick = onIrParaDefinirSenha, modifier = Modifier.fillMaxWidth()) {
                Text(if (temSenha) "Alterar senha" else "Adicionar senha")
            }
        }
    }
}
