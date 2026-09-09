package com.loantracker.app

import android.app.Application
import com.loantracker.app.data.AppDatabase
import com.loantracker.app.data.LoanRepository
import com.loantracker.app.notificacoes.NotificacaoHelper
import com.loantracker.app.notificacoes.NotificationScheduler

class LoanTrackerApp : Application() {

    lateinit var repository: LoanRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = LoanRepository(
            db = db,
            clienteDao = db.clienteDao(),
            emprestimoDao = db.emprestimoDao(),
            parcelaDao = db.parcelaDao(),
            pagamentoDao = db.pagamentoDao()
        )

        NotificacaoHelper.criarCanais(this)
        NotificationScheduler.agendarVerificacaoDiaria(this)
    }
}
