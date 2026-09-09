package com.loantracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.loantracker.app.ui.cliente.CadastroClienteScreen
import com.loantracker.app.ui.detalhe.DetalheEmprestimoScreen
import com.loantracker.app.ui.emprestimo.CadastroEmprestimoScreen
import com.loantracker.app.ui.home.HomeScreen
import com.loantracker.app.ui.info.InfoScreen
import com.loantracker.app.ui.pagamento.RegistrarPagamentoScreen
import com.loantracker.app.ui.perfil.PerfilClienteScreen

object Routes {
    const val HOME = "home"
    const val INFO = "info"
    const val CADASTRO_CLIENTE = "cadastro_cliente"
    const val CADASTRO_EMPRESTIMO = "cadastro_emprestimo"
    const val REGISTRAR_PAGAMENTO = "registrar_pagamento"
    const val PERFIL_CLIENTE = "perfil_cliente/{clienteId}"
    const val DETALHE_EMPRESTIMO = "detalhe_emprestimo/{emprestimoId}"

    fun perfilCliente(id: Long) = "perfil_cliente/$id"
    fun detalheEmprestimo(id: Long) = "detalhe_emprestimo/$id"
    fun cadastroEmprestimo(clienteId: Long? = null) =
        if (clienteId != null) "$CADASTRO_EMPRESTIMO?clienteId=$clienteId" else CADASTRO_EMPRESTIMO
}

private data class AbaPrincipal(val rota: String, val titulo: String, val icone: androidx.compose.ui.graphics.vector.ImageVector)

private val abas = listOf(
    AbaPrincipal(Routes.HOME, "Início", Icons.Filled.Home),
    AbaPrincipal(Routes.INFO, "Informações", Icons.Filled.Assessment)
)

@Composable
fun LoanTrackerNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination

    val mostrarBottomBar = abas.any { it.rota == rotaAtual?.route }

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual?.hierarchy?.any { it.route == aba.rota } == true,
                            onClick = {
                                navController.navigate(aba.rota) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = aba.titulo) },
                            label = { Text(aba.titulo) }
                        )
                    }
                }
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
                    onNovoEmprestimo = { id -> navController.navigate(Routes.cadastroEmprestimo(id)) },
                    onAbrirEmprestimo = { id -> navController.navigate(Routes.detalheEmprestimo(id)) }
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
        }
    }
}
