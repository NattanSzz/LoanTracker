package com.loantracker.app.data

import android.content.Context
import java.security.MessageDigest

/**
 * Guarda a senha numérica de bloqueio do app. Nunca é salva em texto puro —
 * só o hash (SHA-256 com um salt fixo por instalação) fica no
 * SharedPreferences do próprio app.
 */
object PasswordManager {
    private const val PREFS = "seguranca_prefs"
    private const val CHAVE_HASH = "senha_hash"
    private const val CHAVE_SALT = "senha_salt"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun temSenha(context: Context): Boolean =
        prefs(context).contains(CHAVE_HASH)

    fun definirSenha(context: Context, senha: String) {
        val salt = gerarSalt()
        prefs(context).edit()
            .putString(CHAVE_SALT, salt)
            .putString(CHAVE_HASH, hash(senha, salt))
            .apply()
    }

    fun verificarSenha(context: Context, senha: String): Boolean {
        val p = prefs(context)
        val salt = p.getString(CHAVE_SALT, null) ?: return false
        val hashSalvo = p.getString(CHAVE_HASH, null) ?: return false
        return hashSalvo == hash(senha, salt)
    }

    private fun gerarSalt(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hash(senha: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((salt + senha).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
