package com.loantracker.app.ui.info

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.BackupManager
import com.loantracker.app.data.InfoGeral
import com.loantracker.app.data.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InfoViewModel(private val repository: LoanRepository) : ViewModel() {
    val info: StateFlow<InfoGeral?> =
        repository.observarInfoGeral().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _mensagemBackup = MutableStateFlow<String?>(null)
    val mensagemBackup: StateFlow<String?> = _mensagemBackup

    fun exportarBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                BackupManager.exportar(context, repository, uri)
                _mensagemBackup.value = "Backup exportado com sucesso."
            } catch (e: Exception) {
                _mensagemBackup.value = "Erro ao exportar backup: ${e.message}"
            }
        }
    }

    fun importarBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                BackupManager.importar(context, repository, uri)
                _mensagemBackup.value = "Backup restaurado com sucesso."
            } catch (e: Exception) {
                _mensagemBackup.value = "Erro ao restaurar backup: ${e.message}"
            }
        }
    }

    fun limparMensagemBackup() {
        _mensagemBackup.value = null
    }
}
