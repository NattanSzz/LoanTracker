package com.loantracker.app.notificacoes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.loantracker.app.LoanTrackerApp
import com.loantracker.app.data.BackupManager

/**
 * Verificação diária: processa empréstimos do tipo Aluguel (auto-pagamento e
 * geração da próxima parcela), faz o backup automático (se uma pasta de
 * destino já foi escolhida) e por fim posta as notificações de parcelas
 * vencendo amanhã, hoje ou vencidas. Não depende de internet, servidor ou
 * Firebase.
 */
class VerificacaoParcelasWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as LoanTrackerApp
            val repository = app.repository

            repository.processarEmprestimosAluguel()
            BackupManager.executarBackupAutomaticoSeConfigurado(applicationContext, repository)

            val dados = repository.buscarDadosParaNotificacao()
            NotificacaoHelper.notificarTudo(applicationContext, dados)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
