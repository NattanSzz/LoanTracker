package com.loantracker.app.notificacoes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.loantracker.app.LoanTrackerApp

/**
 * Verificação diária das parcelas: busca no banco local quais vencem amanhã,
 * quais vencem hoje e quais estão vencidas, e posta as notificações
 * correspondentes. Não depende de internet, servidor ou Firebase.
 */
class VerificacaoParcelasWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = (applicationContext as LoanTrackerApp).repository
            val dados = repository.buscarDadosParaNotificacao()
            NotificacaoHelper.notificarTudo(applicationContext, dados)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
