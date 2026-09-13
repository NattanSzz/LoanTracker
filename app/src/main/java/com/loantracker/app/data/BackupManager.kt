package com.loantracker.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Backup manual (restaurar) e automático (pasta de destino escolhida pelo
 * usuário via seletor do sistema — Storage Access Framework). O arquivo de
 * backup automático tem nome fixo e é sempre sobrescrito, nunca acumula.
 */
object BackupManager {

    private const val NOME_ARQUIVO_BACKUP = "controle_emprestimos_backup.json"
    private const val PREFS = "backup_prefs"
    private const val CHAVE_PASTA_URI = "pasta_backup_uri"

    /** Usuário escolheu uma pasta: guarda a permissão e já grava um backup nela imediatamente. */
    suspend fun escolherDestinoEExportar(context: Context, repository: LoanRepository, pastaUri: Uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.takePersistableUriPermission(
                pastaUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(CHAVE_PASTA_URI, pastaUri.toString())
                .apply()
            escreverBackupNaPasta(context, repository, pastaUri)
        }
    }

    /** Chamado uma vez por dia pelo worker em segundo plano — não faz nada se nenhuma pasta foi escolhida ainda. */
    suspend fun executarBackupAutomaticoSeConfigurado(context: Context, repository: LoanRepository) {
        val pastaUri = obterPastaDestino(context) ?: return
        withContext(Dispatchers.IO) {
            try {
                escreverBackupNaPasta(context, repository, pastaUri)
            } catch (e: Exception) {
                // pasta pode ter sido apagada/revogada pelo usuário — ignora silenciosamente,
                // o próximo "Escolher destino" resolve.
            }
        }
    }

    fun obterPastaDestino(context: Context): Uri? {
        val texto = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CHAVE_PASTA_URI, null)
            ?: return null
        return Uri.parse(texto)
    }

    private suspend fun escreverBackupNaPasta(context: Context, repository: LoanRepository, pastaUri: Uri) {
        val json = repository.exportarParaJson()
        val pasta = DocumentFile.fromTreeUri(context, pastaUri)
            ?: throw IOException("Pasta de backup inválida")
        val existente = pasta.findFile(NOME_ARQUIVO_BACKUP)
        val arquivo = existente ?: pasta.createFile("application/json", NOME_ARQUIVO_BACKUP)
            ?: throw IOException("Não foi possível criar o arquivo de backup")

        context.contentResolver.openOutputStream(arquivo.uri, "wt")?.use { saida ->
            saida.write(json.toByteArray(Charsets.UTF_8))
        } ?: throw IOException("Não foi possível abrir o arquivo de backup para escrita")
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
