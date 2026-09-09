package com.loantracker.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.loantracker.app.notificacoes.NotificacaoHelper
import com.loantracker.app.ui.navigation.DeepLinkTarget
import com.loantracker.app.ui.navigation.LoanTrackerNavGraph
import com.loantracker.app.ui.theme.LoanTrackerTheme

class MainActivity : ComponentActivity() {

    private var deepLink by mutableStateOf<DeepLinkTarget?>(null)

    private val solicitarPermissaoNotificacao =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* segue normalmente mesmo se negado */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pedirPermissaoDeNotificacaoSeNecessario()
        deepLink = extrairDeepLink(intent)

        setContent {
            LoanTrackerRoot(
                deepLink = deepLink,
                onDeepLinkConsumido = { deepLink = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLink = extrairDeepLink(intent)
    }

    private fun pedirPermissaoDeNotificacaoSeNecessario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val jaConcedida = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!jaConcedida) {
                solicitarPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun extrairDeepLink(intent: Intent?): DeepLinkTarget? {
        if (intent == null) return null
        val clienteId = intent.getLongExtra(NotificacaoHelper.EXTRA_ABRIR_CLIENTE_ID, -1L)
        if (clienteId >= 0) return DeepLinkTarget.AbrirCliente(clienteId)
        if (intent.getBooleanExtra(NotificacaoHelper.EXTRA_ABRIR_INFO, false)) return DeepLinkTarget.AbrirInfo
        return null
    }
}

@Composable
fun LoanTrackerRoot(
    deepLink: DeepLinkTarget? = null,
    onDeepLinkConsumido: () -> Unit = {}
) {
    LoanTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoanTrackerNavGraph(deepLink = deepLink, onDeepLinkConsumido = onDeepLinkConsumido)
        }
    }
}
