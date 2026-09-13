package com.loantracker.app.notificacoes

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.loantracker.app.data.NotificationPrefs
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val NOME_TRABALHO = "verificacao_parcelas_diaria"

    /** Chamado na abertura do app — não reinicia a contagem se já houver um agendamento. */
    fun agendarVerificacaoDiaria(context: Context) {
        agendar(context, ExistingPeriodicWorkPolicy.KEEP)
    }

    /** Chamado quando o usuário muda o horário nas Configurações — força o reagendamento. */
    fun reagendarComNovoHorario(context: Context) {
        agendar(context, ExistingPeriodicWorkPolicy.UPDATE)
    }

    private fun agendar(context: Context, politica: ExistingPeriodicWorkPolicy) {
        val (horaConfigurada, minutoConfigurado) = NotificationPrefs.obterHorario(context)

        val agora = LocalDateTime.now()
        var proximaExecucao = agora.withHour(horaConfigurada).withMinute(minutoConfigurado).withSecond(0).withNano(0)
        if (!proximaExecucao.isAfter(agora)) {
            proximaExecucao = proximaExecucao.plusDays(1)
        }
        val atraso = Duration.between(agora, proximaExecucao)

        val pedido = PeriodicWorkRequestBuilder<VerificacaoParcelasWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(atraso.toMinutes(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(NOME_TRABALHO, politica, pedido)
    }
}
