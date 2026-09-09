package com.loantracker.app.notificacoes

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val NOME_TRABALHO = "verificacao_parcelas_diaria"

    /** Horário-alvo da verificação diária: manhã, nunca de madrugada. */
    private const val HORA_EXECUCAO = 9

    fun agendarVerificacaoDiaria(context: Context) {
        val agora = LocalDateTime.now()
        var proximaExecucao = agora.withHour(HORA_EXECUCAO).withMinute(0).withSecond(0).withNano(0)
        if (!proximaExecucao.isAfter(agora)) {
            proximaExecucao = proximaExecucao.plusDays(1)
        }
        val atraso = Duration.between(agora, proximaExecucao)

        val pedido = PeriodicWorkRequestBuilder<VerificacaoParcelasWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(atraso.toMinutes(), TimeUnit.MINUTES)
            .build()

        // KEEP: se já existir um agendamento, não reinicia a contagem a cada
        // vez que o app é aberto — só agenda de verdade na primeira vez.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOME_TRABALHO,
            ExistingPeriodicWorkPolicy.KEEP,
            pedido
        )
    }
}
