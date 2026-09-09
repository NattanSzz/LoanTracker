package com.loantracker.app.ui.navigation

sealed class DeepLinkTarget {
    data class AbrirCliente(val clienteId: Long) : DeepLinkTarget()
    data object AbrirInfo : DeepLinkTarget()
}
