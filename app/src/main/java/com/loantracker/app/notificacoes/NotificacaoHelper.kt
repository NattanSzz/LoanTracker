package com.loantracker.app.notificacoes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
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

    // IDs novos (com sufixo _v2): uma vez que um canal é criado no aparelho,
    // suas configurações de som/importância ficam travadas pra sempre — não
    // dá pra "atualizar" um canal existente por código. Trocar o ID força o
    // Android a criar um canal novo já com som e heads-up habilitados.
    const val CANAL_LEMBRETES = "lembretes_vencimento_v2"
    const val CANAL_ATRASOS = "parcelas_vencidas_v2"

    private const val CANAL_LEMBRETES_ANTIGO = "lembretes_vencimento"
    private const val CANAL_ATRASOS_ANTIGO = "parcelas_vencidas"

    const val ID_AMANHA = 1
    const val ID_HOJE = 2
    const val ID_VENCIDAS = 3

    const val EXTRA_ABRIR_CLIENTE_ID = "abrir_cliente_id"
    const val EXTRA_ABRIR_INFO = "abrir_info"

    fun criarCanais(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        // Remove os canais antigos (criados por uma versão anterior do app,
        // sem som configurado) pra não deixar duplicado nas configurações do
        // sistema e pra garantir que o canal novo (com som) seja o usado.
        manager.deleteNotificationChannel(CANAL_LEMBRETES_ANTIGO)
        manager.deleteNotificationChannel(CANAL_ATRASOS_ANTIGO)

        val somPadrao = Settings.System.DEFAULT_NOTIFICATION_URI
        val atributosDeAudio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // IMPORTANCE_HIGH nos dois canais: é o que faz a notificação tocar som
        // E aparecer como pop-up (heads-up) na tela, tipo WhatsApp.
        manager.createNotificationChannel(
            NotificationChannel(CANAL_LEMBRETES, "Lembretes de vencimento", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos de parcelas vencendo hoje ou amanhã"
                setSound(somPadrao, atributosDeAudio)
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CANAL_ATRASOS, "Parcelas vencidas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos de parcelas em atraso"
                setSound(somPadrao, atributosDeAudio)
                enableVibration(true)
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
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            // PRIORITY_HIGH é ignorado a partir do Android 8 (quem manda é o
            // canal), mas mantém o comportamento correto em qualquer cenário.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
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
