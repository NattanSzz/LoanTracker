package com.loantracker.app.data

private const val CODIGO_PAIS_BR = "55"

/**
 * Tudo relacionado a telefone fica centralizado aqui pra manter uma única
 * fonte de verdade entre o cadastro, a edição, a exibição e o link do
 * WhatsApp — e pra não quebrar clientes cadastrados antes desta atualização,
 * que podiam ter qualquer formato livre (com traço, sem DDD, etc.).
 */
object PhoneUtils {

    fun apenasDigitos(texto: String): String = texto.filter { it.isDigit() }

    /**
     * Usado enquanto o usuário digita/cola no campo de telefone. Remove tudo
     * que não for número e, se vier com o código do país colado (ex.: copiado
     * do próprio WhatsApp como "+55 85 99999-9999"), remove o "55" também —
     * o campo sempre trabalha só com DDD + número.
     */
    fun normalizarEntrada(texto: String): String {
        val digitos = apenasDigitos(texto)
        return if (digitos.length in 12..13 && digitos.startsWith(CODIGO_PAIS_BR)) {
            digitos.removePrefix(CODIGO_PAIS_BR)
        } else {
            digitos
        }
    }

    /**
     * Formata pra exibição SÓ quando há exatamente 10 ou 11 dígitos (DDD +
     * número) — fora disso, devolve os dígitos como estão, sem tentar montar
     * uma máscara errada.
     */
    fun formatarParaExibicao(digitosLocais: String): String = when (digitosLocais.length) {
        11 -> "(${digitosLocais.substring(0, 2)}) ${digitosLocais.substring(2, 7)}-${digitosLocais.substring(7, 11)}"
        10 -> "(${digitosLocais.substring(0, 2)}) ${digitosLocais.substring(2, 6)}-${digitosLocais.substring(6, 10)}"
        else -> digitosLocais
    }

    /**
     * Valor que vai pro banco de dados: com o código do país na frente quando
     * o número tiver DDD + número válido (10 ou 11 dígitos). Fora desse caso
     * (menos ou mais dígitos — permitido, só não vira compatível com a
     * formatação/WhatsApp), salva os dígitos como estão, sem forçar nada.
     */
    fun paraArmazenamento(digitosDigitados: String): String = when (digitosDigitados.length) {
        10, 11 -> CODIGO_PAIS_BR + digitosDigitados
        else -> digitosDigitados
    }

    /**
     * A partir de um telefone já salvo (pode ser de antes desta atualização,
     * em qualquer formato livre), extrai só os dígitos de DDD + número, sem o
     * código do país, pra pré-preencher o campo de edição.
     */
    fun paraEdicao(telefoneSalvo: String?): String {
        if (telefoneSalvo.isNullOrBlank()) return ""
        val digitos = apenasDigitos(telefoneSalvo)
        return if (digitos.length in 12..13 && digitos.startsWith(CODIGO_PAIS_BR)) {
            digitos.removePrefix(CODIGO_PAIS_BR)
        } else {
            digitos
        }
    }

    /**
     * Texto pronto pra exibição em telas somente-leitura (perfil do cliente).
     * Se der pra reconhecer um DDD + número válido, mostra formatado; senão,
     * mostra exatamente como está salvo, pra não esconder nem estragar dados
     * antigos que não se encaixam no padrão novo.
     */
    fun paraExibicao(telefoneSalvo: String?): String? {
        if (telefoneSalvo.isNullOrBlank()) return null
        val digitosLocais = paraEdicao(telefoneSalvo)
        return if (digitosLocais.length == 10 || digitosLocais.length == 11) {
            formatarParaExibicao(digitosLocais)
        } else {
            telefoneSalvo
        }
    }

    /**
     * Número pronto pro link do WhatsApp (wa.me exige só dígitos, com código
     * do país, sem símbolos). Retorna null quando não é possível reconhecer
     * um número válido de 10 ou 11 dígitos — quem chama deve avisar o usuário
     * nesse caso, em vez de tentar abrir um link quebrado.
     */
    fun paraWhatsApp(telefoneSalvo: String?): String? {
        if (telefoneSalvo.isNullOrBlank()) return null
        val digitos = apenasDigitos(telefoneSalvo)
        return when {
            digitos.length in 12..13 && digitos.startsWith(CODIGO_PAIS_BR) -> digitos
            digitos.length in 10..11 -> CODIGO_PAIS_BR + digitos
            else -> null
        }
    }
}
