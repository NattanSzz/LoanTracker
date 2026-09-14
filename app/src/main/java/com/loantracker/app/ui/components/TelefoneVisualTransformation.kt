package com.loantracker.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.loantracker.app.data.PhoneUtils

/**
 * Aplica a máscara "(XX) XXXXX-XXXX" / "(XX) XXXX-XXXX" visualmente, mantendo
 * o valor real do campo como só dígitos por trás. Só entra em ação quando há
 * exatamente 10 ou 11 dígitos — fora disso, mostra os dígitos "crus", sem
 * quebrar a digitação de números fora do padrão.
 */
class TelefoneVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val formatado = PhoneUtils.formatarParaExibicao(digitos)

        val mapa = when (digitos.length) {
            11 -> MAPA_11
            10 -> MAPA_10
            else -> null
        }

        val mapeamento = if (mapa == null) {
            OffsetMapping.Identity
        } else {
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    mapa.getOrElse(offset.coerceIn(0, mapa.size - 1)) { formatado.length }

                override fun transformedToOriginal(offset: Int): Int {
                    for (i in mapa.indices.reversed()) {
                        if (offset >= mapa[i]) return i
                    }
                    return 0
                }
            }
        }

        return TransformedText(AnnotatedString(formatado), mapeamento)
    }

    private companion object {
        // Posição (no texto formatado) logo antes de cada dígito, pra manter o
        // cursor no lugar certo enquanto o usuário edita um número já completo.
        val MAPA_11 = intArrayOf(0, 2, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15)
        val MAPA_10 = intArrayOf(0, 2, 5, 6, 7, 8, 10, 11, 12, 13, 14)
    }
}
