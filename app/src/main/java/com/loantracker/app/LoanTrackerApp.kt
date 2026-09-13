package com.loantracker.app

import android.app.Application
import com.loantracker.app.data.AppDatabase
import com.loantracker.app.data.LoanRepository
import com.loantracker.app.notificacoes.NotificacaoHelper
import com.loantracker.app.notificacoes.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LoanTrackerApp : Application() {

    lateinit var repository: LoanRepository
        private set

    /** Escopo de vida longa para tarefas em segundo plano que não pertencem a uma tela específica. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        // Processa empréstimos do tipo Aluguel logo na abertura do app também,
        // sem esperar a verificação diária em segundo plano.
        applicationScope.launch {
            repository.processarEmprestimosAluguel()
        }
    }
}
