package com.loantracker.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.window.Dialog
import com.loantracker.app.data.NotificationPrefs
import com.loantracker.app.data.PasswordManager
import com.loantracker.app.notificacoes.NotificationScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onIrParaDefinirSenha: () -> Unit
) {
    val context = LocalContext.current
    // Recalculado sempre que esta tela é composta — o NavHost recompõe este
    // destino do zero ao voltar pra ela, então isto reflete corretamente uma
    // senha recém-criada ou alterada.
    val temSenha = remember { PasswordManager.temSenha(context) }

    var horarioAtual by remember { mutableStateOf(NotificationPrefs.obterHorario(context)) }
    var mostrarSeletorHorario by remember { mutableStateOf(false) }

    if (mostrarSeletorHorario) {
        SeletorDeHorarioDialog(
            horaInicial = horarioAtual.first,
            minutoInicial = horarioAtual.second,
            onConfirmar = { hora, minuto ->
                NotificationPrefs.definirHorario(context, hora, minuto)
                NotificationScheduler.reagendarComNovoHorario(context)
                horarioAtual = hora to minuto
                mostrarSeletorHorario = false
            },
            onCancelar = { mostrarSeletorHorario = false }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Text("Configurações", style = MaterialTheme.typography.headlineSmall)

            Button(onClick = onIrParaDefinirSenha, modifier = Modifier.fillMaxWidth()) {
                Text(if (temSenha) "Alterar senha" else "Adicionar senha")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Horário das notificações", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "As verificações diárias (avisos de vencimento e backup automático) acontecem nesse horário.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
                    onClick = { mostrarSeletorHorario = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("%02d:%02d".format(horarioAtual.first, horarioAtual.second))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorDeHorarioDialog(
    horaInicial: Int,
    minutoInicial: Int,
    onConfirmar: (Int, Int) -> Unit,
    onCancelar: () -> Unit
) {
    val estado = rememberTimePickerState(initialHour = horaInicial, initialMinute = minutoInicial, is24Hour = true)

    Dialog(onDismissRequest = onCancelar) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Horário das notificações", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = estado)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancelar) { Text("Cancelar") }
                    TextButton(onClick = { onConfirmar(estado.hour, estado.minute) }) { Text("OK") }
                }
            }
        }
    }
}
