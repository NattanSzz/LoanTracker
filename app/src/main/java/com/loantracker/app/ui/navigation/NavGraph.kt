package com.loantracker.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loantracker.app.ui.cliente.CadastroClienteScreen
import com.loantracker.app.ui.cliente.EditarClienteScreen
import com.loantracker.app.ui.detalhe.DetalheEmprestimoScreen
import com.loantracker.app.ui.emprestimo.CadastroEmprestimoScreen
import com.loantracker.app.ui.home.HomeScreen
import com.loantracker.app.ui.info.InfoScreen
import com.loantracker.app.ui.pagamento.RegistrarPagamentoScreen
import com.loantracker.app.ui.perfil.PerfilClienteScreen
import com.loantracker.app.ui.settings.DefinirSenhaScreen
import com.loantracker.app.ui.settings.SettingsScreen
import com.loantracker.app.ui.theme.CorFundoEscuro

object Routes {
    const val HOME = "home"
    const val INFO = "info"
    const val CADASTRO_CLIENTE = "cadastro_cliente"
    const val CADASTRO_EMPRESTIMO = "cadastro_emprestimo"
    const val REGISTRAR_PAGAMENTO = "registrar_pagamento"
    const val PERFIL_CLIENTE = "perfil_cliente/{clienteId}"
    const val EDITAR_CLIENTE = "editar_cliente/{clienteId}"
    const val DETALHE_EMPRESTIMO = "detalhe_emprestimo/{emprestimoId}"
    const val CONFIGURACOES = "configuracoes"
    const val DEFINIR_SENHA = "definir_senha"

    fun perfilCliente(id: Long) = "perfil_cliente/$id"
    fun editarCliente(id: Long) = "editar_cliente/$id"
    fun detalheEmprestimo(id: Long) = "detalhe_emprestimo/$id"
    fun cadastroEmprestimo(clienteId: Long? = null) =
        if (clienteId != null) "$CADASTRO_EMPRESTIMO?clienteId=$clienteId" else CADASTRO_EMPRESTIMO
}

private data class AbaPrincipal(val rota: String, val titulo: String, val icone: ImageVector)

// Configurações agora é uma aba da navegação inferior, junto com Início e Informações.
private val abas = listOf(
    AbaPrincipal(Routes.HOME, "Início", Icons.Filled.Home),
    AbaPrincipal(Routes.INFO, "Informações", Icons.Filled.Assessment),
    AbaPrincipal(Routes.CONFIGURACOES, "Configurações", Icons.Filled.Settings)
)

@Composable
fun LoanTrackerNavGraph(
    deepLink: DeepLinkTarget? = null,
    onDeepLinkConsumido: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination

    val mostrarBottomBar = abas.any { it.rota == rotaAtual?.route }

    LaunchedEffect(deepLink) {
        when (deepLink) {
            is DeepLinkTarget.AbrirCliente -> {
                navController.navigate(Routes.perfilCliente(deepLink.clienteId)) {
                    launchSingleTop = true
                }
                onDeepLinkConsumido()
            }
            DeepLinkTarget.AbrirInfo -> {
                navController.navigate(Routes.INFO) {
                    launchSingleTop = true
                }
                onDeepLinkConsumido()
            }
            null -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                BarraNavegacaoFlutuante(
                    abas = abas,
                    selecionada = { aba -> rotaAtual?.hierarchy?.any { it.route == aba.rota } == true },
                    onSelecionar = { aba ->
                        navController.navigate(aba.rota) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onCadastrarCliente = { navController.navigate(Routes.CADASTRO_CLIENTE) },
                    onCadastrarEmprestimo = { navController.navigate(Routes.cadastroEmprestimo()) },
                    onRegistrarPagamento = { navController.navigate(Routes.REGISTRAR_PAGAMENTO) },
                    onAbrirCliente = { id -> navController.navigate(Routes.perfilCliente(id)) }
                )
            }
            composable(Routes.INFO) {
                InfoScreen()
            }
            composable(Routes.CADASTRO_CLIENTE) {
                CadastroClienteScreen(
                    onVoltar = { navController.popBackStack() },
                    onClienteCadastrado = { id ->
                        navController.popBackStack()
                        navController.navigate(Routes.perfilCliente(id))
                    }
                )
            }
            composable(
                route = "${Routes.CADASTRO_EMPRESTIMO}?clienteId={clienteId}",
                arguments = listOf(navArgument("clienteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { entry ->
                val clienteId = entry.arguments?.getLong("clienteId")?.takeIf { it >= 0 }
                CadastroEmprestimoScreen(
                    clientePreSelecionadoId = clienteId,
                    onVoltar = { navController.popBackStack() },
                    onEmprestimoCadastrado = { navController.popBackStack() }
                )
            }
            composable(Routes.REGISTRAR_PAGAMENTO) {
                RegistrarPagamentoScreen(
                    onVoltar = { navController.popBackStack() },
                    onPagamentoRegistrado = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.PERFIL_CLIENTE,
                arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
            ) { entry ->
                val clienteId = entry.arguments?.getLong("clienteId") ?: return@composable
                PerfilClienteScreen(
                    clienteId = clienteId,
                    onVoltar = { navController.popBackStack() },
                    onEditarCliente = { id -> navController.navigate(Routes.editarCliente(id)) },
                    onNovoEmprestimo = { id -> navController.navigate(Routes.cadastroEmprestimo(id)) },
                    onAbrirEmprestimo = { id -> navController.navigate(Routes.detalheEmprestimo(id)) }
                )
            }
            composable(
                route = Routes.EDITAR_CLIENTE,
                arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
            ) { entry ->
                val clienteId = entry.arguments?.getLong("clienteId") ?: return@composable
                EditarClienteScreen(
                    clienteId = clienteId,
                    onVoltar = { navController.popBackStack() },
                    onClienteSalvo = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.DETALHE_EMPRESTIMO,
                arguments = listOf(navArgument("emprestimoId") { type = NavType.LongType })
            ) { entry ->
                val emprestimoId = entry.arguments?.getLong("emprestimoId") ?: return@composable
                DetalheEmprestimoScreen(
                    emprestimoId = emprestimoId,
                    onVoltar = { navController.popBackStack() }
                )
            }
            composable(Routes.CONFIGURACOES) {
                SettingsScreen(
                    onIrParaDefinirSenha = { navController.navigate(Routes.DEFINIR_SENHA) }
                )
            }
            composable(Routes.DEFINIR_SENHA) {
                DefinirSenhaScreen(
                    onVoltar = { navController.popBackStack() },
                    onSenhaSalva = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Barra inferior flutuante em formato de pílula escura, com um anel branco no item selecionado. */
@Composable
private fun BarraNavegacaoFlutuante(
    abas: List<AbaPrincipal>,
    selecionada: (AbaPrincipal) -> Boolean,
    onSelecionar: (AbaPrincipal) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = CorFundoEscuro,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                abas.forEach { aba ->
                    val ativa = selecionada(aba)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .then(
                                if (ativa) Modifier.border(1.5.dp, Color.White, CircleShape) else Modifier
                            )
                            .clickable { onSelecionar(aba) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(aba.icone, contentDescription = aba.titulo, tint = Color.White)
                    }
                }
            }
        }
    }
}
