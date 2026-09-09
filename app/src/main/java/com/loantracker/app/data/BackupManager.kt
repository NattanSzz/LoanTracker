package com.loantracker.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object BackupManager {

    suspend fun exportar(context: Context, repository: LoanRepository, uri: Uri) {
        withContext(Dispatchers.IO) {
            val json = repository.exportarParaJson()
            context.contentResolver.openOutputStream(uri)?.use { saida ->
                saida.write(json.toByteArray(Charsets.UTF_8))
            } ?: throw IOException("Não foi possível abrir o arquivo para escrita")
        }
    }

    suspend fun importar(context: Context, repository: LoanRepository, uri: Uri) {
        withContext(Dispatchers.IO) {
            val texto = context.contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: throw IOException("Não foi possível abrir o arquivo para leitura")
            repository.importarDeJson(texto)
        }
    }
}
