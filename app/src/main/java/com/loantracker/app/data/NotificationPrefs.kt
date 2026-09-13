package com.loantracker.app.data

import android.content.Context

/** Horário em que a verificação diária (notificações + backup automático) deve rodar. */
object NotificationPrefs {
    private const val PREFS = "notificacao_prefs"
    private const val CHAVE_HORA = "hora_notificacao"
    private const val CHAVE_MINUTO = "minuto_notificacao"

    const val HORA_PADRAO = 9
    const val MINUTO_PADRAO = 0

    fun obterHorario(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val hora = prefs.getInt(CHAVE_HORA, HORA_PADRAO)
        val minuto = prefs.getInt(CHAVE_MINUTO, MINUTO_PADRAO)
        return hora to minuto
    }

    fun definirHorario(context: Context, hora: Int, minuto: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(CHAVE_HORA, hora)
            .putInt(CHAVE_MINUTO, minuto)
            .apply()
    }
}
