package com.loantracker.app.notificacoes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.loantracker.app.MainActivity
import com.loantracker.app.R
import com.loantracker.app.data.DadosNotificacao
import com.loantracker.app.data.ParcelaParaNotificar
import com.loantracker.app.data.toBRL

/**
 * Cria os canais de notificação e monta/posta as notificações agrupadas por
 * categoria (vence amanhã / vence hoje / vencidas), usando somente recursos
 * locais do Android — sem servidor, sem Firebase.
 */
object NotificacaoHelper {

    const val CANAL_LEMBRETES = "lembretes_vencimento"
    const val CANAL_ATRASOS = "parcelas_vencidas"

    const val ID_AMANHA = 1
    const val ID_HOJE = 2
    const val ID_VENCIDAS = 3

    const val EXTRA_ABRIR_CLIENTE_ID = "abrir_cliente_id"
    const val EXTRA_ABRIR_INFO = "abrir_info"

    fun criarCanais(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(CANAL_LEMBRETES, "Lembretes de vencimento", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisos de parcelas vencendo hoje ou amanhã"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CANAL_ATRASOS, "Parcelas vencidas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos de parcelas em atraso"
            }
        )
    }

    fun notificarTudo(context: Context, dados: DadosNotificacao) {
        notificarCategoria(
            context = context,
            notificationId = ID_AMANHA,
            canal = CANAL_LEMBRETES,
            tituloSingular = "Parcela vencendo amanhã",
            tituloPlural = "Parcelas vencendo amanhã",
            itens = dados.vencemAmanha,
            mostrarAtraso = false
        )
        notificarCategoria(
            context = context,
            notificationId = ID_HOJE,
            canal = CANAL_LEMBRETES,
            tituloSingular = "Parcela vencendo hoje",
            tituloPlural = "Parcelas vencendo hoje",
            itens = dados.vencemHoje,
            mostrarAtraso = false
        )
        notificarCategoria(
            context = context,
            notificationId = ID_VENCIDAS,
            canal = CANAL_ATRASOS,
            tituloSingular = "Parcela vencida",
            tituloPlural = "Parcelas vencidas",
            itens = dados.vencidas,
            mostrarAtraso = true
        )
    }

    private fun notificarCategoria(
        context: Context,
        notificationId: Int,
        canal: String,
        tituloSingular: String,
        tituloPlural: String,
        itens: List<ParcelaParaNotificar>,
        mostrarAtraso: Boolean
    ) {
        val manager = NotificationManagerCompat.from(context)

        if (itens.isEmpty()) {
            manager.cancel(notificationId)
            return
        }

        val titulo = if (itens.size == 1) tituloSingular else tituloPlural
        val totalCents = itens.sumOf { it.valorRestanteCents }

        val estilo = NotificationCompat.InboxStyle().setBigContentTitle(titulo)
        itens.forEach { item ->
            val linha = "${item.clienteNome} — ${item.valorRestanteCents.toBRL()}" +
                if (mostrarAtraso) " (vencida há ${item.diasAtraso} dia(s))" else ""
            estilo.addLine(linha)
        }
        if (itens.size > 1) {
            estilo.setSummaryText("${itens.size} parcelas — ${totalCents.toBRL()}")
        }

        val intent = criarIntentDestino(context, itens)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacao = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(itens.first().let { "${it.clienteNome} — ${it.valorRestanteCents.toBRL()}" })
            .setStyle(estilo)
            .setPriority(if (mostrarAtraso) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val podeNotificar = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (podeNotificar) {
            manager.notify(notificationId, notificacao)
        }
    }

    private fun criarIntentDestino(context: Context, itens: List<ParcelaParaNotificar>): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (itens.size == 1) {
                putExtra(EXTRA_ABRIR_CLIENTE_ID, itens.first().clienteId)
            } else {
                putExtra(EXTRA_ABRIR_INFO, true)
            }
        }
    }
}
